package com.vigg.common.capabilities.uuid;

import java.util.List;
import java.util.UUID;

import com.vigg.common.Waypoint;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class UUIDCapabilityStorage implements IStorage<IUUIDCapability>
{

	@Override
	public NBTBase writeNBT(Capability<IUUIDCapability> capability, IUUIDCapability instance, EnumFacing side) 
	{
		NBTTagCompound nbt = new NBTTagCompound();

		nbt.setString("uuid", instance.getUUID().toString());
		
		return nbt;
	}

	@Override
	public void readNBT(Capability<IUUIDCapability> capability, IUUIDCapability instance, EnumFacing side, NBTBase nbt) 
	{
		NBTTagCompound nbtCompound = (NBTTagCompound)nbt;
		
		instance.setUUID(UUID.fromString(nbtCompound.getString("uuid")));
	}
	
}
