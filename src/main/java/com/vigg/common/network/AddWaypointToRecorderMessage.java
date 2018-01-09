package com.vigg.common.network;

import java.util.List;
import java.util.UUID;

import com.vigg.common.Waypoint;
import com.vigg.common.capabilities.waypoints.IWaypointMemoryCapability;
import com.vigg.common.capabilities.waypoints.WaypointMemoryCapabilityProvider;
import com.vigg.common.items.ModItems;
import com.vigg.common.items.SynchingItem;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

// sent from client to server when player right clicks on the ground to add a new waypoint
// sent back again from server to client to confirm that the waypoint was successfully added on the server
public class AddWaypointToRecorderMessage implements IMessage 
{
	private UUID recorderUUID;
	private int index = 0; // only used when sending from server to client
    private Waypoint waypoint;

    // default constructor required
    public AddWaypointToRecorderMessage() 
    {
    }

    public AddWaypointToRecorderMessage(UUID parRecorderUUID, int parIndex, Waypoint parWaypoint) 
    {
    	recorderUUID = parRecorderUUID;
    	index = parIndex;
     	waypoint = parWaypoint;
    }
    
    // this constructor without the index is here for convenience of the client, which doesn't send an index to the server
    public AddWaypointToRecorderMessage(UUID parRecorderUUID, Waypoint parWaypoint) 
    {
    	recorderUUID = parRecorderUUID;
     	waypoint = parWaypoint;
    }

    @Override
    public void fromBytes(ByteBuf buf) 
    {
    	recorderUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
    	index = ByteBufUtils.readVarShort(buf);

    	waypoint = new Waypoint();
		waypoint.deserializeNBT(ByteBufUtils.readTag(buf));
    }

    @Override
    public void toBytes(ByteBuf buf) 
    {	
    	ByteBufUtils.writeUTF8String(buf, recorderUUID.toString());
    	ByteBufUtils.writeVarShort(buf, index);
		ByteBufUtils.writeTag(buf, waypoint.serializeNBT());
    }

    public static class Handler implements IMessageHandler<AddWaypointToRecorderMessage, IMessage> 
    {
        @Override
        public IMessage onMessage(final AddWaypointToRecorderMessage message, MessageContext ctx) 
        {
        	System.out.println("stub received message AddWaypointToRecorderMessage");
        	
        	// sanity check
        	if (message.recorderUUID == null || message.waypoint == null)
        	{
        		System.out.println("stub failed sanity check");
        		return null;
        	}
        	
        	if (ctx.side.isClient())
        	{
        		// client side
        		
        		final Minecraft mc =  Minecraft.getMinecraft();
        		
        		mc.addScheduledTask(
            		new Runnable()
    				{
            			@Override
            			public void run() 
    				    {
            				ItemStack waypointRecorder = mc.player.getHeldItemMainhand();
  				    	  
    				    	if (waypointRecorder != null && waypointRecorder.getItem() == ModItems.getWaypointRecorder() && SynchingItem.getUUID(waypointRecorder).equals(message.recorderUUID))
    				    	{
    				    		IWaypointMemoryCapability waypointCap = waypointRecorder.getCapability(WaypointMemoryCapabilityProvider.WAYPOINT_CAPABILITY, null);
    				    		List<Waypoint> waypoints = waypointCap.getWaypoints();
    				    		
    				    		waypoints.add(message.index, message.waypoint);
    				    	}
    				    }
    				}
                );
    		
        		// no response message
        		return null;
        	}
        	else
        	{
        		// server side
        		
        		System.out.println("stub processing on server");
        		
        		final EntityPlayerMP player = ctx.getServerHandler().playerEntity;
                player.getServerWorld().addScheduledTask(
            		new Runnable()
    				{
            			@Override
            			public void run() 
    				    {
            				ItemStack waypointRecorder = player.getHeldItemMainhand();
    				    	 
    				    	if (waypointRecorder != null && waypointRecorder.getItem() == ModItems.getWaypointRecorder() && SynchingItem.getUUID(waypointRecorder).equals(message.recorderUUID))
    				    	{
    				    		System.out.println("stub made it");
    				    		
    				    		IWaypointMemoryCapability waypointCap = waypointRecorder.getCapability(WaypointMemoryCapabilityProvider.WAYPOINT_CAPABILITY, null);
    				    		List<Waypoint> waypoints = waypointCap.getWaypoints();
    				    		
    				    		int newIndex = waypoints.size();
    				    		waypoints.add(newIndex, message.waypoint);
    				    		
    				    		// now echo the message back to the client (with the new index) so they can add it on the client side
    				    		message.index = newIndex;
    				    		ModPacketHandler.INSTANCE.sendTo(message, player);
    				    		System.out.println("stub sending to client, index " + newIndex);
    				    	}
    				    }
    				}
                );
                
                // no response message
                return null;
        	}
        }
    }
}
