package com.vigg.common.waypoints;

import java.util.UUID;

import com.vigg.common.ModItems;
import com.vigg.common.ModPacketHandler;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityWaypoint extends TileEntityBeacon
{
	private static final String NBT_KEY = "com.vigg.TileEntityWaypoint";
	
	private UUID recorderUUID = null;
	private EntityPlayer player = null;
	private IBlockState originalState = null;
	
	
	public void initWaypoint(UUID parRecorderUUID, EntityPlayer parPlayer, IBlockState parOriginalState)
	{
		recorderUUID = parRecorderUUID;
		player = parPlayer;
		originalState = parOriginalState;
		
		updateBeacon();
	}
	
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
		
		// make the WaypointTileEntity self-destruct when it's no longer supposed to be visible
		if (player == null || recorderUUID == null)
			selfDestruct();
		else
		{
			ItemStack heldItem = player.getHeldItemMainhand();
			
			if (heldItem == null)
				selfDestruct();
			else
			{
				WaypointRecorder recorder = ModItems.getWaypointRecorder();

				if (heldItem.getItem() != recorder || !recorderUUID.equals(recorder.getUUID(heldItem)) || !recorder.containsWaypoint(heldItem, pos.getX(), pos.getY(), pos.getZ()))
					selfDestruct();
			}
		}
	}

	private void selfDestruct()
	{
		//System.out.println("STUB self-destruct");
		
		if (originalState != null)
			world.setBlockState(pos, originalState);
		else
			world.setBlockState(pos, Blocks.AIR.getDefaultState());
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) 
	{
		super.readFromNBT(compound);
		
		if (compound.hasKey(NBT_KEY))
		{
			NBTTagCompound nbt = compound.getCompoundTag(NBT_KEY);

			if (nbt.hasKey("recorderUUID"))
				recorderUUID = UUID.fromString(nbt.getString("recorderUUID"));
			
			if (nbt.hasKey("player"))
				player = (EntityPlayer)world.getEntityByID(nbt.getInteger("player"));
			
			if (nbt.hasKey("originalState"))
				originalState = NBTUtil.readBlockState(nbt.getCompoundTag("originalState"));
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) 
	{
		compound = super.writeToNBT(compound);
		
		NBTTagCompound nbt = new NBTTagCompound();

		if (recorderUUID != null)
			nbt.setString("recorderUUID", recorderUUID.toString());
		
		if (player != null)
			nbt.setInteger("player", player.getEntityId());
		
		if (originalState != null)
			nbt.setTag("originalState", NBTUtil.writeBlockState(new NBTTagCompound(), originalState));
		
		compound.setTag(NBT_KEY, nbt);
		return compound;
	}
}
