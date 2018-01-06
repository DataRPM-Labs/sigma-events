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

import org.apache.activemq.broker.BrokerService;
import org.apache.log4j.Logger;

import com.datarpm.sigma.event.core.EventEnvironment;
import com.datarpm.sigma.event.core.conf.Configuration;
import com.datarpm.sigma.event.core.conf.ConfigurationReader;

public class EventEngineServer {

  private static final Logger _logger = Logger.getLogger(EventEngineServer.class);

  public static void main(String[] args) throws Exception {

    Configuration configuration = ConfigurationReader.INSTANCE
        .readConfiguration("channel-site.xml");
    String channelType = configuration.get("events.channel", "default");
    if ("default".equalsIgnoreCase(channelType)) {
      BrokerService broker = new BrokerService();
      broker.addConnector("tcp://localhost:61616");
      broker.start();
    }

    EventPersister.INSTANCE.start();
    if (EventEnvironment.INSTANCE.isArchiveEnabled()) {
      EventArchiver.INSTANCE.start();
    }
    EventPurger.INSTANCE.start();

    _logger.info("Start event-engine api service");
    new EventEngineAPIServiceStarter().start();
    _logger.info("Started event-engine api service");

    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        _logger.info("Shutting down Event Engine Server ... ");
        EventPersister.INSTANCE.stop();
        EventArchiver.INSTANCE.stop();
        EventPurger.INSTANCE.stop();
      }
    });
  }

}
