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
        				BlockPos nextLineOfSightPos = new BlockPos(message.x, message.y, message.z);
        				
        				// try a couple different spots, and put the waypoint on the first one that meets the conditions.
        				// if neither spot matches the conditions, then no waypoint is placed, and nothing happens as a result of the player's click.
        				BlockPos[] possibleWaypointPositions = new BlockPos[] {
        						nextLineOfSightPos,					// first try to place at the actual position clicked (example: player clicks on tall grass)
        						nextLineOfSightPos.add(0, 1, 0)		// then try to place above the clicked position (example: player clicks on the ground)
        				};
        				for (BlockPos possiblePos : possibleWaypointPositions)
        				{
        					// don't place waypoints very far above the player, because that's probably an accident and doesn't make sense
        					if (possiblePos != null && possiblePos.getY() <= (player.getPosition().getY() + 1) && possiblePos.getY() > 0)
        					{
        						// don't place waypoints inside of solid blocks
        						IBlockState possiblePosState = player.world.getBlockState(possiblePos);
        						if (possiblePosState != null && possiblePosState.getMaterial() != null && !possiblePosState.getMaterial().isSolid())
        						{
        							// only place waypoints on top of solid blocks
        							IBlockState blockBelow = player.world.getBlockState(possiblePos.add(0, -1, 0));
        							if (blockBelow != null && blockBelow.getMaterial() != null && blockBelow.getMaterial().isSolid())
        							{
        								ItemWaypointRecorder itemWaypointRecorder = ModItems.getWaypointRecorder();
        								ItemStack heldItem = player.getHeldItemMainhand();
        								if (heldItem != null && heldItem.getItem() == itemWaypointRecorder && message.recorderUUID.equals(itemWaypointRecorder.getUUID(heldItem)))
        								{
        									// make sure we aren't trying to add two waypoints to the same spot.        									
        									// if we are, then assume they were *trying* to remove the existing waypoint.
        									
        									WaypointEntry existingWaypoint = itemWaypointRecorder.getWaypoint(heldItem, possiblePos.getX(), possiblePos.getY(), possiblePos.getZ());
        									if (existingWaypoint == null)
        									{
        										// add the new waypoint
        										Waypoint newWaypoint = new Waypoint(possiblePos.getX(), possiblePos.getY(), possiblePos.getZ());
            									itemWaypointRecorder.addWaypoint(heldItem, newWaypoint);
        									}
        									else
        									{
        										// remove the existing waypoint
        										itemWaypointRecorder.removeWaypoint(heldItem, existingWaypoint.index);
        									}
        									
        								}
        							}
        						}
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
