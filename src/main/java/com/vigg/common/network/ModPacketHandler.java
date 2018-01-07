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
		INSTANCE.registerMessage(AddWaypointToRecorderMessage.Handler.class, AddWaypointToRecorderMessage.class, 0, Side.SERVER);
	}
}
