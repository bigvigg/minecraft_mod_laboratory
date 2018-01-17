package com.vigg.common.waypoints;

import java.util.UUID;

import com.vigg.common.ModBlocks;
import com.vigg.common.ModItems;
import com.vigg.common.waypoints.IWaypointStorage.WaypointEntry;
import com.vigg.common.waypoints.ItemWaypointRecorder.RecorderMode;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


// ClientStateManager keeps track of short-lived, client-only data in static variables.

@Mod.EventBusSubscriber
public class ClientStateManager 
{
	@SideOnly(Side.CLIENT)
	private static UUID lastTickRecorderUUID = null; // set every tick in onClientTick
	
	
	
	@SideOnly(Side.CLIENT)
	public static int selectedWaypointIndex = -1;
	
	@SideOnly(Side.CLIENT)
	public static ItemWaypointRecorder.RecorderMode selectedMode = ItemWaypointRecorder.RecorderMode.ADD_REMOVE;
	
	@SideOnly(Side.CLIENT)
	public static Waypoint[] heldRecorderWaypoints = new Waypoint[0];
	
	@SideOnly(Side.CLIENT)
	public static BlockPos targetedPosition = null;
	
	@SideOnly(Side.CLIENT)
	public static WaypointEntry targetedWaypoint = null;
	
	
	@SubscribeEvent	
	@SideOnly(Side.CLIENT)
	public static void onClientTick(TickEvent.ClientTickEvent e)
	{
		// every client tick, update all our static variables
		
		
		ItemWaypointRecorder itemRecorder = ModItems.getWaypointRecorder();
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		
		heldRecorderWaypoints = null;
		targetedWaypoint = null;
		targetedPosition = null;
		
		if (player != null)
		{
			ItemStack heldItem = player.getHeldItemMainhand();
			
			if (heldItem != null && heldItem.getItem() == itemRecorder)
			{
				heldRecorderWaypoints = itemRecorder.getWaypoints(heldItem);
				targetedWaypoint = getTargetedWaypoint();
				
				// show message and reset selected waypoint when the player selects a recorder in their hand
				if (lastTickRecorderUUID == null || !lastTickRecorderUUID.equals(lastTickRecorderUUID))
				{
					ItemWaypointRecorder.showModeMessage(player, heldItem, ClientStateManager.selectedMode);
					selectedWaypointIndex = -1;
				}
				
				// set targetedPosition
				if (selectedMode == RecorderMode.ADD_REMOVE || selectedWaypointIndex > -1)
				{
					// if we're looking at a waypoint beacon, then pretend we have that waypoint's position targeted
					if (targetedWaypoint != null)
						targetedPosition = targetedWaypoint.waypoint.getBlockPos();
					else
						targetedPosition = getTargetedPos();
				}
				
				lastTickRecorderUUID = itemRecorder.getUUID(heldItem);
			}
			else
				lastTickRecorderUUID = null;
			
			// just so other code doesn't have to bother checking for null before looping
			if (heldRecorderWaypoints == null)
				heldRecorderWaypoints = new Waypoint[0];
		}
		else
			lastTickRecorderUUID = null;
	}
	
