package com.datarpm.sigma.event.core.channel;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.activemq.broker.BrokerService;

import com.datarpm.sigma.event.core.conf.Configuration;
import com.datarpm.sigma.event.core.conf.ConfigurationReader;

public class EmbeddedActiveMQService {

  public static EmbeddedActiveMQService INSTANCE = new EmbeddedActiveMQService();

  private AtomicBoolean startedFlag = new AtomicBoolean(false);

  private BrokerService broker;

  private EmbeddedActiveMQService() {
  }

  public void startService() {
    if (startedFlag.get()) {
      return;
    }

    synchronized (INSTANCE) {
      if (startedFlag.get()) {
        return;
      }

      try {
        Configuration configuration = ConfigurationReader.INSTANCE
            .readConfiguration("channel-site.xml");
        String port = configuration.get("events.channel.default.port", "61616");
        broker = new BrokerService();
        broker.addConnector("tcp://localhost:" + port);
        broker.start();
        startedFlag.set(true);
      } catch (Exception e) {
        throw new IllegalStateException("Event MQ Channel Initialization Failure", e);
      }
    }
  }
}
