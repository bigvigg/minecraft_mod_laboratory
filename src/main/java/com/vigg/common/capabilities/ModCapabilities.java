package com.vigg.common.capabilities;

import com.vigg.common.Reference;
import com.vigg.common.capabilities.synchingitem.ISynchingItemCapability;
import com.vigg.common.capabilities.synchingitem.SynchingItemCapability;
import com.vigg.common.capabilities.synchingitem.SynchingItemCapabilityStorage;
import com.vigg.common.capabilities.waypoints.IWaypointMemoryCapability;
import com.vigg.common.capabilities.waypoints.WaypointMemoryCapability;
import com.vigg.common.capabilities.waypoints.WaypointMemoryCapabilityProvider;
import com.vigg.common.capabilities.waypoints.WaypointMemoryCapabilityStorage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ModCapabilities 
{
	public static final ResourceLocation WAYPOINT_CAPABILITY = new ResourceLocation(Reference.MOD_ID, "WaypointMemoryCapability");
	public static final ResourceLocation SYNCHING_ITEM_CAPABILITY = new ResourceLocation(Reference.MOD_ID, "SynchingItemCapability");
	
	public ModCapabilities()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public void preInit(FMLPreInitializationEvent e) 
	{
		CapabilityManager.INSTANCE.register(IWaypointMemoryCapability.class, new WaypointMemoryCapabilityStorage(), WaypointMemoryCapability.class);
		CapabilityManager.INSTANCE.register(ISynchingItemCapability.class, new SynchingItemCapabilityStorage(), SynchingItemCapability.class);
	}

    public void init(FMLInitializationEvent e)
    {
    }

    public void postInit(FMLPostInitializationEvent e)
    {
    }
    
	@SubscribeEvent
	public void attachCapability(AttachCapabilitiesEvent.Entity event)
	{
		if (!(event.getEntity() instanceof EntityPlayer))
		{
			event.addCapability(WAYPOINT_CAPABILITY, new WaypointMemoryCapabilityProvider());
		}
	}
}