	@SideOnly(Side.CLIENT)
	private static BlockPos getTargetedPos()
	{
		Minecraft mc = Minecraft.getMinecraft();
		RayTraceResult rayTraceResult = mc.getRenderViewEntity().rayTrace(ItemWaypointRecorder.MAX_WAYPOINT_CLICK_DISTANCE, 1.0F);
		if (rayTraceResult != null)
		{
			BlockPos posClicked = rayTraceResult.getBlockPos();
			
			// try a couple different spots, and put the waypoint on the first one that meets the conditions.
			// if neither spot matches the conditions, then no waypoint is placed, and nothing happens as a result of the player's click.
			BlockPos[] possibleWaypointPositions = new BlockPos[] {
					posClicked,					// first try to place at the actual position clicked (example: player clicks on tall grass)
					posClicked.add(0, 1, 0)		// then try to place above the clicked position (example: player clicks on the ground)
			};
			for (BlockPos possiblePos : possibleWaypointPositions)
			{
				// don't place waypoints very far above the player, because that's probably an accident and doesn't make sense
				if (possiblePos != null && possiblePos.getY() <= (mc.player.getPosition().getY() + 1) && possiblePos.getY() > 0)
				{
					// don't place waypoints inside of solid blocks
					IBlockState possiblePosState = mc.world.getBlockState(possiblePos);
					if (possiblePosState != null && possiblePosState.getMaterial() != null && !possiblePosState.getMaterial().isSolid())
					{
						// only place waypoints on top of solid blocks
						IBlockState blockBelow = mc.player.world.getBlockState(possiblePos.add(0, -1, 0));
						if (blockBelow != null && blockBelow.getMaterial() != null && blockBelow.getMaterial().isSolid())
						{
							return possiblePos;
						}
					}
				}
			}
		}
		
		return null;
	}
	
	
	@SideOnly(Side.CLIENT)
	private static WaypointEntry getTargetedWaypoint()
	{
		// traverse down the player's current line of sight, evaluating each block along the way until one of 3 things happens:
		//	1. we find a solid block, in which case we'll attempt to add a new waypoint at that spot
		//	2. we find a block above an existing waypoint, in which case we'll remove that waypoint
		//	3. we reach the limit defined by MAX_WAYPOINT_CLICK_DISTANCE without either 1 or 2 happening,
		//	   in which case we attempt to add a waypoint there at the distance limit
		
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		WaypointEntry clickedWaypointEntry = null;
		ItemStack recorder = player.getHeldItemMainhand();
		Vec3d lookVec = player.getLookVec();
		Vec3d positionVec = player.getPositionVector().addVector(0, 1, 0);			
		BlockPos lastLineOfSightPos = null;
		
		// distanceCounterIncrementer: how much unevaluated space to leave between the points that we're about to evaluate along the player's line of sight.
		// - A value of 1.0D will leave exactly one block of unevaluated space in between each evaluated point.  
		//   0.5D will leave exactly half a block of unevaluated space between each point.  Etc.
		// - A smaller value will make it less likely to skip over blocks when the player's line of sight crosses
		//   the corners where blocks intersect, BUT it will also mean more trips through the loop (which can be mostly mitigated by checking lastLineOfSightPos)
		double distanceCounterIncrementer = 0.2D;
		
		lineOfSightLoop:
		for (double distanceCounter = 0D; distanceCounter < ItemWaypointRecorder.MAX_WAYPOINT_CLICK_DISTANCE; distanceCounter += 0.2D)
		{
			BlockPos nextLineOfSightPos = new BlockPos(positionVec.add(lookVec.scale(distanceCounter)));
			
			// don't waste time evaluating the same position twice (which will happen if the distanceCounterIncrementer above is less than 1.0D, for greater accuracy)
			if (lastLineOfSightPos != null && lastLineOfSightPos.equals(nextLineOfSightPos))
				continue;
			
			IBlockState nextLineOfSightBlock = player.world.getBlockState(nextLineOfSightPos);
			
			// see if any waypoints are below this next block in the player's line of sight
			for (int waypointIndex = 0; waypointIndex < ClientStateManager.heldRecorderWaypoints.length; waypointIndex++)
			{
				Waypoint nextWaypoint = ClientStateManager.heldRecorderWaypoints[waypointIndex];

				//System.out.println("** STUB - " + nextLineOfSightPos.toString() + " vs " + nextWaypoint.getCoordinateString());
				
				if (nextWaypoint.x == nextLineOfSightPos.getX() && nextWaypoint.z == nextLineOfSightPos.getZ()/* && nextWaypoint.y <= nextLineOfSightPos.getY()*/)
				{
					// Player's line of sight has crossed somewhere above, below, or directly through an existing waypoint.

					// See if there are any solid blocks between player's line of sight and the waypoint
					// (e.g. if the player is on the top floor of a house, and the waypoint is on the bottom floor).
					// If there is no solid block between the player's line of sight and the waypoint,
					// then assume that the player was intentionally attempting to click on that waypoint's visible beacon.
					
					// hacky workaround for situation where player's line of site goes directly UNDER the waypoint, and through
					// the solid block below it (this happens a lot)
					int searchDownStartY = nextLineOfSightPos.getY();
					if (nextWaypoint.y <= positionVec.y)
						searchDownStartY += 1;
					
					for (int searchDownCounter = searchDownStartY; searchDownCounter >= nextWaypoint.y; searchDownCounter--)
					{
						BlockPos nextBlockPos = new BlockPos(nextLineOfSightPos.getX(), searchDownCounter, nextLineOfSightPos.getZ());
						IBlockState nextBlockDownState = player.world.getBlockState(nextBlockPos);
						if (nextBlockDownState.getBlock() == ModBlocks.getBlockWaypoint())
						{
							// we found the waypoint
							clickedWaypointEntry = new WaypointEntry(waypointIndex, nextWaypoint);
							break lineOfSightLoop;
						}
						else if (nextBlockDownState.getMaterial().isSolid())
						{
							// We hit a solid block, so the player probably *wasn't* trying to click this waypoint after all.
							break lineOfSightLoop;
						}
					}						
					// note: it *should* be impossible for code to reach this point, where it never found the waypoint
				}
			} // end waypoint loop
			
			if (nextLineOfSightBlock.getMaterial().isSolid())
			{
				// we hit a solid block.
				// the next code block after lineOfSightLoop will attempt to add a new waypoint at nextLineOfSightPos current position
				break lineOfSightLoop; 
			}
			
			lastLineOfSightPos = nextLineOfSightPos;
		} // end lineOfSightLoop
		
		return clickedWaypointEntry;
	}
	
}
