package com.vigg.common.waypoints;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.vigg.common.ModItems;
import com.vigg.common.waypoints.IWaypointStorage.WaypointEntry;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;


public class MessageUpdateWaypointOnRecorder implements IMessage 
{
	private UUID recorderUUID = null;
    private WaypointEntry waypointEntry = null;

    // default constructor required
    public MessageUpdateWaypointOnRecorder() 
    {
    }

    public MessageUpdateWaypointOnRecorder(UUID parRecorderUUID, WaypointEntry parWaypointEntry) 
    {
    	recorderUUID = parRecorderUUID;
    	waypointEntry = parWaypointEntry;
    }

    @Override
    public void fromBytes(ByteBuf buf) 
    {
    	recorderUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));

    	waypointEntry = new WaypointEntry();
    	waypointEntry.deserializeNBT(ByteBufUtils.readTag(buf));
    }

    @Override
    public void toBytes(ByteBuf buf) 
    {	
    	ByteBufUtils.writeUTF8String(buf, recorderUUID.toString());
		ByteBufUtils.writeTag(buf, waypointEntry.serializeNBT());
    }

    public static class Handler implements IMessageHandler<MessageUpdateWaypointOnRecorder, IMessage> 
    {
        @Override
        public IMessage onMessage(final MessageUpdateWaypointOnRecorder message, MessageContext ctx) 
        {
        	// sanity check
        	if (message.waypointEntry == null || message.waypointEntry.waypoint == null || message.recorderUUID == null || ctx.side.isClient())
        		return null;

        	// server side
    		
    		final EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(
        		new Runnable()
				{
        			@Override
        			public void run() 
				    {
        				ItemStack heldItem = player.getHeldItemMainhand();
        				ItemWaypointRecorder itemWaypointRecorder = ModItems.getWaypointRecorder();
        				
				    	if (heldItem != null && heldItem.getItem() == itemWaypointRecorder && message.recorderUUID.equals(itemWaypointRecorder.getUUID(heldItem)))
				    	{
				    		Waypoint[] waypoints = itemWaypointRecorder.getWaypoints(heldItem);
				    		if (waypoints.length > message.waypointEntry.index)
				    		{
				    			waypoints[message.waypointEntry.index] = message.waypointEntry.waypoint;
					    		itemWaypointRecorder.setWaypoints(heldItem, waypoints);	
				    		}
				    	}
				    }
				}
            );
            
            // no response message
            return null;
        }
    }
}
