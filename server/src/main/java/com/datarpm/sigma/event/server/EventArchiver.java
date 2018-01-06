/*******************************************************************************
 * Copyright 2017 DataRPM
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
/**
 * 
 */
package com.datarpm.sigma.event.server;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.datarpm.sigma.event.core.EventEnvironment;
import com.datarpm.sigma.event.model.EventHdfsDumperRunStats;
import com.datarpm.sigma.event.model.EventModel;
import com.datarpm.sigma.event.model.da.EventHdfsDumperRunStatsAccessProvider;
import com.datarpm.sigma.event.model.da.EventModelAccessProvider;

/**
 * @author vishal
 *
 */
public class EventArchiver {

  public static final EventArchiver INSTANCE = new EventArchiver();
  private static final long SCHEDULE_TIME = TimeUnit.HOURS.toMillis(1);
  private TimedHDFSDumpTask timerTask;
  private Timer timer;
  private ReentrantLock writeLock;

  private EventArchiver() {
    writeLock = new ReentrantLock();
  }

  public void start() {
    if (timerTask == null) {
      timerTask = new TimedHDFSDumpTask();
      timer = new Timer(true);
    }

    timer.scheduleAtFixedRate(timerTask, 0, SCHEDULE_TIME);
  }

  public void stop() {
    timer.cancel();
  }

  class TimedHDFSDumpTask extends TimerTask {

    @Override
    public void run() {
      EventHdfsDumperRunStatsAccessProvider accessProvider = null;
      long fetchTillMillis = System.currentTimeMillis();
      long fetchFromMillis = -1;
      writeLock.lock();
      HDFSEventWriter hdfsEventWriter = new HDFSEventWriter();
      try {
        accessProvider = new EventHdfsDumperRunStatsAccessProvider();
        EventHdfsDumperRunStats runStats = accessProvider.findById(1L);
        if (runStats == null) {
          runStats = new EventHdfsDumperRunStats();
          runStats.setId(1);
          runStats.setLastRunMillis(-1);
          runStats = accessProvider.create(runStats);
        } else {
          fetchFromMillis = runStats.getLastRunMillis();
        }
        // batching event model push
        long startTime = fetchFromMillis;
        while (startTime < fetchTillMillis) {
          long endTime = getEndTime(startTime, fetchTillMillis);
          List<EventModel> eventsToLoad = new EventModelAccessProvider().fetchByTime(startTime,
              endTime);
          if (eventsToLoad != null && !eventsToLoad.isEmpty()) {
            hdfsEventWriter.addEvents(eventsToLoad);
          }
          if (fetchFromMillis == -1)
            break;
          startTime = endTime;
        }

        runStats.setLastRunMillis(fetchTillMillis);
        accessProvider.update(runStats);
      } catch (Exception e) {
        throw new IllegalStateException(e);
      } finally {
        writeLock.unlock();
        hdfsEventWriter.stop();
      }
    }

    private long getEndTime(long startTime, long thresoldMilis) {
      TimeUnit granularity = EventEnvironment.INSTANCE.getArchiverBatchGranularity();
      long fequency = EventEnvironment.INSTANCE.getArchiverBatchFrecuency();
      long endTime = granularity.toMillis(fequency) + startTime;
      if (thresoldMilis < endTime)
        endTime = thresoldMilis;
      return endTime;
    }

  }
}
