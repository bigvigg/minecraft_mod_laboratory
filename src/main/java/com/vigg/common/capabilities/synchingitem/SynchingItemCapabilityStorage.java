package com.vigg.common.capabilities.synchingitem;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class SynchingItemCapabilityStorage implements IStorage<ISynchingItemCapability>
{

	@Override
	public NBTBase writeNBT(Capability<ISynchingItemCapability> capability, ISynchingItemCapability instance, EnumFacing side) 
	{
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setBoolean("isDirty", instance.isDirty());
		nbt.setLong("lastRequestTime", instance.lastRequestTime());
		nbt.setLong("lastResponseTime", instance.lastResponseTime());
		return nbt;
	}

	@Override
	public void readNBT(Capability<ISynchingItemCapability> capability, ISynchingItemCapability instance, EnumFacing side, NBTBase nbt) 
	{
		NBTTagCompound nbtCompound = (NBTTagCompound)nbt;
		instance.setIsDirty(nbtCompound.getBoolean("isDirty"));;
		instance.setLastRequestTime(nbtCompound.getLong("lastRequestTime"));
		instance.setLastResponseTime(nbtCompound.getLong("lastResponseTime"));
	}
	
}
