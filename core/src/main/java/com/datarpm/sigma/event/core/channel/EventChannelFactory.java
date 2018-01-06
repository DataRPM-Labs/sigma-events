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
package com.datarpm.sigma.event.core.channel;

import java.io.IOException;

import com.datarpm.sigma.event.core.conf.Configuration;
import com.datarpm.sigma.event.core.conf.ConfigurationReader;

public class EventChannelFactory {
  private static final String CHANNEL_TYPE_PROPERTY_KEY = "events.channel";

  public EventChannel createChannel() throws IOException {
    Configuration configuration = ConfigurationReader.INSTANCE
        .readConfiguration("channel-site.xml");
    String channel = null;
    if (configuration == null || (channel = configuration.get(CHANNEL_TYPE_PROPERTY_KEY)) == null
        || "default".equalsIgnoreCase(channel)) {
      return EventEmbeddedActiveMQChannel.INSTANCE;
    }

    if ("activemq".equalsIgnoreCase(channel)) {
      return EventActiveMQChannel.INSTANCE;
    }

    return EventEmbeddedActiveMQChannel.INSTANCE;
  }
}
