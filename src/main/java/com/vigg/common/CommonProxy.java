package com.vigg.common;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {
	
	private ModBlocks modBlocks;
	private ModItems modItems;
	//private ModCapabilities modCapabilities;
	
	public CommonProxy()
	{
		modBlocks = new ModBlocks();
		modItems = new ModItems();
		//modCapabilities = new ModCapabilities();
	}

    public void preInit(FMLPreInitializationEvent e) 
    {
    	modBlocks.preInit(e);;
    	modItems.preInit(e);
    	//modCapabilities.preInit(e);
    	ModPacketHandler.preInit();
    }

    public void init(FMLInitializationEvent e) 
    {
    	modBlocks.init(e);
    	modItems.init(e);
    	//modCapabilities.init(e);
    }

    public void postInit(FMLPostInitializationEvent e) 
    {
    	modBlocks.postInit(e);;
    	modItems.postInit(e);
    	//modCapabilities.postInit(e);
    }
}