package com.vigg.common.waypoints;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.vigg.common.ModBlocks;
import com.vigg.common.ModItems;
import com.vigg.common.ModPacketHandler;
import com.vigg.common.Reference;
import com.vigg.common.waypoints.IWaypointStorage.WaypointEntry;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemWaypointRecorder extends Item implements IWaypointStorage<ItemStack>
{
	// static stuff
	
	private final static String ITEMSTACK_UUID_TAG_KEY = "com.vigg.uuid";
	private final static String ITEMSTACK_WAYPOINTS_TAG_KEY = "com.vigg.waypoints";
	private final static String ITEMSTACK_RECORDER_MODE_TAG_KEY = "com.vigg.wprecorder.mode";
	private final static String ITEMSTACK_RECORDER_SELECTED_WAYPOINT_INDEX_TAG_KEY = "com.vigg.wprecorder.selectedIndex";
	
	public final static double MAX_WAYPOINT_CLICK_DISTANCE = 50D;
	
	
	@SideOnly(Side.CLIENT)
	public static void showModeMessage(EntityPlayer player, ItemStack recorder, RecorderMode mode)
	{
		String message;
		
		if (mode == RecorderMode.ADD_REMOVE)
		{
			message = recorder.getDisplayName() + " is in Add/Remove Mode";
		}
		else
		{
			message = recorder.getDisplayName() + " is in Edit Mode";
		}
		
		player.sendStatusMessage(new TextComponentString(message), true);
	}
	
	
	
	// instance (non-static) stuff
	
	
	public ItemWaypointRecorder() 
	{
	    super();

	    String className = this.getClass().getSimpleName();
	    
	    this.setRegistryName(Reference.MOD_ID, className);
	    this.setUnlocalizedName(className);
	    this.setMaxStackSize(1);
	    this.setCreativeTab(CreativeTabs.TOOLS);
	}
	
	
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) 
	{
		System.out.println("STUB onItemUse");
		// TODO Auto-generated method stub
		return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
	}


	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) 
	{
		System.out.println("STUB onItemRightClick");
		
		ItemStack recorder = playerIn.getHeldItem(handIn);
				
		if (worldIn.isRemote)
		{
			// client side
			
			
			if (playerIn.isSneaking())
			{
				// player shift+right clicked - toggle recorder mode
				
				RecorderMode newMode;
				if (ClientStateManager.selectedMode == RecorderMode.ADD_REMOVE)
					newMode = RecorderMode.EDIT;
				else
					newMode = RecorderMode.ADD_REMOVE;
				
				ClientStateManager.selectedMode = newMode;
				showModeMessage(playerIn, recorder, newMode);
			}
			else
			{
				// player did a normal right click, without the shift key
				
				if (ClientStateManager.selectedMode == RecorderMode.ADD_REMOVE)
					handleRightClick_AddRemoveMode(worldIn, playerIn, handIn);
				else
				{
					handleRightClick_EditMode(worldIn, playerIn, handIn);
				}
			}
		}
		
		return super.onItemRightClick(worldIn, playerIn, handIn);
	}
	
	@SideOnly(Side.CLIENT)
	private void handleRightClick_AddRemoveMode(World worldIn, EntityPlayer playerIn, EnumHand handIn)
	{
		ItemStack recorder = playerIn.getHeldItem(handIn);
		WaypointEntry clickedWaypointEntry = getTargetedWaypoint(playerIn);

		if (clickedWaypointEntry == null)
		{
			// attempt to add a new waypoint entry
			
			if (ClientStateManager.targetedPosition != null)
			{
				// make sure we aren't trying to add two waypoints to the same spot.        									
				// if we are, then assume the player intended to remove the existing waypoint.
				
				WaypointEntry existingWaypoint = getWaypoint(recorder, ClientStateManager.targetedPosition.getX(), ClientStateManager.targetedPosition.getY(), ClientStateManager.targetedPosition.getZ());
				if (existingWaypoint == null)
				{
					// attempt to add the new waypoint
					ModPacketHandler.INSTANCE.sendToServer(new MessageAddWaypointToRecorder(
							ModItems.getWaypointRecorder().getUUID(playerIn.getHeldItem(handIn)), 
							ClientStateManager.targetedPosition.getX(),
							ClientStateManager.targetedPosition.getY(),
							ClientStateManager.targetedPosition.getZ()
					));
				}
				else
				{
					// set clickedWaypointEntry so that the existing waypoint will be removed by the code below
					clickedWaypointEntry = existingWaypoint;
				}	
			}
		}

		if (clickedWaypointEntry != null)
		{
			// attempt to remove the waypoint that the player clicked on
			ModPacketHandler.INSTANCE.sendToServer(new MessageRemoveWaypointFromRecorder(getUUID(recorder), clickedWaypointEntry));
		}
	}

	@SideOnly(Side.CLIENT)
	private void handleRightClick_EditMode(World worldIn, EntityPlayer playerIn, EnumHand handIn)
	{
		// remember - the selected waypoint purely client side, and the nbt tag never gets set on the server
		
		ItemStack recorder = playerIn.getHeldItem(handIn);
		WaypointEntry clickedWaypointEntry = getTargetedWaypoint(playerIn);

		if (clickedWaypointEntry == null)
		{
			if (ClientStateManager.selectedWaypointIndex > -1 && ClientStateManager.targetedPosition != null && ClientStateManager.selectedWaypointIndex < ClientStateManager.heldRecorderWaypoints.length)
			{
				// move the selected waypoint to the targeted position
				
				WaypointEntry selectedWaypointEntry = new WaypointEntry(
						ClientStateManager.selectedWaypointIndex, 
						ClientStateManager.heldRecorderWaypoints[ClientStateManager.selectedWaypointIndex]
				);
				
				selectedWaypointEntry.waypoint.x = ClientStateManager.targetedPosition.getX();
				selectedWaypointEntry.waypoint.y = ClientStateManager.targetedPosition.getY();
				selectedWaypointEntry.waypoint.z = ClientStateManager.targetedPosition.getZ();
				
				ModPacketHandler.INSTANCE.sendToServer(new MessageUpdateWaypointOnRecorder(getUUID(recorder), selectedWaypointEntry));
				ClientStateManager.selectedWaypointIndex = -1;
			}
		}
		else
		{
			if (ClientStateManager.selectedWaypointIndex == -1)
				ClientStateManager.selectedWaypointIndex = clickedWaypointEntry.index; // select the clicked waypoint
			else if (ClientStateManager.selectedWaypointIndex == clickedWaypointEntry.index)
				ClientStateManager.selectedWaypointIndex = -1; // clicked on same waypoint twice: deselect it
			else
			{
				// swap the two waypoints
				ModPacketHandler.INSTANCE.sendToServer(new MessageSwapWaypointsOnRecorder(getUUID(recorder), ClientStateManager.selectedWaypointIndex, clickedWaypointEntry.index));
				ClientStateManager.selectedWaypointIndex = -1;
			}
		}
	}
	
	// NOTE: this function doesn't ever seem to actually get called, even though it seems like it SHOULD
	// be getting called, according to the vanilla code
	/*
	@Override
	public boolean updateItemStackNBT(NBTTagCompound nbt) {
		System.out.println("** STUB2 updateItemStackNBT " + nbt.toString());
		return true;

		// TODO Auto-generated method stub
		//return super.updateItemStackNBT(nbt);
	}
	*/


	@SideOnly(Side.CLIENT)
	private WaypointEntry getTargetedWaypoint(EntityPlayer playerIn)
	{
		// traverse down the player's current line of sight, evaluating each block along the way until one of 3 things happens:
		//	1. we find a solid block, in which case we'll attempt to add a new waypoint at that spot
		//	2. we find a block above an existing waypoint, in which case we'll remove that waypoint
		//	3. we reach the limit defined by MAX_WAYPOINT_CLICK_DISTANCE without either 1 or 2 happening,
		//	   in which case we attempt to add a waypoint there at the distance limit
		
		WaypointEntry clickedWaypointEntry = null;
		ItemStack recorder = playerIn.getHeldItemMainhand();
		Vec3d lookVec = playerIn.getLookVec();
		Vec3d positionVec = playerIn.getPositionVector().addVector(0, 1, 0);			
		BlockPos lastLineOfSightPos = null;
		
		// distanceCounterIncrementer: how much unevaluated space to leave between the points that we're about to evaluate along the player's line of sight.
		// - A value of 1.0D will leave exactly one block of unevaluated space in between each evaluated point.  
		//   0.5D will leave exactly half a block of unevaluated space between each point.  Etc.
		// - A smaller value will make it less likely to skip over blocks when the player's line of sight crosses
		//   the corners where blocks intersect, BUT it will also mean more trips through the loop (which can be mostly mitigated by checking lastLineOfSightPos)
		double distanceCounterIncrementer = 0.2D;
		
		lineOfSightLoop:
		for (double distanceCounter = 0D; distanceCounter < MAX_WAYPOINT_CLICK_DISTANCE; distanceCounter += 0.2D)
		{
			BlockPos nextLineOfSightPos = new BlockPos(positionVec.add(lookVec.scale(distanceCounter)));
			
			// don't waste time evaluating the same position twice (which will happen if the distanceCounterIncrementer above is less than 1.0D, for greater accuracy)
			if (lastLineOfSightPos != null && lastLineOfSightPos.equals(nextLineOfSightPos))
				continue;
			
			IBlockState nextLineOfSightBlock = playerIn.world.getBlockState(nextLineOfSightPos);
			
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
						IBlockState nextBlockDownState = playerIn.world.getBlockState(nextBlockPos);
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
	
	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) 
	{
		System.out.println("STUB onLeftClickEntity");
		// TODO Auto-generated method stub
		return super.onLeftClickEntity(stack, player, entity);
	}


	@Override
	public void onCreated(ItemStack stack, World worldIn, EntityPlayer playerIn) 
	{
		System.out.println("STUB onCreated");
		
		// TODO Auto-generated method stub
		super.onCreated(stack, worldIn, playerIn);
	}
	
	@Override
	public boolean onDroppedByPlayer(ItemStack item, EntityPlayer player) 
	{
		System.out.println("STUB onDroppedByPlayer");
		
		// TODO Auto-generated method stub
		return super.onDroppedByPlayer(item, player);
	}


	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) 
	{
		super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);
		
		if (worldIn.isRemote)
		{
			// client side
			
			
			if (isSelected)
			{
				// Make sure the BlockWaypoint is spawned for each current waypoint.
				// Since these waypoint blocks are 100% just-for-looks and do not interact with anything in the world,
				// I'm gonna go against common wisdom and create them *only* on the client side.
				UUID recorderID = getUUID(stack);
				for (int i = 0; i < ClientStateManager.heldRecorderWaypoints.length; i++)
				{
					Waypoint wp = ClientStateManager.heldRecorderWaypoints[i];
					spawnWaypointTileEntity(recorderID, new BlockPos(wp.x, wp.y, wp.z), true);
				}
				
				// also show a waypoint tile entity on the space the player is currently targeting
				if (ClientStateManager.targetedPosition != null)
				{
					spawnWaypointTileEntity(recorderID, ClientStateManager.targetedPosition, true);
				}
			}
		}
		else
		{
			// server side
			
			ensureUUID(stack);
		}
	}
    
	@SideOnly(Side.CLIENT)
	private void spawnWaypointTileEntity(UUID recorderID, BlockPos pos, boolean showNameplate)
	{
		Minecraft mc = Minecraft.getMinecraft();
		IBlockState waypointState = ModBlocks.getBlockWaypoint().getDefaultState();
		
		IBlockState originalState = mc.world.getBlockState(pos);
		if (originalState != waypointState)
		{
			TileEntity originalEntity = mc.world.getTileEntity(pos);
			NBTTagCompound originalEntityState = null;
			if (originalEntity != null)
				originalEntityState = originalEntity.serializeNBT();
			
			mc.world.setBlockState(pos, waypointState);
			
			// initialize the TileEntityWaypoint
			TileEntity te = mc.world.getTileEntity(pos);
			if (te instanceof TileEntityWaypoint)
			{
				((TileEntityWaypoint)te).initWaypoint(recorderID, mc.player, originalState, originalEntityState, showNameplate);
			}
		}
	}
	
    @Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) 
    {
		super.addInformation(stack, worldIn, tooltip, flagIn);

		if (Reference.DEBUG)
    	{
    		UUID stackID = getUUID(stack);
    		if (stackID == null)
    			tooltip.add("uuid: null");
    		else
    			tooltip.add("uuid: " + stackID.toString());
    	}
		
		if (worldIn != null && worldIn.isRemote)
		{
			if (ClientStateManager.selectedMode == RecorderMode.ADD_REMOVE)
			{
				tooltip.add("Right click to add a waypoint. Right click again to remove it.");
				tooltip.add("Sneak + right click for Edit Mode");
			}
			else
			{
				tooltip.add("Right click two waypoints to swap their #'s");
				tooltip.add("Sneak + right click for Add/Remove Mode");
			}
		}
    	
		tooltip.add(Integer.toString(getWaypointCount(stack)) + " waypoints");
		if (Reference.DEBUG)
    	{
			Waypoint[] waypoints = getWaypoints(stack);
			
			for (int i = 0; i < waypoints.length; i++)
			{
				Waypoint wp = waypoints[i];
				String line = " " + wp.getLabel();
				if (wp.hasCustomLabel())
					line += " " + wp.getCoordinateString();
				tooltip.add(line);
			}
    	}
	}


    
    // IWaypointStorage stuff
    
	@Override
    public Waypoint[] getWaypoints(ItemStack container)
    {
    	if (container.hasTagCompound())
    	{
    		NBTTagCompound nbtTag = container.getTagCompound();
    		if (nbtTag.hasKey(ITEMSTACK_WAYPOINTS_TAG_KEY))
    		{
    			NBTTagList nbtWaypointList = (NBTTagList)nbtTag.getTagList(ITEMSTACK_WAYPOINTS_TAG_KEY, Constants.NBT.TAG_COMPOUND);
    			int waypointCount = nbtWaypointList.tagCount();
    			Waypoint[] waypoints = new Waypoint[waypointCount];
    			for (int i = 0; i < waypointCount; i++)
    			{
    				Waypoint wp = new Waypoint();
    				wp.deserializeNBT(nbtWaypointList.getCompoundTagAt(i));
    				waypoints[i] = wp;
    			}
    			
    			return waypoints;
    		}
    	}

    	return new Waypoint[] {};
    }


	@Override
	public void setWaypoints(ItemStack container, Waypoint[] waypoints) 
	{
		NBTTagList nbtList = new NBTTagList();
		
		for (int i = 0; i < waypoints.length; i++)
		{
			nbtList.appendTag(waypoints[i].serializeNBT());
		}
		
		container.setTagInfo(ITEMSTACK_WAYPOINTS_TAG_KEY, nbtList);
	}
	
	@Override
	public int addWaypoint(ItemStack container, Waypoint waypoint)
	{
		if (!container.hasTagCompound())
			container.setTagCompound(new NBTTagCompound());

		NBTTagCompound nbtTag = container.getTagCompound();
		if (!nbtTag.hasKey(ITEMSTACK_WAYPOINTS_TAG_KEY))
			nbtTag.setTag(ITEMSTACK_WAYPOINTS_TAG_KEY, new NBTTagList());

		NBTTagList nbtWaypointList = (NBTTagList)nbtTag.getTagList(ITEMSTACK_WAYPOINTS_TAG_KEY, Constants.NBT.TAG_COMPOUND);
		nbtWaypointList.appendTag(waypoint.serializeNBT());
		
		return (nbtWaypointList.tagCount() - 1);
	}
	
	@Override
	public void removeWaypoint(ItemStack container, int index)
	{
		List<Waypoint> waypoints = new LinkedList<Waypoint>(Arrays.asList(getWaypoints(container)));
		waypoints.remove(index);
		setWaypoints(container, waypoints.toArray(new Waypoint[waypoints.size()]));
	}
	
	@Override
	public int getWaypointCount(ItemStack container)
	{
		if (container.hasTagCompound())
    	{
    		NBTTagCompound nbtTag = container.getTagCompound();
    		if (nbtTag.hasKey(ITEMSTACK_WAYPOINTS_TAG_KEY))
    		{
    			NBTTagList nbtWaypointList = (NBTTagList)nbtTag.getTagList(ITEMSTACK_WAYPOINTS_TAG_KEY, Constants.NBT.TAG_COMPOUND);
    			return nbtWaypointList.tagCount();
    		}
    	}

		return 0;
	}
	
	@Override
	public IWaypointStorage.WaypointEntry getWaypoint(ItemStack container, int x, int y, int z)
	{
		if (container.hasTagCompound())
    	{
    		NBTTagCompound nbtTag = container.getTagCompound();
    		if (nbtTag.hasKey(ITEMSTACK_WAYPOINTS_TAG_KEY))
    		{
    			NBTTagList nbtWaypointList = (NBTTagList)nbtTag.getTagList(ITEMSTACK_WAYPOINTS_TAG_KEY, Constants.NBT.TAG_COMPOUND);
    			int waypointCount = nbtWaypointList.tagCount();
    			
    			for (int i = 0; i < waypointCount; i++)
    			{
    				Waypoint wp = new Waypoint();
    				wp.deserializeNBT(nbtWaypointList.getCompoundTagAt(i));
    				
    				if (wp.x == x && wp.y == y && wp.z == z)
    					return new IWaypointStorage.WaypointEntry(i, wp);
    			}
    			
    		}
    	}

		return null;
	}
	
	@Override
	public Waypoint getWaypoint(ItemStack container, int index)
	{
		if (container.hasTagCompound())
    	{
    		NBTTagCompound nbtTag = container.getTagCompound();
    		if (nbtTag.hasKey(ITEMSTACK_WAYPOINTS_TAG_KEY))
    		{
    			NBTTagList nbtWaypointList = (NBTTagList)nbtTag.getTagList(ITEMSTACK_WAYPOINTS_TAG_KEY, Constants.NBT.TAG_COMPOUND);
    			
    			if (index < nbtWaypointList.tagCount())
    			{
	    			Waypoint wp = new Waypoint();
	    			wp.deserializeNBT(nbtWaypointList.getCompoundTagAt(index));
	    			return wp;
    			}
    		}
    	}

		return null;
	}
	
	
	
	// uuid stuff
	// note: these functions could be made into an Item base class if we need UUID in other stuff
	
	public UUID getUUID(ItemStack stack)
	{
		if (stack.hasTagCompound())
		{
			NBTTagCompound nbtTag = stack.getTagCompound();
			
			if (nbtTag.hasKey(ITEMSTACK_UUID_TAG_KEY))
				return UUID.fromString(nbtTag.getString(ITEMSTACK_UUID_TAG_KEY));
		}
		
		return null;
	}

	public void ensureUUID(ItemStack stack)
	{
		if (!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		
		NBTTagCompound nbtTag = stack.getTagCompound();
		if (!nbtTag.hasKey(ITEMSTACK_UUID_TAG_KEY)) 
		{
			System.out.println("STUB GENERATING UUID");
			nbtTag.setString(ITEMSTACK_UUID_TAG_KEY, UUID.randomUUID().toString());
		}
	}
	
	public enum RecorderMode
	{
		ADD_REMOVE,
		EDIT
	}
}


