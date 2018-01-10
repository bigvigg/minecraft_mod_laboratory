package com.vigg.common.waypoints;

import java.util.List;

import com.vigg.common.Waypoint;

// T is the type of the object that IWaypointStorage is implemented on
public interface IWaypointStorage<T> 
{
	public Waypoint[] getWaypoints(T container);
	
	public void setWaypoints(T container, Waypoint[] waypoints);
	
	// returns the index the waypoint was added at
	public int addWaypoint(T container, Waypoint waypoint);
	
	public int getWaypointCount(T container);
}
