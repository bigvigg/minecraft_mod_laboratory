package com.vigg.common.waypoints;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

// T is the type of the object that IWaypointStorage is implemented on
public interface IWaypointStorage<T> 
{
	public Waypoint[] getWaypoints(T container);
	
	public void setWaypoints(T container, Waypoint[] waypoints);
	
	// returns the index the waypoint was added at
	public int addWaypoint(T container, Waypoint waypoint);
	
	public int getWaypointCount(T container);
	
	//public boolean containsWaypoint(T container, int x, int y, int z);
	
	public WaypointEntry getWaypoint(T container, int x, int y, int z);
	
	public class WaypointEntry implements INBTSerializable<NBTTagCompound>
	{
		public int index;
		public Waypoint waypoint;
		
		public WaypointEntry()
		{	
		}
		
		public WaypointEntry(int parIndex, Waypoint parWaypoint)
		{	
			index = parIndex;
			waypoint = parWaypoint;
		}
		
		
		@Override
		public NBTTagCompound serializeNBT() 
		{
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setInteger("index", index);
			nbt.setTag("waypoint", waypoint.serializeNBT());
			return nbt;
		}

		@Override
		public void deserializeNBT(NBTTagCompound nbt) 
		{
			index = nbt.getInteger("index");
			
			waypoint = new Waypoint();
			waypoint.deserializeNBT(nbt.getCompoundTag("waypoint"));
		}
	}
}
