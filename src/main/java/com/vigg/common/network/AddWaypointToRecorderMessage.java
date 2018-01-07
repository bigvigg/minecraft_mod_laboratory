package com.vigg.common.network;

import com.vigg.common.Waypoint;
import com.vigg.common.items.ModItems;
import com.vigg.common.items.WaypointRecorder;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

// NOTE: this message only goes one way, from client to server.
// Updates of ItemStack NBT data from servers to clients happens automatically.
public class AddWaypointToRecorderMessage implements IMessage 
{
    private Waypoint waypoint;

    // default constructor required
    public AddWaypointToRecorderMessage() 
    {
    }

    public AddWaypointToRecorderMessage(Waypoint parWaypoint) 
    {
     	waypoint = parWaypoint;
    }

    @Override
    public void fromBytes(ByteBuf buf) 
    {
    	waypoint = new Waypoint();
		waypoint.deserializeNBT(ByteBufUtils.readTag(buf));
    }

    @Override
    public void toBytes(ByteBuf buf) 
    {	
		ByteBufUtils.writeTag(buf, waypoint.serializeNBT());
    }

    public static class Handler implements IMessageHandler<AddWaypointToRecorderMessage, IMessage> 
    {
        @Override
        public IMessage onMessage(final AddWaypointToRecorderMessage message, MessageContext ctx) 
        {
        	if (ctx.side.isClient())
        		return null;
        	
        	
        	//ctx.side.isClient() ? Minecraft.getMinecraft().thePlayer : ctx.getServerHandler().playerEntity        	
            //final EntityPlayerMP thePlayer = (EntityPlayerMP) BlockSmith.proxy.getPlayerEntityFromContext(ctx);        	
        	
        	
			final EntityPlayerMP thePlayer = ctx.getServerHandler().playerEntity;
			
            thePlayer.getServerWorld().addScheduledTask(
        		new Runnable()
				{
        			@Override
        			public void run() 
				    {
        				ItemStack waypointRecorder = thePlayer.getHeldItemMainhand();
				    	  
				    	if (waypointRecorder != null && waypointRecorder.getItem() == ModItems.getWaypointRecorder())
				    	{
							NBTTagCompound nbt = waypointRecorder.hasTagCompound() ? waypointRecorder.getTagCompound() : new NBTTagCompound();

							NBTTagList nbtWaypoints = null;
							if (nbt.hasKey(WaypointRecorder.NBT_KEY))
								nbtWaypoints = (NBTTagList)nbt.getTag(WaypointRecorder.NBT_KEY);
							else
								nbt.setTag(WaypointRecorder.NBT_KEY, new NBTTagList());
							nbtWaypoints.appendTag(message.waypoint.serializeNBT());
							waypointRecorder.setTagCompound(nbt);
							
							thePlayer.sendMessage(new TextComponentString("STUB added " + message.waypoint.getLabel()));
				    	}
				    }
				}
            );
            
            // no response message
            return null;
        }
    }
}
