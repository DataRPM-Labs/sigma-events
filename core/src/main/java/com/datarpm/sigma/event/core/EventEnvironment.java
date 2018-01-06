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
package com.datarpm.sigma.event.core;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import com.datarpm.sigma.event.core.conf.Configuration;
import com.datarpm.sigma.event.core.conf.ConfigurationReader;

/**
 * @author vishal
 *
 */
public class EventEnvironment {

  public static final EventEnvironment INSTANCE = new EventEnvironment();
  private static final long DEFAULT_PURGE_BY_MILLIS = TimeUnit.DAYS.toMillis(30);

  private boolean archiveEnabled;

  private EventEnvironment() {
    loadConfig();
  }

  private void loadConfig() {
    Configuration storageConfig = null;
    try {
      storageConfig = ConfigurationReader.INSTANCE.readConfiguration("storage-site.xml");
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }

    archiveEnabled = Boolean.parseBoolean(storageConfig.get("events.archive.enable", "false"));
  }

  public Properties getArchiverConfig() {
    Configuration storageConfig = null;
    try {
      storageConfig = ConfigurationReader.INSTANCE.readConfiguration("storage-site.xml");
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
    Properties properties = new Properties();
    String hdfsAddress = storageConfig.get("events.archive.hdfs.address", "hdfs://127.0.0.1:8020");
    String archiverPath = storageConfig.get("events.archive.hdfs.path",
        "/user/datarpm/data/events/");

    properties.put("events.archive.hdfs.address", hdfsAddress);
    properties.put("events.archive.hdfs.path", archiverPath);

    properties.list(System.out);
    return properties;
  }

  public long getPurgeOlderEventMillis() {
    Configuration storageConfig = null;
    try {
      storageConfig = ConfigurationReader.INSTANCE.readConfiguration("storage-site.xml");
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
    String millisStr = storageConfig.get("event.purge.older.millis", DEFAULT_PURGE_BY_MILLIS + "");
    return Long.parseLong(millisStr);
  }

  public TimeUnit getArchiverBatchGranularity() {
    Configuration storageConfig = null;
    try {
      storageConfig = ConfigurationReader.INSTANCE.readConfiguration("storage-site.xml");
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }

    String granularity = storageConfig.get("events.archive.batch.granularity", "hours");
    switch (granularity.toLowerCase()) {
    case "hours":
      return TimeUnit.HOURS;

    case "days":
      return TimeUnit.DAYS;

    case "minutes":
      return TimeUnit.MINUTES;
    default:
      return TimeUnit.HOURS;
    }
  }

  public long getArchiverBatchFrecuency() {
    Configuration storageConfig = null;
    try {
      storageConfig = ConfigurationReader.INSTANCE.readConfiguration("storage-site.xml");
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
    String frequency = storageConfig.get("events.archive.batch.frequency", "1");
    return Long.valueOf(frequency);
  }

  public boolean isArchiveEnabled() {
    return archiveEnabled;
  }

  public void setArchiveEnabled(boolean archiveEnabled) {
    this.archiveEnabled = archiveEnabled;
  }

}
