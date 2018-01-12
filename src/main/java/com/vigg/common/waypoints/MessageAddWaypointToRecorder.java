package com.vigg.common.waypoints;

import java.util.UUID;

import com.vigg.common.ModItems;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

// sent from client to server when player right clicks on the ground to add a new waypoint
public class MessageAddWaypointToRecorder implements IMessage 
{
	private UUID recorderUUID = null;
    private Waypoint waypoint = null;

    // default constructor required
    public MessageAddWaypointToRecorder() 
    {
    }

    public MessageAddWaypointToRecorder(UUID parRecorderUUID, Waypoint parWaypoint) 
    {
    	recorderUUID = parRecorderUUID;
     	waypoint = parWaypoint;
    }

    @Override
    public void fromBytes(ByteBuf buf) 
    {
    	recorderUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));

    	waypoint = new Waypoint();
		waypoint.deserializeNBT(ByteBufUtils.readTag(buf));
    }

    @Override
    public void toBytes(ByteBuf buf) 
    {	
    	ByteBufUtils.writeUTF8String(buf, recorderUUID.toString());
		ByteBufUtils.writeTag(buf, waypoint.serializeNBT());
    }

    public static class Handler implements IMessageHandler<MessageAddWaypointToRecorder, IMessage> 
    {
        @Override
        public IMessage onMessage(final MessageAddWaypointToRecorder message, MessageContext ctx) 
        {
        	// sanity check
        	if (message.waypoint == null || message.recorderUUID == null || ctx.side.isClient())
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
				    		itemWaypointRecorder.addWaypoint(heldItem, message.waypoint);
				    	}
				    }
				}
            );
            
            // no response message
            return null;
        }
    }
}
