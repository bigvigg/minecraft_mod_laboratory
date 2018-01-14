package com.vigg.common.waypoints;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.vigg.common.ModItems;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

// sent from client to server when player right clicks on the ground to add a new waypoint
public class MessageSwapWaypointsOnRecorder implements IMessage 
{
	private UUID recorderUUID = null;
    private int index1, index2;

    // default constructor required
    public MessageSwapWaypointsOnRecorder() 
    {
    }

    public MessageSwapWaypointsOnRecorder(UUID parRecorderUUID, int parIndex1, int parIndex2) 
    {
    	recorderUUID = parRecorderUUID;
    	index1 = parIndex1;
    	index2 = parIndex2;
    }

    @Override
    public void fromBytes(ByteBuf buf) 
    {
    	recorderUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
    	index1 = ByteBufUtils.readVarInt(buf, 5);
    	index2 = ByteBufUtils.readVarInt(buf, 5);
    }

    @Override
    public void toBytes(ByteBuf buf) 
    {	
    	ByteBufUtils.writeUTF8String(buf, recorderUUID.toString());
		ByteBufUtils.writeVarInt(buf, index1, 5);
		ByteBufUtils.writeVarInt(buf, index2, 5);
    }

    public static class Handler implements IMessageHandler<MessageSwapWaypointsOnRecorder, IMessage> 
    {
        @Override
        public IMessage onMessage(final MessageSwapWaypointsOnRecorder message, MessageContext ctx) 
        {
        	// sanity check
        	if (message.recorderUUID == null || message.index1 < 0 || message.index2 < 0 || ctx.side.isClient())
        		return null;

        	// server side
    		
    		final EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(
        		new Runnable()
				{
        			@Override
        			public void run() 
				    {
        				ItemWaypointRecorder itemWaypointRecorder = ModItems.getWaypointRecorder();
						ItemStack heldItem = player.getHeldItemMainhand();
						if (heldItem != null && heldItem.getItem() == itemWaypointRecorder && message.recorderUUID.equals(itemWaypointRecorder.getUUID(heldItem)))
						{
							List<Waypoint> waypoints = new LinkedList<Waypoint>(Arrays.asList(itemWaypointRecorder.getWaypoints(heldItem)));
							if (message.index1 < waypoints.size() && message.index2 < waypoints.size()) 
							{
								Waypoint wp1 = waypoints.get(message.index1);
								Waypoint wp2 = waypoints.get(message.index2);
								waypoints.set(message.index2, wp1);
								waypoints.set(message.index1, wp2);
								itemWaypointRecorder.setWaypoints(heldItem, waypoints.toArray(new Waypoint[waypoints.size()]));
								player.sendStatusMessage(new TextComponentString("Waypoints order swapped"), true);
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
