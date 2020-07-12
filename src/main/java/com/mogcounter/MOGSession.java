package com.mogcounter;

import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.coords.WorldPoint;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

class MOGSession
{
	private static final Duration HOUR = Duration.ofHours(1);

	private int lastMarksSpawned;
	@Getter(AccessLevel.PACKAGE)
	private int marksSpawned;
	@Getter(AccessLevel.PACKAGE)
	private Instant lastMarkSpawn;
	@Getter(AccessLevel.PACKAGE)
	private Instant firstMarkSpawn;
	@Getter(AccessLevel.PACKAGE)
	private int totalMarkSpawnEvents;
	@Getter(AccessLevel.PACKAGE)
	private int spawnsPerHour;

	private final Map<WorldPoint, Integer> markPoints = new HashMap<>();
	private boolean isDirty;

	void addMarkTile(WorldPoint point, int markCount)
	{
		markPoints.put(point, markCount);
		isDirty = true;
	}

	void removeMarkTile(WorldPoint point)
	{
		markPoints.remove(point);
		isDirty = true;
	}

	void checkMarkSpawned()
	{
		if (!isDirty) return;
		isDirty = false;

		marksSpawned = 0;
		for (int i : markPoints.values())
			marksSpawned += i;

		Instant now = Instant.now();
		if(marksSpawned > lastMarksSpawned)
		{
			if (firstMarkSpawn == null)
				firstMarkSpawn = now;
			lastMarkSpawn = now;
			totalMarkSpawnEvents++;
		}
		lastMarksSpawned = marksSpawned;

		if (firstMarkSpawn != null)
		{
			Duration timeSinceStart = Duration.between(firstMarkSpawn, now);
			if (!timeSinceStart.isZero())
			{
				double val = (double) totalMarkSpawnEvents * (double) HOUR.toMillis() / (double) timeSinceStart.toMillis();
				if (val > 999) spawnsPerHour = 999;
				else spawnsPerHour = (int) val;
			}
		}
	}

	void clearCounters()
	{
		lastMarksSpawned = 0;
		lastMarkSpawn = null;
		firstMarkSpawn = null;
		marksSpawned = 0;
		totalMarkSpawnEvents = 0;
		spawnsPerHour = 0;
		markPoints.clear();
		isDirty = false;
	}

	void clearSpawnedMarks()
	{
		marksSpawned = 0;
		lastMarksSpawned = 0;
		markPoints.clear();
		isDirty = false;
	}
}
