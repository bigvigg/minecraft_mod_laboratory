package com.vigg.common.capabilities;

import com.vigg.common.Reference;
import com.vigg.common.capabilities.uuid.IUUIDCapability;
import com.vigg.common.capabilities.uuid.UUIDCapability;
import com.vigg.common.capabilities.uuid.UUIDCapabilityStorage;
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
	public static final ResourceLocation UUID_CAPABILITY = new ResourceLocation(Reference.MOD_ID, "UUIDCapability");

	public ModCapabilities()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public void preInit(FMLPreInitializationEvent e) 
	{
		CapabilityManager.INSTANCE.register(IWaypointMemoryCapability.class, new WaypointMemoryCapabilityStorage(), WaypointMemoryCapability.class);
		CapabilityManager.INSTANCE.register(IUUIDCapability.class, new UUIDCapabilityStorage(), UUIDCapability.class);
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