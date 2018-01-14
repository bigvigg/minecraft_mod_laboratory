package com.vigg.common.waypoints;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.vigg.common.ModItems;
import com.vigg.common.waypoints.IWaypointStorage.WaypointEntry;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

//code stolen from vanilla TileEntityBeacon

public class TileEntityWaypoint extends TileEntity implements ITickable
{
	public static final String NBT_KEY = "com.vigg.TileEntityWaypoint";
	
	private UUID recorderUUID = null;
	private EntityPlayer player = null;
	private IBlockState originalState = null;
	private EnumDyeColor beamColor = EnumDyeColor.WHITE;
	
    // properties copied from TileEntityBeacon:
    private final List<TileEntityWaypoint.BeamSegment> beamSegments = Lists.<TileEntityWaypoint.BeamSegment>newArrayList();
    @SideOnly(Side.CLIENT)
    private long beamRenderCounter;
    @SideOnly(Side.CLIENT)
    private float beamRenderScale;
    private String customName;
    
	
	public void initWaypoint(UUID parRecorderUUID, EntityPlayer parPlayer, IBlockState parOriginalState)
	{
		recorderUUID = parRecorderUUID;
		player = parPlayer;
		originalState = parOriginalState;
		
		updateBeacon();
	}
	
	public void setName(String name)
    {
        this.customName = name;
    }
	
	public String getName()
    {
        return this.hasCustomName() ? this.customName : "unnamed waypoint";
    }

    public boolean hasCustomName()
    {
        return this.customName != null && !this.customName.isEmpty();
    }
	
	
	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox() 
	{
		return INFINITE_EXTENT_AABB;
	}
	
	@Override
	public void update()
	{
		if (this.world.getTotalWorldTime() % 80L == 0L)
            this.updateBeacon();

		
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
				ItemWaypointRecorder recorder = ModItems.getWaypointRecorder();
				
				if (heldItem.getItem() != recorder || !recorderUUID.equals(recorder.getUUID(heldItem)))
					selfDestruct();
				else
				{
					// see if we can find a Waypoint with our current location
					WaypointEntry waypointEntry = null;
					for (int i = 0; i < ClientStateManager.heldRecorderWaypoints.length; i++)
					{
						Waypoint wp = ClientStateManager.heldRecorderWaypoints[i];
						if (wp.x == pos.getX() && wp.y == pos.getY() && wp.z == pos.getZ())
						{
							waypointEntry = new WaypointEntry(i, wp);
							break;
						}
					}
					
					if (waypointEntry == null)
						selfDestruct();
					else
					{
						// update beacon display with latest waypoint data
						
						String newName = "#" + Integer.toString(waypointEntry.index + 1) + " " + waypointEntry.waypoint.getLabel();
						
						if (waypointEntry.index == ClientStateManager.selectedWaypointIndex && ClientStateManager.selectedMode == ItemWaypointRecorder.RecorderMode.EDIT)
							this.beamColor = EnumDyeColor.LIME;
						else
							this.beamColor = EnumDyeColor.WHITE;
								
						this.setName(newName);
						this.updateBeacon();
					}
				}
			}
		}
	}
	
	public void updateBeacon()
    {
        if (this.world != null)
        {
            this.updateSegmentColors();
        }
    }


    private void updateSegmentColors()
    {
        int i = this.pos.getX();
        int j = this.pos.getY();
        int k = this.pos.getZ();
        this.beamSegments.clear();
        TileEntityWaypoint.BeamSegment tileentitybeacon$beamsegment = new TileEntityWaypoint.BeamSegment(beamColor.getColorComponentValues());
        this.beamSegments.add(tileentitybeacon$beamsegment);
        boolean flag = true;
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (int i1 = j + 1; i1 < 256; ++i1)
        {
            IBlockState iblockstate = this.world.getBlockState(blockpos$mutableblockpos.setPos(i, i1, k));
            float[] afloat;

            //if (iblockstate.getBlock() == Blocks.STAINED_GLASS)
            //{
            //    afloat = ((EnumDyeColor)iblockstate.getValue(BlockStainedGlass.COLOR)).getColorComponentValues();
            //}
            //else
            //{
                //if (iblockstate.getBlock() != Blocks.STAINED_GLASS_PANE)
                //{
                    if (iblockstate.getLightOpacity(world, blockpos$mutableblockpos) >= 15 && iblockstate.getBlock() != Blocks.BEDROCK)
                    {
                        this.beamSegments.clear();
                        break;
                    }
                    float[] customColor = iblockstate.getBlock().getBeaconColorMultiplier(iblockstate, this.world, blockpos$mutableblockpos, getPos());
                    if (customColor != null)
                        afloat = customColor;
                    else {
                    	tileentitybeacon$beamsegment.incrementHeight();
                    	continue;
                    }
                //}
                //else
                //	afloat = ((EnumDyeColor)iblockstate.getValue(BlockStainedGlassPane.COLOR)).getColorComponentValues();
            //}

            if (!flag)
            {
                afloat = new float[] {(tileentitybeacon$beamsegment.getColors()[0] + afloat[0]) / 2.0F, (tileentitybeacon$beamsegment.getColors()[1] + afloat[1]) / 2.0F, (tileentitybeacon$beamsegment.getColors()[2] + afloat[2]) / 2.0F};
            }

            if (Arrays.equals(afloat, tileentitybeacon$beamsegment.getColors()))
            {
                tileentitybeacon$beamsegment.incrementHeight();
            }
            else
            {
                tileentitybeacon$beamsegment = new TileEntityWaypoint.BeamSegment(afloat);
                this.beamSegments.add(tileentitybeacon$beamsegment);
            }

            flag = false;
        }

        //if (this.isComplete)
        //{
            for (int l1 = 1; l1 <= 4; /*this.levels = */l1++)
            {
                int i2 = j - l1;

                if (i2 < 0)
                {
                    break;
                }

                boolean flag1 = true;

                for (int j1 = i - l1; j1 <= i + l1 && flag1; ++j1)
                {
                    for (int k1 = k - l1; k1 <= k + l1; ++k1)
                    {
                        Block block = this.world.getBlockState(new BlockPos(j1, i2, k1)).getBlock();

                        if (!block.isBeaconBase(this.world, new BlockPos(j1, i2, k1), getPos()))
                        {
                            flag1 = false;
                            break;
                        }
                    }
                }

                if (!flag1)
                {
                    break;
                }
            }

        //}

    }

	private void selfDestruct()
	{
		//System.out.println("STUB self-destruct");
		
		if (originalState != null)
			world.setBlockState(pos, originalState);
		else
			world.setBlockState(pos, Blocks.AIR.getDefaultState());
	}
	
	@SideOnly(Side.CLIENT)
    public List<TileEntityWaypoint.BeamSegment> getBeamSegments()
    {
        return this.beamSegments;
    }
	
	@Override
	@SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared()
    {
        return 65536.0D;
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
	
	public static class BeamSegment
    {
        /** RGB (0 to 1.0) colors of this beam segment */
        private final float[] colors;
        private int height;

        public BeamSegment(float[] colorsIn)
        {
            this.colors = colorsIn;
            this.height = 1;
        }

        protected void incrementHeight()
        {
            ++this.height;
        }

        /**
         * Returns RGB (0 to 1.0) colors of this beam segment
         */
        public float[] getColors()
        {
            return this.colors;
        }

        @SideOnly(Side.CLIENT)
        public int getHeight()
        {
            return this.height;
        }
    }
}
