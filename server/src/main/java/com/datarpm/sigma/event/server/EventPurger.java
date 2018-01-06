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
package com.datarpm.sigma.event.server;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.datarpm.sigma.event.core.EventEnvironment;
import com.datarpm.sigma.event.model.EventHdfsDumperRunStats;
import com.datarpm.sigma.event.model.da.EventHdfsDumperRunStatsAccessProvider;
import com.datarpm.sigma.event.model.da.EventModelAccessProvider;

public class EventPurger {

  public static final EventPurger INSTANCE = new EventPurger();
  private static final long SCHEDULE_TIME = TimeUnit.HOURS.toMillis(1);
  private TimedHDFSDumpTask timerTask;
  private Timer timer;
  private ReentrantLock writeLock;

  private EventPurger() {
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
      long purgeOlderEventMillis = EventEnvironment.INSTANCE.getPurgeOlderEventMillis();
      long currentTimeMillis = System.currentTimeMillis();
      long purgeOlderThanMillis = currentTimeMillis - purgeOlderEventMillis;
      writeLock.lock();
      try {
        EventModelAccessProvider eventAccessProvider = new EventModelAccessProvider();
        if (EventEnvironment.INSTANCE.isArchiveEnabled()) {
          EventHdfsDumperRunStatsAccessProvider archiveRunStatsProvider = new EventHdfsDumperRunStatsAccessProvider();
          EventHdfsDumperRunStats runStats = archiveRunStatsProvider.findById(1L);
          if (runStats != null) {
            purgeOlderThanMillis = Math.min(runStats.getLastRunMillis(), purgeOlderThanMillis);
          }
        }

        eventAccessProvider.deleteByTime(purgeOlderThanMillis);
      } catch (Exception e) {
        throw new IllegalStateException(e);
      } finally {
        writeLock.unlock();
      }
    }

  }
}
