package com.vigg.common.waypoints;

import java.util.UUID;

import com.vigg.common.ModItems;
import com.vigg.common.ModPacketHandler;
import com.vigg.common.waypoints.IWaypointStorage.WaypointEntry;
import com.vigg.common.waypoints.ItemWaypointRecorder.RecorderMode;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
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
	
	
	@SubscribeEvent	
	@SideOnly(Side.CLIENT)
	public static void onClientTick(TickEvent.ClientTickEvent e)
	{		
		// Whenever a recorder is selected in the player's hand, show them a message to remind them what mode it's in.
		// (it's weird that I can't find any sort of ItemSelected event hook, and instead have to do it this way, by checking every tick)
		
		ItemWaypointRecorder itemRecorder = ModItems.getWaypointRecorder();
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		heldRecorderWaypoints = null;
		targetedPosition = null;
		
		if (player != null)
		{
			ItemStack heldItem = player.getHeldItemMainhand();
			UUID heldItemUUID = null;
			
			if (heldItem != null && heldItem.getItem() == itemRecorder)
			{
				heldItemUUID = itemRecorder.getUUID(heldItem);
				heldRecorderWaypoints = itemRecorder.getWaypoints(heldItem);

				// show the mode message when the player selects a recorder in their hand
				if (heldItemUUID != null && (lastTickRecorderUUID == null || !heldItemUUID.equals(lastTickRecorderUUID)))
				{
					ItemWaypointRecorder.showModeMessage(player, heldItem, ClientStateManager.selectedMode);
					selectedWaypointIndex = -1;
				}
				
				// update the targeted position
				if (selectedMode == RecorderMode.ADD_REMOVE || selectedWaypointIndex > -1)
					targetedPosition = getTargetedPos();
			}
			
			lastTickRecorderUUID = heldItemUUID;
			
			if (heldRecorderWaypoints == null)
				heldRecorderWaypoints = new Waypoint[0];
		}
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
}
