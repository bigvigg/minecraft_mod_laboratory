package com.vigg.common.capabilities.synchingitem;

// SynchingItemCapability was created specifically for use in the SynchingItem class.  I don't intend to use it anywhere
// besides the inner workings of the SynchingItem.

public interface ISynchingItemCapability 
{
	// on the client, isDirty is true if the client needs to request an update for an ItemStack
	// on the server, isDirty is true if the server needs to send an *unrequested* update for the ItemStack	
	public boolean isDirty();
	public void setIsDirty(boolean dirty);
	
	// The UTC time in total milliseconds when the client last requested an update from the server
	public long lastRequestTime();
	public void setLastRequestTime(long time);
	
	// The UTC time in total milliseconds when the server last sent an update to the client
	public long lastResponseTime();
	public void setLastResponseTime(long time);
}
