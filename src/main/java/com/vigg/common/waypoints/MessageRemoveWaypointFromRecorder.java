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

// sent from client to server when player right clicks on an existing waypoint
public class MessageRemoveWaypointFromRecorder implements IMessage 
{
	private UUID recorderUUID = null;
    private WaypointEntry waypointEntry = null;

    // default constructor required
    public MessageRemoveWaypointFromRecorder() 
    {
    }

    public MessageRemoveWaypointFromRecorder(UUID parRecorderUUID, WaypointEntry parWaypointEntry) 
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

    public static class Handler implements IMessageHandler<MessageRemoveWaypointFromRecorder, IMessage> 
    {
        @Override
        public IMessage onMessage(final MessageRemoveWaypointFromRecorder message, MessageContext ctx) 
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
				    		//List<Waypoint> waypoints = Arrays.asList(itemWaypointRecorder.getWaypoints(heldItem));
				    		List<Waypoint> waypoints = new LinkedList<Waypoint>(Arrays.asList(itemWaypointRecorder.getWaypoints(heldItem)));
				    		
				    		if (waypoints.size() > message.waypointEntry.index && waypoints.get(message.waypointEntry.index).equals(message.waypointEntry.waypoint))
				    		{
				    			waypoints.remove(message.waypointEntry.index);
				    			itemWaypointRecorder.setWaypoints(heldItem, waypoints.toArray(new Waypoint[waypoints.size()]));
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
