package com.vigg.common.waypoints;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityWaypoint extends TileEntityBeacon
{
	@Override
	@SideOnly(Side.CLIENT)
    public float shouldBeamRender()
    {
		return 1f;
    }
	
	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox() 
	{
		return INFINITE_EXTENT_AABB;
	}
	
	@Override
	public boolean isUsableByPlayer(EntityPlayer player)
	{
		return false;
	}
	
	@Override
	public void update()
	{
		// do NOT call super.update() because we don't need to waste resources doing normal beacon stuff (player buffs, pyramid building, etc)
	}
}
