
 package com.vigg.common.capabilities;
 
 import com.vigg.common.capabilities.synchingitem.ISynchingItemCapability;
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
 	@CapabilityInject(ISynchingItemCapability.class)
 	public static final Capability<ISynchingItemCapability> SYNCHING_ITEM_CAPABILITY = null;
 	
 	@CapabilityInject(IWaypointMemoryCapability.class)
 	public static final Capability<IWaypointMemoryCapability> WAYPOINT_CAPABILITY = null;
 	
 	
 	private ISynchingItemCapability synchingItemCapability = SYNCHING_ITEM_CAPABILITY.getDefaultInstance();
 	private IWaypointMemoryCapability waypointCapability = WAYPOINT_CAPABILITY.getDefaultInstance();
 	
 	
 	@Override
 	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
 	{
 		return (capability == SYNCHING_ITEM_CAPABILITY || capability == WAYPOINT_CAPABILITY);
 	}
 	
 	@Override
 	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
 	{
 		//return capability == UUID_CAPABILITY ? UUID_CAPABILITY.<T> cast(this.uuidCapability) : null;
 		
 		if (capability == SYNCHING_ITEM_CAPABILITY)
 			return SYNCHING_ITEM_CAPABILITY.<T> cast(this.synchingItemCapability);
 		else if (capability == WAYPOINT_CAPABILITY)
 			return WAYPOINT_CAPABILITY.<T> cast(this.waypointCapability);
 		else
 			return null;
 	}
 
 	@Override
 	public NBTTagCompound serializeNBT()
 	{
 		NBTTagCompound nbt = new NBTTagCompound();
 		NBTBase uuidNbt = SYNCHING_ITEM_CAPABILITY.getStorage().writeNBT(SYNCHING_ITEM_CAPABILITY, this.synchingItemCapability, null);
 		NBTBase waypointsNbt = WAYPOINT_CAPABILITY.getStorage().writeNBT(WAYPOINT_CAPABILITY, this.waypointCapability, null);
 		nbt.setTag("synchingevent", uuidNbt);
 		nbt.setTag("waypoints", waypointsNbt);
 		
 		//System.out.println("stub ModCapabilityProvider serializeNBT() returning " + nbt);
 		
 		return nbt;
 	}
 
 	@Override
 	public void deserializeNBT(NBTTagCompound nbt)
 	{
 		//System.out.println("stub ModCapabilityProvider deserializeNBT() getting " + nbt);
 		
 		if (nbt.hasKey("Parent"))
 			nbt = nbt.getCompoundTag("Parent");
 		
 		NBTBase uuidNbt = nbt.getTag("synchingevent");
 		NBTBase waypointsNbt = nbt.getTag("waypoints");
 		
 		SYNCHING_ITEM_CAPABILITY.getStorage().readNBT(SYNCHING_ITEM_CAPABILITY, this.synchingItemCapability, null, uuidNbt);
 		WAYPOINT_CAPABILITY.getStorage().readNBT(WAYPOINT_CAPABILITY, this.waypointCapability, null, waypointsNbt);
 	}
 } 
