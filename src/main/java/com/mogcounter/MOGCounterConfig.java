package com.mogcounter;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Units;

@ConfigGroup("example")
public interface MOGCounterConfig extends Config
{
	@ConfigItem(
			keyName = "showMarkCount",
			name = "Show Marks Count",
			description = "Enable/disable the Marks of Grace Counter"
	)
	default boolean showMarkCount()
	{
		return true;
	}

	@ConfigItem(
			keyName = "markTimeout",
			name = "Hide Marks Count",
			description = "Time until the Marks of Grace Counter hides/resets (Uses 'Last Mark Time')"
	)
	@Units(Units.MINUTES)
	default int markTimeout() { return 10; }

	@ConfigItem(
			keyName = "showMarksSpawned",
			name = "Show Amount of Spawned Marks",
			description = "Shows how many Marks are currently on the course (Recommended for Ardougne Rooftops)"
	)
	default boolean showMarksSpawned()
	{
		return false;
	}

	@ConfigItem(
			keyName = "showMarkLastSpawn",
			name = "Show Time Since Last Mark",
			description = "Shows the time since the last Mark of Grace spawned"
	)
	default boolean showMarkLastSpawn()
	{
		return true;
	}

	@ConfigItem(
			keyName = "showMarksPerHour",
			name = "Show Marks per Hour",
			description = "Shows the estimated amount of Mark spawns per hour"
	)
	default boolean showMarksPerHour()
	{
		return true;
	}
}
