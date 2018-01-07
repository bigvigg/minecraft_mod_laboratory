package com.vigg.common.items;

import java.util.List;

import com.vigg.common.Reference;
import com.vigg.common.Waypoint;
import com.vigg.common.network.AddWaypointToRecorderMessage;
import com.vigg.common.network.ModPacketHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class WaypointRecorder extends SynchingItem 
{
	public static final String NBT_KEY = "com.vigg.waypoints";
	
	
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
					mc.player.sendChatMessage("stub: clicked " + blockClicked.getX() + ", " + blockClicked.getY() + ", " + blockClicked.getZ());
					
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
		// TODO Auto-generated method stub
		super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);
	}


	/*
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) 
	{
		return super.initCapabilities(stack, nbt);
	}
	*/
 
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List lores, boolean b)
    {
    	super.addInformation(stack, player, lores, b);
    	
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey(NBT_KEY))
        {
        	NBTTagList waypoints = (NBTTagList)stack.getTagCompound().getTag(NBT_KEY);
        			
            lores.add(Integer.toString(waypoints.tagCount()) + " waypoints");
        }
    }
    
	
}


