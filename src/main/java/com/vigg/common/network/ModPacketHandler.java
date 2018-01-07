package com.vigg.common.network;

import com.vigg.common.Reference;

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
		INSTANCE.registerMessage(AddWaypointToRecorderMessage.Handler.class, AddWaypointToRecorderMessage.class, nextChannelID++, Side.SERVER);
		INSTANCE.registerMessage(RequestSynchingItemDataMessage.Handler.class, RequestSynchingItemDataMessage.class, nextChannelID++, Side.SERVER);
		
		//client
		INSTANCE.registerMessage(ResponseSynchingItemDataMessage.Handler.class, ResponseSynchingItemDataMessage.class, nextChannelID++, Side.CLIENT);
	}
}
