package com.vigg.common.capabilities.waypoints;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class WaypointMemoryCapabilityProvider implements ICapabilitySerializable<NBTBase>, ICapabilityProvider
{
	@CapabilityInject(IWaypointMemoryCapability.class)
	public static final Capability<IWaypointMemoryCapability> WAYPOINT_CAPABILITY = null;
	
	private IWaypointMemoryCapability instance = WAYPOINT_CAPABILITY.getDefaultInstance();
	
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		return capability == WAYPOINT_CAPABILITY;
	}
	
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		return capability == WAYPOINT_CAPABILITY ? WAYPOINT_CAPABILITY.<T> cast(this.instance) : null;
	}

	@Override
	public NBTBase serializeNBT()
	{
		return WAYPOINT_CAPABILITY.getStorage().writeNBT(WAYPOINT_CAPABILITY, this.instance, null);
	}

	@Override
	public void deserializeNBT(NBTBase nbt)
	{
		WAYPOINT_CAPABILITY.getStorage().readNBT(WAYPOINT_CAPABILITY, this.instance, null, nbt);
	}
}