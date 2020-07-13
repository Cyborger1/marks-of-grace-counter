package com.mogcounter;

import com.google.common.collect.EvictingQueue;
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
	private Instant lastMarkSpawnTime;
	@Getter(AccessLevel.PACKAGE)
	private int totalMarkSpawnEvents;
	@Getter(AccessLevel.PACKAGE)
	private int spawnsPerHour;

	private final Map<WorldPoint, Integer> markPoints = new HashMap<>();
	private boolean isDirty;
	private EvictingQueue<Duration> markSpawnTimes = EvictingQueue.create(10);

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
			if (lastMarkSpawnTime != null)
			{
				markSpawnTimes.add(Duration.between(lastMarkSpawnTime, now));
				calculateMarksPerHour();
			}
			lastMarkSpawnTime = now;
			totalMarkSpawnEvents++;
		}
		lastMarksSpawned = marksSpawned;
	}

	private void calculateMarksPerHour()
	{
		int sz = markSpawnTimes.size();
		if (sz > 0)
		{
			Duration sum = Duration.ZERO;
			for (Duration markTime : markSpawnTimes)
				sum = sum.plus(markTime);

			spawnsPerHour = (int) (HOUR.toMillis() / sum.dividedBy(sz).toMillis());
		}
		else
			spawnsPerHour = 0;
	}

	void clearCounters()
	{
		lastMarksSpawned = 0;
		lastMarkSpawnTime = null;
		markSpawnTimes.clear();
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
