package com.mogcounter;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Units;

@ConfigGroup("mogcounter")
public interface MOGCounterConfig extends Config
{
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

	@ConfigItem(
			keyName = "markTimeout",
			name = "Hide Overlay",
			description = "Time until the Marks of Grace Counter hides/resets (Uses 'Last Mark Time')",
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
}
