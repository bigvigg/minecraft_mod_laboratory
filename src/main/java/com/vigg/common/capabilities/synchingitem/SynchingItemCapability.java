package com.vigg.common.capabilities.synchingitem;

public class SynchingItemCapability implements ISynchingItemCapability
{
	private boolean _isDirty = true;
	private long _lastRequestTime = 0;
	private long _lastResponseTime = 0;
	
	
	@Override
	public boolean isDirty() {
		return _isDirty;
	}

	@Override
	public void setIsDirty(boolean dirty) {
		_isDirty = dirty;
	}

	@Override
	public long lastRequestTime() {
		return _lastRequestTime;
	}

	@Override
	public void setLastRequestTime(long time) {
		_lastRequestTime = time;
	}

	@Override
	public long lastResponseTime() {
		return _lastResponseTime;
	}

	@Override
	public void setLastResponseTime(long time) {
		_lastResponseTime = time;
	}

}
