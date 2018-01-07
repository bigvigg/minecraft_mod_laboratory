package com.vigg.common.items;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import com.vigg.common.Reference;
import com.vigg.common.capabilities.synchingitem.ISynchingItemCapability;
import com.vigg.common.capabilities.synchingitem.SynchingItemCapabilityProvider;
import com.vigg.common.network.ModPacketHandler;
import com.vigg.common.network.RequestSynchingItemDataMessage;
import com.vigg.common.network.ResponseSynchingItemDataMessage;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class SynchingItem extends Item 
{
	public static final String NBT_UUID_KEY = "com.vigg.uuid";
	
	// static helper function
	public static UUID getUUID(ItemStack stack)
	{
		if (stack == null)
			return null;
		
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null)
			return null;
		if (!nbt.hasKey(NBT_UUID_KEY))
			return null;
		
		return UUID.fromString(nbt.getString(NBT_UUID_KEY));
	}
	

	public SynchingItem() 
	{
	    super();

	    String className = this.getClass().getSimpleName();
	    
	    //this.setRegistryName(Reference.MOD_ID, className);
	    this.setUnlocalizedName(className);
	}
	

	@Override
	public void onCreated(ItemStack stack, World worldIn, EntityPlayer playerIn) 
	{
		super.onCreated(stack, worldIn, playerIn);
		
	}
	
	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);
		
		UUID itemUUID = SynchingItem.getUUID(stack);
		
		if (worldIn.isRemote)
		{
			// client side
			
			// note: it takes a moment for the server to automatically send us the stack's uuid when the stack is first created,
			// so we have to check and make sure that's already happened before proceeding
			if (itemUUID != null)
			{
				ISynchingItemCapability synchCap = stack.getCapability(SynchingItemCapabilityProvider.SYNCHING_ITEM_CAPABILITY, null);
				
				// on the client, request a capabilities update from the server if necessary
				// note that synchCap.isDirty = true by default, so this will always happen on the client for newly instantiated ItemStacks
				if (synchCap.isDirty())
				{
					// don't send a new request unless it's been more than 5000 milliseconds since the last time we sent one
					long currentTime = System.currentTimeMillis();
					if (currentTime - synchCap.lastRequestTime() > 5000)
					{
						// ask the server to send us the latest nbt data for this ItemStack
						ModPacketHandler.INSTANCE.sendToServer(new RequestSynchingItemDataMessage(itemSlot, itemUUID));
						synchCap.setLastRequestTime(currentTime);
					}
				}
			}
		}
		else
		{
			// server-side
			
			ISynchingItemCapability synchCap = stack.getCapability(SynchingItemCapabilityProvider.SYNCHING_ITEM_CAPABILITY, null);
		
			if (itemUUID == null)
			{
				// Do some one-time server side initialization stuff for this ItemStack.
				//
				// Note: it would be more efficient to put this in onCreated instead of checking every update, 
				// but that only gets called when you craft the item normally (i.e. not when you grab the item
				// from the creative mode inventory, spawn it with commands, etc.)  We need the stuff below to
				// ALWAYS happen when a new SynchingItem ItemStack comes into existence, and I haven't found
				// a better solution than to just check every update
				
				// Generate this ItemStack's permanent ID, which *should* never change throughout the existence of the ItemStack.
				NBTTagCompound nbt = stack.getTagCompound();
				if (nbt == null)
					nbt = new NBTTagCompound();
				nbt.setString(NBT_UUID_KEY, UUID.randomUUID().toString());
				stack.setTagCompound(nbt);
				
				
				// make sure that isDirty is false on the server side when the ItemStack is instantiated
				synchCap.setIsDirty(false);
			}
			else
			{
				// see if we need to send an unrequested synch to the client
				if (synchCap.isDirty())
				{
					// don't send a new update unless it's been more than 5000 milliseconds since the last time we sent one
					long currentTime = System.currentTimeMillis();
					if (currentTime - synchCap.lastResponseTime() > 5000)
					{
						synchCap.setIsDirty(false);
						synchCap.setLastResponseTime(currentTime);
						ModPacketHandler.INSTANCE.sendTo(new ResponseSynchingItemDataMessage(itemSlot, itemUUID, stack.serializeNBT()), (EntityPlayerMP)entityIn);
					}
				}
			}
		}
	}


	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) 
	{
		SynchingItemCapabilityProvider provider = new SynchingItemCapabilityProvider();
		
		if (nbt != null)
			provider.deserializeNBT(nbt);
		
		return provider;
	}

 
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List lores, boolean b)
    {
    	super.addInformation(stack, player, lores, b);
    	
        if (Reference.DEBUG)
        {
        	lores.add("SynchingItem debug info:");
        	
        	if (stack.hasTagCompound() && stack.getTagCompound().hasKey(NBT_UUID_KEY))
        		lores.add("  uuid: " + stack.getTagCompound().getString(NBT_UUID_KEY));
        	
        	ISynchingItemCapability synchCap = stack.getCapability(SynchingItemCapabilityProvider.SYNCHING_ITEM_CAPABILITY, null);
        	lores.add("  isDirty: " + synchCap.isDirty());
        	
        	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getDefault());
            lores.add("  lastRequestTime: " + (synchCap.lastRequestTime() == 0 ? "never" : sdf.format(new Date(synchCap.lastRequestTime()))));
            lores.add("  lastResponseTime: " + (synchCap.lastResponseTime() == 0 ? "never" : sdf.format(new Date(synchCap.lastResponseTime()))));
        	       
        }
    }
    
	
}


