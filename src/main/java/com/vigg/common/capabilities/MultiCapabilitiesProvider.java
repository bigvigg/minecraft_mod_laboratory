package com.vigg.common.capabilities;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class MultiCapabilitiesProvider implements ICapabilitySerializable<NBTTagCompound>, ICapabilityProvider
{

	private List<ICapabilityProvider> providers;
	public List<ICapabilityProvider> getProviders()
	{
		return providers;
	}
	
	public MultiCapabilitiesProvider()
	{
		providers = new ArrayList<ICapabilityProvider>();
	}
	
	public void add(ICapabilityProvider provider)
	{
		providers.add(provider);
	}
	
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		for (int i = 0; i < providers.size(); i++)
		{
			if (providers.get(i).hasCapability(capability, facing))
				return true;
		}
		
		return false;
	}
	
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		for (int i = 0; i < providers.size(); i++)
		{
			ICapabilityProvider subProvider = providers.get(i);
			if (subProvider.hasCapability(capability, facing))
				return subProvider.getCapability(capability, facing);
		}

		return null;
	}
	
	@Override
	public NBTTagCompound serializeNBT() 
	{
		NBTTagCompound nbt = new NBTTagCompound();
		
		for (int i = 0; i < providers.size(); i++)
		{
			ICapabilityProvider subProvider = providers.get(i);

			if (subProvider instanceof ICapabilitySerializable)
			{
				NBTBase nbtSub = ((ICapabilitySerializable)subProvider).serializeNBT();
				nbt.setTag(Integer.toString(i), nbtSub);
			}
		}
		
		//System.out.println("stub MultiProvider.serializeNBT() returning " + nbt);
		return nbt;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) 
	{
		//System.out.println("stub MultiProvider.deserializeNBT() using " + nbt);
		
		// STUB: not sure why it sometimes has the "Parent" tag nested into the structure, and sometimes it doesn't.
		// If I can ever figure it out and get it consistent, I can remove this check and save a few cpu cycles
		if (nbt.hasKey("Parent"))
 			nbt = nbt.getCompoundTag("Parent");
		
		for (int i = 0; i < providers.size(); i++)
		{
			ICapabilityProvider subProvider = providers.get(i);

			if (subProvider instanceof ICapabilitySerializable)
			{
				NBTBase nbtSub = nbt.getTag(Integer.toString(i));
				if (nbtSub != null)
					((ICapabilitySerializable)subProvider).deserializeNBT(nbtSub);
			}
		}
	}
	
}