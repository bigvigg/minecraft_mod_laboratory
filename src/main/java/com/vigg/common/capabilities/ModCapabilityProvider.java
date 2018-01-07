package com.vigg.common.capabilities;

import com.vigg.common.capabilities.uuid.IUUIDCapability;
import com.vigg.common.capabilities.waypoints.IWaypointMemoryCapability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class ModCapabilityProvider implements ICapabilitySerializable<NBTTagCompound>, ICapabilityProvider
{
	@CapabilityInject(IUUIDCapability.class)
	public static final Capability<IUUIDCapability> UUID_CAPABILITY = null;
	
	@CapabilityInject(IWaypointMemoryCapability.class)
	public static final Capability<IWaypointMemoryCapability> WAYPOINT_CAPABILITY = null;
	
	
	private IUUIDCapability uuidCapability = UUID_CAPABILITY.getDefaultInstance();
	private IWaypointMemoryCapability waypointCapability = WAYPOINT_CAPABILITY.getDefaultInstance();
	
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		return (capability == UUID_CAPABILITY || capability == WAYPOINT_CAPABILITY);
	}
	
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		//return capability == UUID_CAPABILITY ? UUID_CAPABILITY.<T> cast(this.uuidCapability) : null;
		
		if (capability == UUID_CAPABILITY)
			return UUID_CAPABILITY.<T> cast(this.uuidCapability);
		else if (capability == WAYPOINT_CAPABILITY)
			return WAYPOINT_CAPABILITY.<T> cast(this.waypointCapability);
		else
			return null;
	}

	@Override
	public NBTTagCompound serializeNBT()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		NBTBase uuidNbt = UUID_CAPABILITY.getStorage().writeNBT(UUID_CAPABILITY, this.uuidCapability, null);
		NBTBase waypointsNbt = WAYPOINT_CAPABILITY.getStorage().writeNBT(WAYPOINT_CAPABILITY, this.waypointCapability, null);
		nbt.setTag("uuid", uuidNbt);
		nbt.setTag("waypoints", waypointsNbt);
		return nbt;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt)
	{
		NBTBase uuidNbt = nbt.getTag("uuid");
		NBTBase waypointsNbt = nbt.getTag("waypoints");
		
		UUID_CAPABILITY.getStorage().readNBT(UUID_CAPABILITY, this.uuidCapability, null, uuidNbt);
		WAYPOINT_CAPABILITY.getStorage().readNBT(WAYPOINT_CAPABILITY, this.waypointCapability, null, waypointsNbt);
	}
}