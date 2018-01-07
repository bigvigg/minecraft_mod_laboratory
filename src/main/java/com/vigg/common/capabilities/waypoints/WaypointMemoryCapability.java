package com.vigg.common.capabilities.waypoints;

import java.util.ArrayList;
import java.util.List;

import com.vigg.common.Waypoint;

public class WaypointMemoryCapability implements IWaypointMemoryCapability
{
	private List<Waypoint> waypoints;
	
	public WaypointMemoryCapability()
	{
		this.waypoints = new ArrayList<Waypoint>();
	}
	
	public List<Waypoint> getWaypoints()
	{
		return this.waypoints;
	}
	
	public void setWaypoints(List<Waypoint> newWaypoints)
	{
		this.waypoints = newWaypoints;
	}
}
