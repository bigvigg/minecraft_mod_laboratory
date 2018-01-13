package com.vigg.common.waypoints;

import java.util.List;
import java.util.UUID;

import com.vigg.common.ModBlocks;
import com.vigg.common.ModItems;
import com.vigg.common.ModPacketHandler;
import com.vigg.common.Reference;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
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
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

public class ItemWaypointRecorder extends Item implements IWaypointStorage<ItemStack>
{
	private final static String ITEMSTACK_UUID_TAG_KEY = "com.vigg.uuid";
	private final static String ITEMSTACK_WAYPOINTS_TAG_KEY = "com.vigg.waypoints";
	private final static double MAX_WAYPOINT_CLICK_DISTANCE = 50D;
		
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
		
		if (worldIn.isRemote)
		{
			// STUB - testing
			WaypointEntry clickedWaypointEntry = null;
			ItemStack recorder = playerIn.getHeldItem(handIn);
			Waypoint[] waypoints = getWaypoints(recorder);
			Vec3d lookVec = playerIn.getLookVec();
			Vec3d positionVec = playerIn.getPositionVector();
			
			BlockPos nextLineOfSightPos = null;
			lineOfSightLoop:
			for (double distanceCounter = 0D; distanceCounter < MAX_WAYPOINT_CLICK_DISTANCE; distanceCounter++)
			{
				nextLineOfSightPos = new BlockPos(positionVec.add(lookVec.scale(distanceCounter)));
				IBlockState nextLineOfSightBlock = worldIn.getBlockState(nextLineOfSightPos);
								
				if (nextLineOfSightBlock.getMaterial().isSolid())
				{
					// we hit a solid block.
					// the next code block after lineOfSightLoop will attempt to add a new waypoint at nextLineOfSightPos current position
					break lineOfSightLoop; 
				}
				
				// see if any waypoints are below this next block in the player's line of sight
				for (int waypointIndex = 0; waypointIndex < waypoints.length; waypointIndex++)
				{
					Waypoint nextWaypoint = waypoints[waypointIndex];
					
					if (nextWaypoint.x == nextLineOfSightPos.getX() && nextWaypoint.z == nextLineOfSightPos.getZ() && nextWaypoint.y <= nextLineOfSightPos.getY())
					{
						// Player's line of sight has crossed somewhere above an existing waypoint.
						//
						// See if there are any solid blocks between player's line of sight and the waypoint
						// (e.g. if the player is on the top floor of a house, and the waypoint is on the bottom floor).
						// If there is no solid block between the player's line of sight and the waypoint,
						// then assume that the player was intentionally attempting to click on that waypoint's visible beacon.
						for (int lineOfSightToWaypointCounter = nextLineOfSightPos.getY(); lineOfSightToWaypointCounter >= nextWaypoint.y; lineOfSightToWaypointCounter--)
						{
							if (worldIn.getBlockState(new BlockPos(nextLineOfSightPos.getX(), lineOfSightToWaypointCounter, nextLineOfSightPos.getZ())).getMaterial().isSolid())
							{
								// We hit a solid block, so the player probably *wasn't* trying to click this waypoint after all.
								break;
							}
						}
						
						// assume that the player was trying to click on this waypoint beacon
						clickedWaypointEntry = new WaypointEntry(waypointIndex, nextWaypoint);
						break lineOfSightLoop;
					}
				}
			} // end LineOfSightLoop
			
			
			
			if (clickedWaypointEntry == null)
			{
				// attempt to add a new waypoint entry
				
				// try a few different spots, and put the waypoint on the first one that meets the conditions
				BlockPos[] possibleWaypointPositions = new BlockPos[] {
						nextLineOfSightPos,					// first try to place at the actual position clicked (example: player clicks on tall grass)
						nextLineOfSightPos.add(0, 1, 0)		// then try to place above the clicked position (example: player clicks on the ground)
				};
				for (BlockPos possiblePos : possibleWaypointPositions)
				{
					// don't place waypoints very far above the player, because that's probably an accident and doesn't make sense
					if (possiblePos != null && possiblePos.getY() <= (playerIn.getPosition().getY() + 1) && possiblePos.getY() > 0)
					{
						// don't place waypoints inside of solid blocks
						IBlockState possiblePosState = worldIn.getBlockState(possiblePos);
						if (possiblePosState != null && possiblePosState.getMaterial() != null && !possiblePosState.getMaterial().isSolid())
						{
							// only place waypoints on top of solid blocks
							IBlockState blockBelow = worldIn.getBlockState(possiblePos.add(0, -1, 0));
							if (blockBelow != null && blockBelow.getMaterial() != null && blockBelow.getMaterial().isSolid())
							{
								ItemWaypointRecorder itemWaypointRecorder = ModItems.getWaypointRecorder();
								ItemStack heldItem = playerIn.getHeldItem(handIn);
								if (heldItem != null && heldItem.getItem() == itemWaypointRecorder)
								{
									Waypoint newWaypoint = new Waypoint(possiblePos.getX(), possiblePos.getY(), possiblePos.getZ());
									ModPacketHandler.INSTANCE.sendToServer(new MessageAddWaypointToRecorder(itemWaypointRecorder.getUUID(heldItem), newWaypoint));
								}
							}
						}
					}
				}
			}
			else
			{
				// the player clicked on an existing waypoint
				ModPacketHandler.INSTANCE.sendToServer(new MessageRemoveWaypointFromRecorder(getUUID(recorder), clickedWaypointEntry));
			}
		}
		
		return super.onItemRightClick(worldIn, playerIn, handIn);
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
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);
		
		if (worldIn.isRemote)
		{
			// client side
			
			
			if (isSelected)
			{
				Waypoint[] waypoints = getWaypoints(stack);
				
				// Make sure the BlockWaypoint is spawned for each current waypoint.
				// Since these waypoint blocks are 100% just-for-looks and do not interact with anything in the world,
				// I'm gonna go against common wisdom and create them *only* on the client side.
				for (int i = 0; i < waypoints.length; i++)
				{
					Waypoint wp = waypoints[i];
					BlockPos pos = new BlockPos(wp.x, wp.y, wp.z);
					IBlockState waypointState = ModBlocks.getBlockWaypoint().getDefaultState();
					
					IBlockState originalState = worldIn.getBlockState(pos);
					TileEntity originalEntity = worldIn.getTileEntity(pos);
					if (originalState != waypointState && originalEntity == null) // don't destroy other TileEntities to show the waypoint, since that could potentially cause freaky client-side bugs/crashes
					{
						worldIn.setBlockState(pos, waypointState);
						
						// initialize the TileEntityWaypoint
						TileEntity te = worldIn.getTileEntity(pos);
						if (te instanceof TileEntityWaypoint)
						{
							((TileEntityWaypoint)te).initWaypoint(getUUID(stack), (EntityPlayer)entityIn, originalState);
						}
					}
				}
			}
		}
		else
		{
			// server side
			
			ensureUUID(stack);
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
    	
		tooltip.add(this.getWaypointCount(stack) + " waypoints");
	}



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

	
}


