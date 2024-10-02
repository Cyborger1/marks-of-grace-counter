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

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Notification;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

@ConfigGroup("mogcounter")
public interface MOGCounterConfig extends Config
{
	@ConfigSection(
		name = "Experimental",
		description = "Experimental settings",
		position = 100
	)
	String experimentalSection = "experimental";

	@ConfigItem(
		keyName = "showMarkCount",
		name = "Show Overlay",
		description = "Show/Hide the Marks of Grace Counter (It keeps counting)",
		position = 1
	)
	default boolean showMarkCount()
	{
		return true;
	}

	@Range
	(
		min = 1,
		max = 60
	)
	@ConfigItem(
		keyName = "markTimeout",
		name = "Reset Overlay Time",
		description = "Time until the Marks of Grace Counter hides/resets (Uses 'Last Spawn Time')",
		position = 2
	)
	@Units(Units.MINUTES)
	default int markTimeout()
	{
		return 10;
	}

	@ConfigItem(
		keyName = "showMarksSpawned",
		name = "Show On Ground Counter",
		description = "Shows how many Marks are currently on the ground (Recommended for Ardougne Rooftops)",
		position = 3
	)
	default boolean showMarksSpawned()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showMarkLastSpawn",
		name = "Show Time Since Last Spawn",
		description = "Shows the time since the last Mark of Grace spawned",
		position = 4
	)
	default boolean showMarkLastSpawn()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showMarksPerHour",
		name = "Show Spawns per Hour",
		description = "Shows the estimated amount of Mark spawns per hour (After getting 2 spawns)",
		position = 5
	)
	default boolean showMarksPerHour()
	{
		return true;
	}

	@ConfigItem(
		keyName = "useLapFinishTiming",
		name = "Use Lap Finish Mark Timing",
		description = "If checked, the last mark spawned timer will be based on the moment of the last finished lap. Otherwise, timer is based on the moment the mark is seen.",
		position = 6
	)
	default boolean useLapFinishTiming()
	{
		return false;
	}

	@ConfigItem(
		keyName = "markDespawnNotification",
		name = "Notify Before Mark Despawn",
		description = "Sends a notification if enough time has passed since a Mark spawned. Mostly intended for use with the Ardougne course.",
		position = 7
	)
	default Notification markDespawnNotification()
	{
		return Notification.OFF;
	}

	@Range
	(
		min = 60,
		max = 600
	)
	@ConfigItem(
		keyName = "markDespawnNotificationTime",
		name = "Despawn Notification Time",
		description = "Time until a despawn warning notification is sent for any given Mark stack",
		position = 8
	)
	@Units(Units.SECONDS)
	default int markDespawnNotificationTime()
	{
		return 480;
	}

	@ConfigItem(
		keyName = "showMarkLastSpawnMinute",
		name = "Show Time Since Last Spawn (M)",
		description = "Shows the time since the beginning of the minute of 'Last Spawn'. Intended to help time exact Mark spawns along with 'Lap Finish Mark Timing'.",
		position = 1,
		section = experimentalSection
	)
	default boolean showMarkLastSpawnMinute()
	{
		return false;
	}

	@Range(
		min = -60000,
		max = 60000
	)
	@ConfigItem(
		keyName = "markLastSpawnMinuteOffset",
		name = "Last Spawn (M) Offset",
		description = "Use to adjust the start of the minute if your ticks are not lining up correctly",
		position = 2,
		section = experimentalSection
	)
	@Units(value = Units.MILLISECONDS)
	default int markLastSpawnMinuteOffset()
	{
		return 0;
	}
}
