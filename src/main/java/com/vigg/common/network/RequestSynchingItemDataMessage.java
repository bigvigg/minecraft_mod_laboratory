package com.vigg.common.network;

import java.util.UUID;

import com.vigg.common.capabilities.synchingitem.ISynchingItemCapability;
import com.vigg.common.capabilities.synchingitem.SynchingItemCapabilityProvider;
import com.vigg.common.items.SynchingItem;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

// NOTE: this message only goes one way, from client to server.
public class RequestSynchingItemDataMessage implements IMessage 
{
	private int itemSlot = -1;
	private UUID itemUUID = null;

    // default constructor required
    public RequestSynchingItemDataMessage() 
    {
    }

    public RequestSynchingItemDataMessage(int parItemSlot, UUID parItemUUID) 
    {
    	itemSlot = parItemSlot;
    	itemUUID = parItemUUID;
    }

    @Override
    public void fromBytes(ByteBuf buf) 
    {
    	itemSlot = ByteBufUtils.readVarInt(buf, 2);
    	itemUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
    }

    @Override
    public void toBytes(ByteBuf buf) 
    {	
		ByteBufUtils.writeVarInt(buf, itemSlot, 2);
		ByteBufUtils.writeUTF8String(buf, itemUUID.toString());
    }

    public static class Handler implements IMessageHandler<RequestSynchingItemDataMessage, IMessage> 
    {
        @Override
        public IMessage onMessage(final RequestSynchingItemDataMessage message, MessageContext ctx) 
        {
        	// sanity check
        	if (ctx.side.isClient() || message.itemUUID == null || message.itemSlot < 0)
        		return null;

			final EntityPlayerMP player = ctx.getServerHandler().playerEntity;
			
            player.getServerWorld().addScheduledTask(
        		new Runnable()
				{
        			@Override
        			public void run() 
				    {
        				ItemStack requestedItem = null;
        				
        				// try to locate the specified ItemStack in the player's inventory
        				NonNullList[] inventoriesToSearch = { player.inventory.mainInventory, player.inventory.offHandInventory, player.inventory.armorInventory };
        				for (int i = 0; i < inventoriesToSearch.length; i++)
        				{
        					if (inventoriesToSearch[i] != null && inventoriesToSearch[i].size() > message.itemSlot)
        					{
	        					ItemStack itemToCheck = (ItemStack)inventoriesToSearch[i].get(message.itemSlot);
	        					
	            				if (itemToCheck != null && SynchingItem.getUUID(itemToCheck) == message.itemUUID && itemToCheck.hasCapability(SynchingItemCapabilityProvider.SYNCHING_ITEM_CAPABILITY, null))
	            				{
	            					requestedItem = itemToCheck;
	            					break;
	            				}
        					}
        				}
        				
        				if (requestedItem != null)
        				{
        					// we found the item - update its synchCap fields and send its data back to the client
        					ISynchingItemCapability synchCap = requestedItem.getCapability(SynchingItemCapabilityProvider.SYNCHING_ITEM_CAPABILITY, null);
        					long currentTime = System.currentTimeMillis();
        					synchCap.setIsDirty(false);
        					synchCap.setLastRequestTime(currentTime);
        					synchCap.setLastResponseTime(currentTime);
        					
        					ModPacketHandler.INSTANCE.sendTo(new ResponseSynchingItemDataMessage(message.itemSlot, message.itemUUID, requestedItem.serializeNBT()), player);
        				}
				    }
				}
            );
            
            // no response message
            return null;
        }
    }
}
