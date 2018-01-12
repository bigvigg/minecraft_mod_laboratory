package com.vigg.client;

import com.vigg.common.CommonProxy;
import com.vigg.common.Reference;
import com.vigg.common.waypoints.TileEntityWaypoint;
import com.vigg.common.waypoints.TileEntityWaypointRenderer;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);
    }

    @Override
    public void init(FMLInitializationEvent e) {
        super.init(e);

        //reg(ModBlocks.getBlockWaypoint());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWaypoint.class, new TileEntityWaypointRenderer());
        //ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWaypoint.class, new TileEntityBeaconRenderer());
    }

    @Override
    public void postInit(FMLPostInitializationEvent e) {
        super.postInit(e);
    }
    

    private void reg(Block block) {
        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(Item.getItemFromBlock(block), 0, new ModelResourceLocation(Reference.MOD_ID + ":" + block.getUnlocalizedName().substring(5), "inventory"));
    }
}

