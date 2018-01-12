package com.vigg.common;

import com.vigg.common.waypoints.ItemWaypointRecorder;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ModItems 
{
	private static ItemWaypointRecorder waypointRecorder;
	
	public ModItems()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public void preInit(FMLPreInitializationEvent e) 
	{
	}

    public void init(FMLInitializationEvent e)
    {
    }

    public void postInit(FMLPostInitializationEvent e)
    {
    }
    
    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) 
    {
    	waypointRecorder = new ItemWaypointRecorder();
    	
        event.getRegistry().registerAll(waypointRecorder /*, ...more items. */);
    }
    
    public static ItemWaypointRecorder getWaypointRecorder()
    {
    	return waypointRecorder;
    }
}