package com.vigg.common;

import com.vigg.common.waypoints.BlockWaypoint;
import com.vigg.common.waypoints.WaypointRecorder;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ModBlocks 
{
	private static BlockWaypoint blockWaypoint;
	public static BlockWaypoint getBlockWaypoint()
	{
		return blockWaypoint;
	}
	
	
	public ModBlocks()
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
    public void registerBlocks(RegistryEvent.Register<Block> event) 
    {
    	blockWaypoint = new BlockWaypoint();
    	
        event.getRegistry().registerAll(blockWaypoint/*, block2, ...*/);
    }
    
    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) 
    {
    	ItemBlock itemBlockWaypoint = new ItemBlock(blockWaypoint);
    	itemBlockWaypoint.setRegistryName(blockWaypoint.getRegistryName());
    	
        event.getRegistry().registerAll(itemBlockWaypoint /*, ...more. */);
    }
    
}