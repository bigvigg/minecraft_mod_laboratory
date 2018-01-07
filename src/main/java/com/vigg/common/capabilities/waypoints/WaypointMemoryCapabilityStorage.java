package com.vigg.common.capabilities.waypoints;

import java.util.List;

import com.vigg.common.Waypoint;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class WaypointMemoryCapabilityStorage implements IStorage<IWaypointMemoryCapability>
{

	@Override
	public NBTBase writeNBT(Capability<IWaypointMemoryCapability> capability, IWaypointMemoryCapability instance, EnumFacing side) 
	{
		NBTTagCompound nbt = new NBTTagCompound();
		NBTTagList waypointListTag = new NBTTagList();
		
		List<Waypoint> waypoints = instance.getWaypoints();
		for (int i = 0; i < waypoints.size(); i++)
		{
			waypointListTag.appendTag(waypoints.get(i).serializeNBT());
		}

		nbt.setTag("waypoints", waypointListTag);
		
		return nbt;
	}

	@Override
	public void readNBT(Capability<IWaypointMemoryCapability> capability, IWaypointMemoryCapability instance, EnumFacing side, NBTBase nbt) 
	{
		NBTTagCompound nbtCompound = (NBTTagCompound)nbt;
		NBTTagList waypointListTag = (NBTTagList)nbtCompound.getTag("waypoints");
		List<Waypoint> waypoints = instance.getWaypoints();
		
		waypoints.clear();
		for (int i = 0; i < waypointListTag.tagCount(); i++)
		{
			Waypoint wp = new Waypoint();
			wp.deserializeNBT((NBTTagCompound)waypointListTag.get(i));
			waypoints.add(wp);
		}
	}
	
}
