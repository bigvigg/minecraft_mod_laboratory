package com.vigg.common.capabilities.uuid;

import java.util.UUID;

public class UUIDCapability implements IUUIDCapability
{
	private UUID uuid = UUID.randomUUID();

	public UUIDCapability()
	{
		System.out.println("STUB constructing with " + uuid.toString());
	}
	
	public UUID getUUID()
	{
		return uuid;
	}
	
	public void setUUID(UUID newID)
	{
		System.out.println("STUB setting to " + newID.toString());
		uuid = newID;
	}
}
