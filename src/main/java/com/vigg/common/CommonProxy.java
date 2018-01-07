package com.vigg.common;

import com.vigg.common.capabilities.ModCapabilities;
import com.vigg.common.items.ModItems;
import com.vigg.common.network.ModPacketHandler;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {
	
	private ModItems modItems;
	private ModCapabilities modCapabilities;
	
	public CommonProxy()
	{
		modItems = new ModItems();
		modCapabilities = new ModCapabilities();
	}

    public void preInit(FMLPreInitializationEvent e) 
    {
    	modItems.preInit(e);
    	modCapabilities.preInit(e);
    	ModPacketHandler.preInit();
    }

    public void init(FMLInitializationEvent e) 
    {
    	modItems.init(e);
    	modCapabilities.init(e);
    }

    public void postInit(FMLPostInitializationEvent e) 
    {
    	modItems.postInit(e);
    	modCapabilities.postInit(e);
    }
}