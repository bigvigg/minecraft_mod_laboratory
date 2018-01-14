package com.vigg.common.waypoints;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.UUID;

import com.vigg.common.ModItems;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


// ClientStateManager keeps track of short-lived, client-only data in static variables.

@Mod.EventBusSubscriber
public class ClientStateManager 
{
	@SideOnly(Side.CLIENT)
	private static UUID lastTickRecorderUUID = null; // set every tick in onClientTick
	
	
	
	@SideOnly(Side.CLIENT)
	public static int selectedWaypointIndex = -1;
	
	@SideOnly(Side.CLIENT)
	public static ItemWaypointRecorder.RecorderMode selectedMode = ItemWaypointRecorder.RecorderMode.ADD_REMOVE;
	
	@SideOnly(Side.CLIENT)
	public static Waypoint[] heldRecorderWaypoints = new Waypoint[0];
	
	
	
	@SubscribeEvent	
	@SideOnly(Side.CLIENT)
	public static void onClientTick(TickEvent.ClientTickEvent e)
	{		
		// Whenever a recorder is selected in the player's hand, show them a message to remind them what mode it's in.
		// (it's weird that I can't find any sort of ItemSelected event hook, and instead have to do it this way, by checking every tick)
		
		ItemWaypointRecorder itemRecorder = ModItems.getWaypointRecorder();
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		heldRecorderWaypoints = null;
		
		if (player != null)
		{
			ItemStack heldItem = player.getHeldItemMainhand();
			UUID heldItemUUID = null;
			
			if (heldItem != null && heldItem.getItem() == itemRecorder)
			{
				heldItemUUID = itemRecorder.getUUID(heldItem);
				heldRecorderWaypoints = itemRecorder.getWaypoints(heldItem);

				// show the mode message when the player selects a recorder in their hand
				if (heldItemUUID != null && (lastTickRecorderUUID == null || !heldItemUUID.equals(lastTickRecorderUUID)))
				{
					ItemWaypointRecorder.showModeMessage(player, heldItem, ClientStateManager.selectedMode);
					selectedWaypointIndex = -1;
				}
			}
			
			lastTickRecorderUUID = heldItemUUID;
			
			if (heldRecorderWaypoints == null)
				heldRecorderWaypoints = new Waypoint[0];
		}
	}
}
