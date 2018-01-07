package com.vigg.common.capabilities.waypoints;

import java.util.List;

import com.vigg.common.Waypoint;

public interface IWaypointMemoryCapability 
{
	public List<Waypoint> getWaypoints();
	public void setWaypoints(List<Waypoint> newWaypoints);
}
