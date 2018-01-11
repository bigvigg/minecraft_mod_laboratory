package com.vigg.common;

import com.vigg.common.waypoints.TileEntityWaypoint;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModTileEntities
{	
	public ModTileEntities()
	{
		//MinecraftForge.EVENT_BUS.register(this);
	}
	
	public void preInit(FMLPreInitializationEvent e) 
	{
		GameRegistry.registerTileEntity(TileEntityWaypoint.class, "com.vigg.TileEntityWaypoint");
	}

    public void init(FMLInitializationEvent e)
    {
    }

    public void postInit(FMLPostInitializationEvent e)
    {
    }
    
    
    
}