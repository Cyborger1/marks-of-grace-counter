/*
 * Copyright (c) 2018, Seth <http://github.com/sethtroll>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.mogcounter;

import javax.inject.Singleton;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;

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
	private int totalMarksSpawned;
	@Getter(AccessLevel.PACKAGE)
	private int marksPerHour;

	void incrementMarksOfGraceSpawned(int inc)
	{
		marksSpawned += inc;
		if (marksSpawned < 0) marksSpawned = 0;
	}

	void checkMarkSpawned()
	{
		Instant now = Instant.now();
		if(marksSpawned > lastMarksSpawned)
		{
			if (firstMarkSpawn == null)
				firstMarkSpawn = now;
			lastMarkSpawn = now;
			totalMarksSpawned += (marksSpawned - lastMarksSpawned);
		}
		lastMarksSpawned = marksSpawned;

		if (firstMarkSpawn != null)
		{
			Duration timeSinceStart = Duration.between(firstMarkSpawn, now);
			if (!timeSinceStart.isZero())
			{
				double val = (double) totalMarksSpawned * (double) HOUR.toMillis() / (double) timeSinceStart.toMillis();
				if (val > 999) marksPerHour = 999;
				else marksPerHour = (int) val;
			}
		}

	}

	void clearCounters()
	{
		lastMarksSpawned = 0;
		lastMarkSpawn = null;
		firstMarkSpawn = null;
		marksSpawned = 0;
		totalMarksSpawned = 0;
		marksPerHour = 0;
	}

	void clearSpawnedMarks()
	{
		marksSpawned = 0;
		lastMarksSpawned = 0;
	}
}
