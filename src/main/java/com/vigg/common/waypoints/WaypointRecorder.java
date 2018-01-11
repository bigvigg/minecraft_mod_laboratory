package com.vigg.common.waypoints;

import java.util.List;
import java.util.UUID;

import com.vigg.common.ModBlocks;
import com.vigg.common.ModItems;
import com.vigg.common.ModPacketHandler;
import com.vigg.common.Reference;
import com.vigg.common.Waypoint;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
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
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

public class WaypointRecorder extends Item implements IWaypointStorage<ItemStack>
{
	public final static String ITEMSTACK_UUID_TAG_KEY = "com.vigg.uuid";
	public final static String ITEMSTACK_WAYPOINTS_TAG_KEY = "com.vigg.waypoints";
		
	public WaypointRecorder() 
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
			Minecraft mc = Minecraft.getMinecraft();
			
			RayTraceResult objectClicked = mc.objectMouseOver;
			if (objectClicked != null)
			{
				BlockPos blockClicked = objectClicked.getBlockPos();
				if (blockClicked != null && blockClicked.getY() <= playerIn.getPosition().getY())
				{
					ItemStack waypointRecorder = playerIn.getHeldItem(handIn);
					if (waypointRecorder != null && waypointRecorder.getItem() == ModItems.getWaypointRecorder())
					{
						Waypoint newWaypoint = new Waypoint(blockClicked.getX(), blockClicked.getY(), blockClicked.getZ());
						ModPacketHandler.INSTANCE.sendToServer(new AddWaypointToRecorderMessage(newWaypoint));
					}
				}
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
			
			Waypoint[] waypoints = getWaypoints(stack);
			
			// Make sure the beacon block is spawned for each current waypoint.
			// Since these waypoint blocks are 100% just-for-looks and do not interact with anything in the world,
			// I'm gonna go against common wisdom and create them *only* on the client side.
			for (int i = 0; i < waypoints.length; i++)
			{
				Waypoint wp = waypoints[i];
				BlockPos pos = new BlockPos(wp.x, wp.y, wp.z);
				IBlockState waypointState = ModBlocks.getBlockWaypoint().getDefaultState();
				
				IBlockState currentState = worldIn.getBlockState(pos);
				if (currentState != waypointState)
				{
					System.out.println("STUB setting waypoint blockstate at " + pos.toString());
					worldIn.setBlockState(pos,  waypointState);
					
					TileEntity te = worldIn.getTileEntity(pos);
					if (te instanceof TileEntityWaypoint)
					{
						((TileEntityWaypoint)te).updateBeacon();
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

	/*
	@Override
	public NBTTagCompound getNBTShareTag(ItemStack stack) 
	{
		System.out.println("STUB getNBTShareTag()");
		
		
		
		return super.getNBTShareTag(stack);		
	}
	*/
	
}


