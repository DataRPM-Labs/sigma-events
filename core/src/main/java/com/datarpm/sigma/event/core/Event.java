/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
package com.datarpm.sigma.event.core;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author vishal
 *
 */
public class Event {

  private String id;
  private EventHeader header;
  private EventDetail eventDetail;
  private Map<String, String> params;

  Event() {
    this.id = UUID.randomUUID().toString();
    this.header = new EventHeader();
    this.header.setTimeStamp(System.currentTimeMillis());
    params = new HashMap<String, String>();
  }

  Event(String code) {
    this();
    this.header.setCode(code);
  }

  public static EventBuilder builder(String eventCode) {
    return new EventBuilder(eventCode);
  }

  public String getId() {
    return id;
  }

  public String getCode() {
    return this.header.getCode();
  }

  public long getTimeStamp() {
    return this.header.getTimeStamp();
  }

  public EventHeader getHeader() {
    return header;
  }

  public EventDetail getEventDetail() {
    return eventDetail;
  }

  public Map<String, String> getParams() {
    return params;
  }

  /**
   * @author vishal
   *
   */
  public static class EventBuilder {

    private Event event;

    public EventBuilder(String code) {
      this.event = new Event(code);
    }

    public EventBuilder generateSystemEvent(SystemEventDetail systemEventDetails) {
      EventDetail localeventDetail = new EventDetail();
      localeventDetail.setSystemEventDetail(systemEventDetails);
      event.eventDetail = localeventDetail;
      event.header.setEventType(EventType.System);
      return this;
    }

    public EventBuilder addHeader(String headerKey, String headerValue) {
      event.header.addHeader(headerKey, headerValue);
      return this;
    }

    public EventBuilder generateUserEvent(UserEventDetail userEventDetail) {
      EventDetail localeventDetail = new EventDetail();
      localeventDetail.setUserEventDetail(userEventDetail);
      event.eventDetail = localeventDetail;
      event.header.setEventType(EventType.User);
      return this;
    }

    public EventBuilder addParam(String paramKey, String paramValue) {
      event.params.put(paramKey, paramValue);
      return this;
    }

    public void skipPersistence() {
      event.header.setSkipPersistence(true);
    }

    public Event toEvent() {
      return this.event;
    }

    public void fireEvent() {
      EventEngine.INSTANCE.generateEvent(event);
    }
  }

  @Override
  public String toString() {
    return "Event [id=" + id + ", header=" + header + ", eventDetail=" + eventDetail + "]";
  }
}