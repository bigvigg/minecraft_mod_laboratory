package com.vigg.common.waypoints;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

public class Waypoint implements INBTSerializable<NBTTagCompound> 
{
	public static String getCoordinateString(int x, int y, int z)
	{
		return "[" + Integer.toString(x) + "x " + Integer.toString(y) + "y " + Integer.toString(z) + "z]";
	}
	

	public int x, y, z;
	
	
	private String label = null;
	public String getLabel()
	{
		if (this.hasCustomLabel())
			return label;
		else
			return this.getCoordinateString();
			
	}
	public void setLabel(String newLabel)
	{
		this.label = newLabel;
	}
	public boolean hasCustomLabel()
	{
		return (label != null && !label.isEmpty());
	}
	
	
	public Waypoint()
	{
	}
	
	public Waypoint(int X, int Y, int Z)
	{
		this.x = X;
		this.y = Y;
		this.z = Z;
	}
	
	public Waypoint(String Label, int X, int Y, int Z)
	{
		this(X,Y,Z);
		this.setLabel(Label);
	}
	
	
	
	public String getCoordinateString()
	{
		return getCoordinateString(x,y,z);
	}
	
	@Override
	public NBTTagCompound serializeNBT() 
	{
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger("x", this.x);
		tag.setInteger("y", this.y);
		tag.setInteger("z", this.z);
		
		if (this.label != null)
			tag.setString("label", this.label);
		
		return tag;
	}
	
	@Override
	public void deserializeNBT(NBTTagCompound nbt) 
	{
		this.x = nbt.getInteger("x");
		this.y = nbt.getInteger("y");
		this.z = nbt.getInteger("z");
		
		if (nbt.hasKey("label"))
			this.setLabel(nbt.getString("label"));		
	}
	
	@Override
	public boolean equals (Object obj)
	{
		if (obj == this)
			return true;
		else if (obj instanceof Waypoint)
		{
			Waypoint wp = (Waypoint)obj;
			return (wp.x == this.x && wp.y == this.y && wp.z == this.z);
		}
		else
			return super.equals(obj);
	}
}
