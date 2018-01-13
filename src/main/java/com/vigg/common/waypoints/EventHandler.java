package com.vigg.common.waypoints;

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

@Mod.EventBusSubscriber
public class EventHandler 
{
	private static UUID lastTickRecorderUUID = null; // set every tick in onClientTick
	
	@SubscribeEvent	
	@SideOnly(Side.CLIENT)
	public static void onClientTick(TickEvent.ClientTickEvent e)
	{		
		// Whenever a recorder is selected in the player's hand, show them a message to remind them what mode it's in.
		// (it's weird that I can't find any sort of ItemSelected event hook, and instead have to do it this way, by checking every tick)
		
		ItemWaypointRecorder itemRecorder = ModItems.getWaypointRecorder();
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		
		if (player != null)
		{
			ItemStack heldItem = player.getHeldItemMainhand();
			
			if (heldItem == null)
				lastTickRecorderUUID = null;
			else if (heldItem.getItem() == itemRecorder)
			{
				UUID currentUUID = itemRecorder.getUUID(heldItem);

				if (currentUUID != null && (lastTickRecorderUUID == null || !currentUUID.equals(lastTickRecorderUUID)))
				{
					ItemWaypointRecorder.showModeMessage(player, heldItem, itemRecorder.getRecorderMode(heldItem));
				}
				
				lastTickRecorderUUID = currentUUID;
			}
			else
				lastTickRecorderUUID = null;
		}
	}
}
