package com.vigg.common.network;

import java.util.UUID;

import com.vigg.common.capabilities.synchingitem.ISynchingItemCapability;
import com.vigg.common.capabilities.synchingitem.SynchingItemCapabilityProvider;
import com.vigg.common.items.SynchingItem;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

// NOTE: this message only goes one way, from server to client
public class ResponseSynchingItemDataMessage implements IMessage 
{
	private int itemSlot = -1;
	private UUID itemUUID = null;
	private NBTTagCompound nbt = null;

    // default constructor required
    public ResponseSynchingItemDataMessage() 
    {
    }

    public ResponseSynchingItemDataMessage(int parItemSlot, UUID parItemUUID, NBTTagCompound parNbt) 
    {
    	itemSlot = parItemSlot;
    	itemUUID = parItemUUID;
    	nbt = parNbt;
    }

    @Override
    public void fromBytes(ByteBuf buf) 
    {
    	itemSlot = ByteBufUtils.readVarInt(buf, 2);
    	itemUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
    	nbt = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) 
    {	
		ByteBufUtils.writeVarInt(buf, itemSlot, 2);
		ByteBufUtils.writeUTF8String(buf, itemUUID.toString());
		ByteBufUtils.writeTag(buf,  nbt);
    }

    public static class Handler implements IMessageHandler<ResponseSynchingItemDataMessage, IMessage> 
    {
        @Override
        public IMessage onMessage(final ResponseSynchingItemDataMessage message, MessageContext ctx) 
        {
        	// sanity check
        	if ((!ctx.side.isClient()) || message.itemUUID == null || message.itemSlot < 0)
        		return null;

        	final Minecraft mc = Minecraft.getMinecraft();
			
        	mc.addScheduledTask(
        		new Runnable()
				{
        			@Override
        			public void run() 
				    {
        				ItemStack itemToUpdate = null;
        				
        				// try to locate the specified ItemStack in the player's inventory
        				NonNullList[] inventoriesToSearch = { mc.player.inventory.mainInventory, mc.player.inventory.offHandInventory, mc.player.inventory.armorInventory };
        				for (int i = 0; i < inventoriesToSearch.length; i++)
        				{
        					if (inventoriesToSearch[i] != null && inventoriesToSearch[i].size() > message.itemSlot)
        					{
	        					ItemStack itemToCheck = (ItemStack)inventoriesToSearch[i].get(message.itemSlot);
	        					
	            				if (itemToCheck != null && SynchingItem.getUUID(itemToCheck) == message.itemUUID && itemToCheck.hasCapability(SynchingItemCapabilityProvider.SYNCHING_ITEM_CAPABILITY, null))
	            				{
	            					itemToUpdate = itemToCheck;
	            					break;
	            				}
        					}
        				}
        				
        				if (itemToUpdate != null)
        				{
        					ISynchingItemCapability synchCap = itemToUpdate.getCapability(SynchingItemCapabilityProvider.SYNCHING_ITEM_CAPABILITY, null);
        					
        					// preserve lastRequestTime before it gets overwritten by deserializeNBT below
        					long lastRequestTime = synchCap.lastRequestTime();
        					
        					// calling deserializeNBT overwrites EVERYTHING from the server, so call it first and THEN make our synchCap changes below
        					itemToUpdate.deserializeNBT(message.nbt);
        					
        					// NOW we can update capabilities on the client without them being overwritten
        					synchCap.setIsDirty(false);
        					synchCap.setLastRequestTime(lastRequestTime);
        					synchCap.setLastResponseTime(System.currentTimeMillis());
        					
        					System.out.println("stub - client processed packet");
        				}
				    }
				}
            );
            
            // no response message
            return null;
        }
    }
}
