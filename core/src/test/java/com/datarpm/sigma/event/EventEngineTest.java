/*******************************************************************************
 * Copyright 2017 DataRPM
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
/**
 * 
 */
package com.datarpm.sigma.event;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import com.datarpm.sigma.event.core.Event;
import com.datarpm.sigma.event.core.Event.EventBuilder;
import com.datarpm.sigma.event.core.EventCallBack;
import com.datarpm.sigma.event.core.EventDetail;
import com.datarpm.sigma.event.core.EventEngine;
import com.datarpm.sigma.event.core.EventHeader;
import com.datarpm.sigma.event.core.EventMatchFilter;
import com.datarpm.sigma.event.core.EventType;
import com.datarpm.sigma.event.core.SystemEventDetail;
import com.datarpm.sigma.event.core.UserEventDetail;
import com.datarpm.sigma.event.core.channel.EmbeddedActiveMQService;

import junit.framework.TestCase;

/**
 * @author vishal
 *
 */
public class EventEngineTest extends TestCase {

  @Override
  protected void setUp() throws Exception {
    EmbeddedActiveMQService.INSTANCE.startService();
  }

  public void testSystemEventPublish() throws InterruptedException {
    final AtomicBoolean eventMatched = new AtomicBoolean(false);
    EventMatchFilter matchFilter = new EventMatchFilter() {

      @Override
      public boolean allow(EventHeader eventHeader) {
        return eventHeader.getEventType() == EventType.System;
      }
    };

    EventEngine.INSTANCE.addListner(matchFilter, new EventCallBack() {

      @Override
      public void onEvent(Event event) {
        Map<String, String> headers = event.getHeader().getHeaders();
        String eventId = headers.get("event-id");
        if (eventId == null || !eventId.equals(eventId)) {
          eventMatched.set(false);
        } else {
          eventMatched.set(true);
        }
      }
    });

    String code = "Test-Event-1";
    EventBuilder eventBuilder = new Event.EventBuilder(code);
    String eventId = UUID.randomUUID().toString();
    eventBuilder.addHeader("event-id", eventId);
    eventBuilder.generateSystemEvent(new SystemEventDetail("eywa", "loader", "IA_INDEX_FLUSH"));
    Event event = eventBuilder.toEvent();
    EventEngine.INSTANCE.generateEvent(event);

    Thread.sleep(2000);

    assertTrue(eventMatched.get());
    EventDetail eventDetail = event.getEventDetail();
    SystemEventDetail systemEventDetail = eventDetail.getSystemEventDetail();
    System.out.println("Process-Details : {processid = " + systemEventDetail.getProcessId()
        + ", processname = " + systemEventDetail.getProcessName() + ", vmdetails = "
        + systemEventDetail.getVmDetail() + "}");
  }

  public void testUserEventPublish() throws InterruptedException {

    final AtomicBoolean eventMatched = new AtomicBoolean(false);
    EventMatchFilter matchFilter = new EventMatchFilter() {

      @Override
      public boolean allow(EventHeader eventHeader) {
        return eventHeader.getEventType() == EventType.User;
      }
    };

    EventEngine.INSTANCE.addListner(matchFilter, new EventCallBack() {

      @Override
      public void onEvent(Event event) {
        Map<String, String> headers = event.getHeader().getHeaders();
        String eventId = headers.get("event-id");
        if (eventId == null || !eventId.equals(eventId)) {
          eventMatched.set(false);
        } else {
          eventMatched.set(true);
        }
      }
    });

    String code = "Test-Event-1";
    EventBuilder eventBuilder = new Event.EventBuilder(code);
    String eventId = UUID.randomUUID().toString();
    eventBuilder.addHeader("event-id", eventId);
    eventBuilder.generateUserEvent(
        new UserEventDetail("userId", "Mozilla", "192.168.0.1", "account/report"));
    Event event = eventBuilder.toEvent();
    EventEngine.INSTANCE.generateEvent(event);

    Thread.sleep(2000);

    assertTrue(eventMatched.get());
    EventDetail eventDetail = event.getEventDetail();
    UserEventDetail userEventDetail = eventDetail.getUserEventDetail();
    System.out.println("User Event : {browser = " + userEventDetail.getUserAgent() + ", userId = "
        + userEventDetail.getUserId() + ", action = " + userEventDetail.getActionUrl() + "}");
  }
  
}
