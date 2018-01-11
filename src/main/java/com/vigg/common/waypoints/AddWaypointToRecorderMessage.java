package com.vigg.common.waypoints;

import java.util.List;

import com.vigg.common.ModItems;
import com.vigg.common.Waypoint;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

// sent from client to server when player right clicks on the ground to add a new waypoint
public class AddWaypointToRecorderMessage implements IMessage 
{
	private int index = 0; // only used when sending from server to client
    private Waypoint waypoint;

    // default constructor required
    public AddWaypointToRecorderMessage() 
    {
    }

    public AddWaypointToRecorderMessage(int parIndex, Waypoint parWaypoint) 
    {
    	index = parIndex;
     	waypoint = parWaypoint;
    }
    
    // this constructor without the index is here for convenience of the client, which doesn't send an index to the server
    public AddWaypointToRecorderMessage(Waypoint parWaypoint) 
    {
     	waypoint = parWaypoint;
    }

    @Override
    public void fromBytes(ByteBuf buf) 
    {
    	index = ByteBufUtils.readVarShort(buf);

    	waypoint = new Waypoint();
		waypoint.deserializeNBT(ByteBufUtils.readTag(buf));
    }

    @Override
    public void toBytes(ByteBuf buf) 
    {	
    	ByteBufUtils.writeVarShort(buf, index);
		ByteBufUtils.writeTag(buf, waypoint.serializeNBT());
    }

    public static class Handler implements IMessageHandler<AddWaypointToRecorderMessage, IMessage> 
    {
        @Override
        public IMessage onMessage(final AddWaypointToRecorderMessage message, MessageContext ctx) 
        {
        	// sanity check
        	if (message.waypoint == null || ctx.side.isClient())
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
				    	 
				    	if (heldItem != null && heldItem.getItem() == ModItems.getWaypointRecorder())
				    	{
				    		ModItems.getWaypointRecorder().addWaypoint(heldItem, message.waypoint);
				    	}
				    }
				}
            );
            
            // no response message
            return null;
        }
    }
}
