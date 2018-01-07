package com.vigg.common.capabilities.synchingitem;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class SynchingItemCapabilityProvider implements ICapabilitySerializable<NBTBase>, ICapabilityProvider
{
	@CapabilityInject(ISynchingItemCapability.class)
	public static final Capability<ISynchingItemCapability> SYNCHING_ITEM_CAPABILITY = null;
	
	private ISynchingItemCapability instance = SYNCHING_ITEM_CAPABILITY.getDefaultInstance();
	
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		return capability == SYNCHING_ITEM_CAPABILITY;
	}
	
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		return capability == SYNCHING_ITEM_CAPABILITY ? SYNCHING_ITEM_CAPABILITY.<T> cast(this.instance) : null;
	}

	@Override
	public NBTBase serializeNBT()
	{
		return SYNCHING_ITEM_CAPABILITY.getStorage().writeNBT(SYNCHING_ITEM_CAPABILITY, this.instance, null);
	}

	@Override
	public void deserializeNBT(NBTBase nbt)
	{
		SYNCHING_ITEM_CAPABILITY.getStorage().readNBT(SYNCHING_ITEM_CAPABILITY, this.instance, null, nbt);
	}
}