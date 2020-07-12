package com.mogcounter;

import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.ItemSpawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.OverlayMenuClicked;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayMenuEntry;

@Slf4j
@PluginDescriptor(
	name = "Marks of Grace Counter",
	description = "Counts Marks of Grace spawns",
	tags={"marks","grace","agility","counter"}
)
public class MOGCounterPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private MOGCounterConfig config;

	@Inject
	private MOGCounterOverlay mogOverlay;

	@Getter
	private MOGSession mogSession;

	@Provides
	MOGCounterConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(MOGCounterConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(mogOverlay);
		mogSession = new MOGSession();
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(mogOverlay);
		mogSession = null;
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		switch (gameStateChanged.getGameState())
		{
			case HOPPING:
			case LOGIN_SCREEN:
				mogSession = null;
				break;
			case LOGGED_IN:
				if (mogSession == null)
					mogSession = new MOGSession();
				break;
		}
	}

	@Subscribe
	public void onItemSpawned(ItemSpawned itemSpawned)
	{
		final TileItem item = itemSpawned.getItem();
		if (item.getId() == ItemID.MARK_OF_GRACE)
		{
			WorldPoint wp = itemSpawned.getTile().getWorldLocation();
			Player player = client.getLocalPlayer();
			if (player != null && !wp.equals(player.getWorldLocation()))
				mogSession.addMarkTile(wp, item.getQuantity());
		}
	}

	@Subscribe
	public void onItemDespawned(ItemDespawned itemDespawned)
	{
		final TileItem item = itemDespawned.getItem();
		if (item.getId() == ItemID.MARK_OF_GRACE)
			mogSession.removeMarkTile(itemDespawned.getTile().getWorldLocation());
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		mogSession.checkMarkSpawned();
	}

	@Subscribe
	public void onOverlayMenuClicked(OverlayMenuClicked overlayMenuClicked)
	{
		OverlayMenuEntry overlayMenuEntry = overlayMenuClicked.getEntry();
		if (overlayMenuEntry.getMenuAction() == MenuAction.RUNELITE_OVERLAY
			&& overlayMenuClicked.getOverlay() == mogOverlay)
		{
			switch(overlayMenuClicked.getEntry().getOption())
			{
				case MOGCounterOverlay.MARK_CLEAR:
					mogSession.clearCounters();
					break;
				case MOGCounterOverlay.GROUND_RESET:
					mogSession.clearSpawnedMarks();
					break;
			}
		}
	}
}
