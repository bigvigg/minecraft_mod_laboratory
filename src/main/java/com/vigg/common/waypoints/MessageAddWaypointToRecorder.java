package com.vigg.common.waypoints;

import java.util.UUID;

import com.vigg.common.ModItems;
import com.vigg.common.waypoints.IWaypointStorage.WaypointEntry;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

// sent from client to server when player right clicks on the ground to add a new waypoint
public class MessageAddWaypointToRecorder implements IMessage 
{
	private UUID recorderUUID = null;
    private int x, y, z;

    // default constructor required
    public MessageAddWaypointToRecorder() 
    {
    }

    public MessageAddWaypointToRecorder(UUID parRecorderUUID, int parX, int parY, int parZ) 
    {
    	recorderUUID = parRecorderUUID;
    	x = parX;
    	y = parY;
    	z = parZ;
    }

    @Override
    public void fromBytes(ByteBuf buf) 
    {
    	recorderUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
    	x = ByteBufUtils.readVarInt(buf, 5);
    	y = ByteBufUtils.readVarInt(buf, 5);
    	z = ByteBufUtils.readVarInt(buf, 5);
    }

    @Override
    public void toBytes(ByteBuf buf) 
    {	
    	ByteBufUtils.writeUTF8String(buf, recorderUUID.toString());
		ByteBufUtils.writeVarInt(buf, x, 5);
		ByteBufUtils.writeVarInt(buf, y, 5);
		ByteBufUtils.writeVarInt(buf, z, 5);
    }

    public static class Handler implements IMessageHandler<MessageAddWaypointToRecorder, IMessage> 
    {
        @Override
        public IMessage onMessage(final MessageAddWaypointToRecorder message, MessageContext ctx) 
        {
        	// sanity check
        	if (message.recorderUUID == null || ctx.side.isClient())
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
							// make sure we aren't trying to add two waypoints to the same spot.        									
							
							WaypointEntry existingWaypoint = itemWaypointRecorder.getWaypoint(heldItem, message.x, message.y, message.z);
							if (existingWaypoint == null)
							{
								// add the new waypoint
								Waypoint newWaypoint = new Waypoint(message.x, message.y, message.z);
								itemWaypointRecorder.addWaypoint(heldItem, newWaypoint);
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
