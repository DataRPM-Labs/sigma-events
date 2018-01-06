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

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

import com.datarpm.sigma.event.core.Event;
import com.datarpm.sigma.event.core.EventCallBack;
import com.datarpm.sigma.event.core.RegistryId;
import com.datarpm.sigma.event.core.conf.Configuration;
import com.datarpm.sigma.event.core.conf.ConfigurationReader;
import com.google.gson.Gson;

/**
 * @author vishal
 *
 */
class EventActiveMQChannel implements EventChannel, MessageListener {

  private static final int WORKER_THREAD_COUNT = 20;
  private static final int EVENT_QUEUE_CAPACITY = 20000;
  static EventActiveMQChannel INSTANCE = new EventActiveMQChannel();
  private Connection conn;
  private WriteLock registryWriteLock;
  private ReadLock registryReadLock;
  private Map<RegistryId, EventCallBack> registry;
  private MessageProducer producer;
  private Session session;
  private ExecutorService workerThreadPool;

  private EventActiveMQChannel() {
    try {
      Configuration configuration = ConfigurationReader.INSTANCE
          .readConfiguration("channel-site.xml");

      ReentrantReadWriteLock lockKeeper = new ReentrantReadWriteLock();
      registryWriteLock = lockKeeper.writeLock();
      registryReadLock = lockKeeper.readLock();

      registry = new ConcurrentHashMap<RegistryId, EventCallBack>();
      ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(
          configuration.get("events.channel.activemq.address", "tcp://127.0.0.1:61616"));
      conn = factory.createConnection();
      session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
      conn.start();

      producer = session.createProducer(
          session.createTopic(configuration.get("events.channel.activemq.topic", "event-stream")));
      producer.setDeliveryMode(DeliveryMode.PERSISTENT);

      MessageConsumer consumer = session.createConsumer(
          session.createTopic(configuration.get("events.channel.activemq.topic", "event-stream")));
      consumer.setMessageListener(this);

      initializeThreadPool(
          configuration.get("events.channel.activemq.worker.pool.size", WORKER_THREAD_COUNT + ""));
    } catch (Exception e) {
      throw new IllegalStateException("Event MQ Channel Initialization Failure", e);
    }
  }

  private void initializeThreadPool(String value) {
    int poolSize = Integer.parseInt(value);
    workerThreadPool = new ThreadPoolExecutor(poolSize, poolSize, 0L, TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue<Runnable>(EVENT_QUEUE_CAPACITY));
    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
      @Override
      public void run() {
        workerThreadPool.shutdown();
      }
    }, "event-engine-pool-shutdown"));
  }

  @Override
  public void publishEvent(Event event) {
    String json = new Gson().toJson(event);
    try {
      TextMessage message = session.createTextMessage(json);
      producer.send(message);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public void registerListner(RegistryId registryId, EventCallBack callback) {
    registryWriteLock.lock();
    try {
      registry.put(registryId, callback);
    } finally {
      registryWriteLock.unlock();
    }
  }

  @Override
  public void onMessage(Message message) {
    registryReadLock.lock();
    try {
      Event event = toEvent(message);
      for (Entry<RegistryId, EventCallBack> eachEntry : registry.entrySet()) {
        workerThreadPool.execute(new EventCallbackTask(eachEntry, event));
      }
    } finally {
      registryReadLock.unlock();
    }
  }

  private Event toEvent(Message message) {
    String text;
    try {
      text = ((TextMessage) message).getText();
    } catch (JMSException e) {
      throw new IllegalStateException(e);
    }
    return new Gson().fromJson(text, Event.class);
  }

  @Override
  public void removeListner(RegistryId registryId) {
    registryWriteLock.lock();
    try {
      registry.remove(registryId);
    } finally {
      registryWriteLock.unlock();
    }
  }

  class EventCallbackTask implements Runnable {

    private Entry<RegistryId, EventCallBack> callbackEntry;
    private Event event;

    public EventCallbackTask(Entry<RegistryId, EventCallBack> callbackEntry, Event event) {
      this.callbackEntry = callbackEntry;
      this.event = event;
    }

    @Override
    public void run() {
      registryReadLock.lock();
      try {
        RegistryId registryId = callbackEntry.getKey();
        if (!registry.containsKey(registryId)) {
          // Stale listener
          return;
        }
        callbackEntry.getValue().onEvent(event);
      } finally {
        registryReadLock.unlock();
      }
    }

  }

}
