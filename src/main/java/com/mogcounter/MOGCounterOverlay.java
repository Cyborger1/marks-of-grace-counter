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

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.time.temporal.ChronoUnit;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import javax.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import static net.runelite.api.MenuAction.RUNELITE_OVERLAY;
import static net.runelite.api.MenuAction.RUNELITE_OVERLAY_CONFIG;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;

class MOGCounterOverlay extends OverlayPanel
{
	static final String MARK_CLEAR = "Clear";
	static final String GROUND_RESET = "Reset Ground Counter";
	private final MOGCounterPlugin plugin;
	private final MOGCounterConfig config;

	@Inject
	private MOGCounterOverlay(MOGCounterPlugin plugin, MOGCounterConfig config)
	{
		super(plugin);
		setPosition(OverlayPosition.TOP_LEFT);
		setPriority(PRIORITY_LOW);
		this.plugin = plugin;
		this.config = config;
		getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "Mark overlay"));
		getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY, GROUND_RESET, "Mark overlay"));
		getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY, MARK_CLEAR, "Mark overlay"));
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showMarkCount() ||
			plugin.getLastMarkSpawnTime() == null)
		{
			return null;
		}

		panelComponent.getChildren().add(TitleComponent.builder().text("Marks of Grace").build());

		addLine("Total Spawns:", plugin.getMarkSpawnEvents());

		if (config.showMarksSpawned())
		{
			addLine("# on Ground:", plugin.getMarksOnGround());
		}

		if (config.showMarkLastSpawn())
		{
			long s = Duration.between(plugin.getLastMarkSpawnTime(), Instant.now()).getSeconds();
			addLine("Last Spawn:", formatSeconds(s));
		}

		if (config.showMarkLastSpawnMinute())
		{
			Instant t = plugin.getLastMarkSpawnTime()
				.plusMillis(config.markLastSpawnMinuteOffset())
				.truncatedTo(ChronoUnit.MINUTES);
			long s = Duration.between(t, Instant.now()).getSeconds();
			addLine("Last Spawn (M):", formatSeconds(s));
		}

		if (config.showMarksPerHour() && plugin.getMarkSpawnEvents() >= 2)
		{
			addLine("Spawns/Hour:", plugin.getSpawnsPerHour());
		}


		return super.render(graphics);
	}

	private void addLine(String left, String right)
	{
		panelComponent.getChildren().add(
			LineComponent.builder()
				.left(left)
				.right(right)
				.build());
	}

	private void addLine(String left, int right)
	{
		addLine(left, Integer.toString(right));
	}

	private String formatSeconds(long s)
	{
		return String.format("%d:%02d", (s % 3600) / 60, (s % 60));
	}
}
