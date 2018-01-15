package com.vigg.common;

import com.vigg.common.waypoints.MessageAddWaypointToRecorder;
import com.vigg.common.waypoints.MessageRemoveWaypointFromRecorder;
import com.vigg.common.waypoints.MessageSwapWaypointsOnRecorder;
import com.vigg.common.waypoints.MessageUpdateWaypointOnRecorder;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class ModPacketHandler 
{
	public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Reference.MOD_ID);
	
	public static void preInit()
	{
		int nextChannelID = -1;
		
		//server
		INSTANCE.registerMessage(MessageAddWaypointToRecorder.Handler.class, MessageAddWaypointToRecorder.class, nextChannelID++, Side.SERVER);
		INSTANCE.registerMessage(MessageRemoveWaypointFromRecorder.Handler.class, MessageRemoveWaypointFromRecorder.class, nextChannelID++, Side.SERVER);
		INSTANCE.registerMessage(MessageSwapWaypointsOnRecorder.Handler.class, MessageSwapWaypointsOnRecorder.class, nextChannelID++, Side.SERVER);
		INSTANCE.registerMessage(MessageUpdateWaypointOnRecorder.Handler.class, MessageUpdateWaypointOnRecorder.class, nextChannelID++, Side.SERVER);
		
		//client
		//INSTANCE.registerMessage(AddWaypointToRecorderMessage.Handler.class, AddWaypointToRecorderMessage.class, nextChannelID++, Side.CLIENT);
		//INSTANCE.registerMessage(ResponseSynchingItemDataMessage.Handler.class, ResponseSynchingItemDataMessage.class, nextChannelID++, Side.CLIENT);
	}
}
