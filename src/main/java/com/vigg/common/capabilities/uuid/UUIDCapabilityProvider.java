package com.vigg.common.capabilities.uuid;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class UUIDCapabilityProvider implements ICapabilitySerializable<NBTBase>, ICapabilityProvider
{
	@CapabilityInject(IUUIDCapability.class)
	public static final Capability<IUUIDCapability> UUID_CAPABILITY = null;
	
	private IUUIDCapability instance = UUID_CAPABILITY.getDefaultInstance();
	
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		return capability == UUID_CAPABILITY;
	}
	
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		return capability == UUID_CAPABILITY ? UUID_CAPABILITY.<T> cast(this.instance) : null;
	}

	@Override
	public NBTBase serializeNBT()
	{
		return UUID_CAPABILITY.getStorage().writeNBT(UUID_CAPABILITY, this.instance, null);
	}

	@Override
	public void deserializeNBT(NBTBase nbt)
	{
		UUID_CAPABILITY.getStorage().readNBT(UUID_CAPABILITY, this.instance, null, nbt);
	}
}