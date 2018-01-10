package com.vigg.common.waypoints;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class BlockWaypoint extends BlockContainer
{
	public BlockWaypoint()
    {
        super(Material.AIR, MapColor.AIR);
        
        String className = this.getClass().getSimpleName();
        
        this.setUnlocalizedName(className);
        this.setRegistryName(className);
    }

	/**
     * Returns a new instance of a block's tile entity class. Called on placing the block.
     */
	@Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TileEntityBeacon();
    }

    /**
     * Used to determine ambient occlusion and culling when rebuilding chunks for render
     */
	@Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }
	@Override
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

    /**
     * The type of render function called. MODEL for mixed tesr and static model, MODELBLOCK_ANIMATED for TESR-only,
     * LIQUID for vanilla liquids, INVISIBLE to skip all rendering
     */
	@Override
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    /**
     * Called by ItemBlocks after a block is set in the world, to allow post-place logic
     */
	@Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);

        // STUB - set block name.  is this called when block placed programatically?
        
        
        /*
        if (stack.hasDisplayName())
        {
            TileEntity tileentity = worldIn.getTileEntity(pos);

            if (tileentity instanceof TileEntityBeacon)
            {
                ((TileEntityBeacon)tileentity).setName(stack.getDisplayName());
            }
        }
        */
    }
	
    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.CUTOUT;
    }
}