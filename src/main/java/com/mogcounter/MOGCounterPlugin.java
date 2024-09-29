/*
 * Copyright (c) 2020, Cyborger1
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.mogcounter;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import javax.inject.Inject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.api.Player;
import static net.runelite.api.Skill.AGILITY;
import net.runelite.api.TileItem;
import net.runelite.api.MenuAction;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.ItemSpawned;
import net.runelite.api.events.StatChanged;
import net.runelite.client.Notifier;
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
	tags = {"marks", "grace", "agility", "counter"}
)
public class MOGCounterPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private Notifier notifier;

	@Inject
	private MOGCounterConfig config;

	@Inject
	private MOGCounterOverlay mogOverlay;

	@Provides
	MOGCounterConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(MOGCounterConfig.class);
	}

	// From Agility plugin, some may not be relevant to this plugin but ehh
	private final Set<WorldPoint> courseEndpoints = ImmutableSet.of(
		new WorldPoint(2484, 3437, 0), // Gnome 1
		new WorldPoint(2487, 3437, 0), // Gnome 2
		new WorldPoint(1554, 3640, 0), // Shayzien Basic
		new WorldPoint(3103, 3261, 0), // Draynor
		new WorldPoint(3299, 3194, 0), // Al-Kharid
		new WorldPoint(3364, 2830, 0), // Pyramid
		new WorldPoint(3236, 3417, 0), // Varrock
		new WorldPoint(2652, 4039, 1), // Penguin
		new WorldPoint(2543, 3553, 0), // Barbarian
		new WorldPoint(3510, 3485, 0), // Canifis
		new WorldPoint(2770, 2747, 0), // Ape Atoll
		new WorldPoint(1522, 3625, 0), // Shayzien Advanced
		new WorldPoint(3029, 3332, 0), // Falador 1
		new WorldPoint(3029, 3333, 0), // Falador 2
		new WorldPoint(3029, 3334, 0), // Falador 3
		new WorldPoint(3029, 3335, 0), // Falador 4
		new WorldPoint(2994, 3933, 0), // Wilderness
		new WorldPoint(3528, 9873, 0), // Werewolf
		new WorldPoint(2704, 3464, 0), // Seers
		new WorldPoint(3363, 2998, 0), // Pollnivneach
		new WorldPoint(2653, 3676, 0), // Relleka
		new WorldPoint(3240, 6109, 0), // Prifddinas
		new WorldPoint(2668, 3297, 0)  // Ardougne
	);

	@Getter
	private int marksOnGround;
	@Getter
	private Instant lastMarkSpawnTime;
	@Getter
	private Instant lastLapTime;
	@Getter
	private int markSpawnEvents;
	@Getter
	private int spawnsPerHour;

	private final Map<WorldPoint, InstantCountTuple> markTiles = new HashMap<>();
	private final Set<WorldPoint> potentialDespawns = new HashSet<>();
	private final Set<WorldPoint> ignoreTiles = new HashSet<>();
	private boolean doCheckGroundItems;
	private Instant lastDespawnNotified;
	private final EvictingQueue<Duration> markSpawnTimes = EvictingQueue.create(20);

	private final Supplier<Instant> markSpawnTimeSupplier = () -> lastLapTime != null && config.useLapFinishTiming() ? lastLapTime : Instant.now();

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(mogOverlay);
		clearCounters();
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(mogOverlay);
		clearCounters();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		switch (gameStateChanged.getGameState())
		{
			case HOPPING:
			case LOGIN_SCREEN:
				clearCounters();
				break;
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged statChanged)
	{
		if (statChanged.getSkill() == AGILITY
			&& courseEndpoints.contains(client.getLocalPlayer().getWorldLocation()))
		{
			lastLapTime = Instant.now();
		}
	}

	@Subscribe
	public void onItemSpawned(ItemSpawned itemSpawned)
	{
		final TileItem item = itemSpawned.getItem();
		if (item.getId() != ItemID.MARK_OF_GRACE)
		{
			return;
		}

		Player player = client.getLocalPlayer();
		if (player == null)
		{
			return;
		}

		WorldPoint wp = itemSpawned.getTile().getWorldLocation();
		// Means we had a despawn/respawn in the same tick, do not clear in onGameTick
		potentialDespawns.remove(wp);

		if (wp.equals(player.getWorldLocation()) || ignoreTiles.contains(wp))
		{
			ignoreTiles.add(wp);
			return;
		}

		InstantCountTuple tuple;
		if (!markTiles.containsKey(wp))
		{
			tuple = new InstantCountTuple(null, 0);
			markTiles.put(wp, tuple);
		}
		else
		{
			tuple = markTiles.get(wp);
		}

		int newCount = item.getQuantity();
		if (newCount != tuple.getCount())
		{
			if (newCount > tuple.getCount())
			{
				tuple.setInstant(markSpawnTimeSupplier.get());
			}
			tuple.setCount(newCount);
			doCheckGroundItems = true;
		}
	}

	@Subscribe
	public void onItemDespawned(ItemDespawned itemDespawned)
	{
		final TileItem item = itemDespawned.getItem();
		if (item.getId() != ItemID.MARK_OF_GRACE)
		{
			return;
		}

		Player player = client.getLocalPlayer();
		if (player == null)
		{
			return;
		}

		WorldPoint wp = itemDespawned.getTile().getWorldLocation();
		// Marks are despawned then immediately spawned again when changing floor
		// Only remove from markTiles if no respawn seen before onGameTick
		potentialDespawns.add(wp);
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		// Check session timeout
		if (lastMarkSpawnTime != null)
		{
			Instant expire = Instant.now().minus(config.markTimeout(), ChronoUnit.MINUTES);
			if (lastMarkSpawnTime.isBefore(expire))
			{
				clearCounters();
				return;
			}
		}

		// Some marks were fully despawned, clean them up here
		if (!potentialDespawns.isEmpty())
		{
			Player player = client.getLocalPlayer();
			for (WorldPoint wp : potentialDespawns)
			{
				markTiles.remove(wp);
				if (player != null && wp.equals(player.getWorldLocation()))
				{
					ignoreTiles.remove(wp);
				}
			}
			potentialDespawns.clear();
			doCheckGroundItems = true;
		}

		checkMarkSpawned();
		notifyDespawns();
	}

	@Subscribe
	public void onOverlayMenuClicked(OverlayMenuClicked overlayMenuClicked)
	{
		OverlayMenuEntry overlayMenuEntry = overlayMenuClicked.getEntry();
		if (overlayMenuEntry.getMenuAction() == MenuAction.RUNELITE_OVERLAY
			&& overlayMenuClicked.getOverlay() == mogOverlay)
		{
			switch (overlayMenuClicked.getEntry().getOption())
			{
				case MOGCounterOverlay.MARK_CLEAR:
					clearCounters();
					break;
				case MOGCounterOverlay.GROUND_RESET:
					clearSpawnedMarks();
					break;
			}
		}
	}

	private synchronized void checkMarkSpawned()
	{
		if (!doCheckGroundItems)
		{
			return;
		}
		doCheckGroundItems = false;

		int newMarksOnGround = markTiles.values().stream().mapToInt(InstantCountTuple::getCount).sum();

		if (newMarksOnGround > marksOnGround)
		{
			Instant spawnMoment = markSpawnTimeSupplier.get();
			if (lastMarkSpawnTime != null)
			{
				markSpawnTimes.add(Duration.between(lastMarkSpawnTime, spawnMoment));
				calculateMarksPerHour();
			}
			lastMarkSpawnTime = spawnMoment;
			markSpawnEvents++;
		}

		marksOnGround = newMarksOnGround;
	}

	private void notifyDespawns()
	{
		if (marksOnGround <= 0 || markTiles.isEmpty())
		{
			return;
		}

		Instant expire = Instant.now().minusSeconds(config.markDespawnNotificationTime());
		boolean doNotify = false;
		for (InstantCountTuple entry : markTiles.values())
		{
			int count = entry.getCount();
			Instant spawn = entry.getInstant();

			if (count <= 0 || spawn == null
				|| (lastDespawnNotified != null && spawn.compareTo(lastDespawnNotified) <= 0))
			{
				continue;
			}

			if (spawn.isBefore(expire))
			{
				lastDespawnNotified = spawn;
				doNotify = true;
			}
		}

		if (doNotify)
		{
			String text = markTiles.size() > 1 ?
				"One of your Marks of Grace stacks is about to despawn!" :
				"Your Marks of Grace stack is about to despawn!";
			notifier.notify(config.markDespawnNotification(), text);
		}
	}

	private void calculateMarksPerHour()
	{
		int sz = markSpawnTimes.size();
		if (sz > 0)
		{
			Duration sum = markSpawnTimes.stream().reduce(Duration.ZERO, Duration::plus);
			spawnsPerHour = (int) (Duration.ofHours(1).toMillis() / sum.dividedBy(sz).toMillis());
		}
		else
		{
			spawnsPerHour = 0;
		}
	}

	public void clearCounters()
	{
		lastMarkSpawnTime = null;
		lastLapTime = null;
		markSpawnTimes.clear();
		marksOnGround = 0;
		markSpawnEvents = 0;
		spawnsPerHour = 0;
		markTiles.clear();
		ignoreTiles.clear();
		potentialDespawns.clear();
		doCheckGroundItems = false;
		lastDespawnNotified = null;
	}

	public void clearSpawnedMarks()
	{
		marksOnGround = 0;
		markTiles.clear();
		ignoreTiles.clear();
		potentialDespawns.clear();
		doCheckGroundItems = false;
		lastDespawnNotified = null;
	}
}
