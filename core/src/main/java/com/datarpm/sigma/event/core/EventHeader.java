/**
 * 
 */
package com.datarpm.sigma.event.core;

import java.util.HashMap;
import java.util.Map;

/**
 * @author vishal
 *
 */
public class EventHeader {

  private String code;
  private boolean skipPersistence;
  private long timeStamp;
  private EventType eventType;
  private Map<String, String> headers;

  public EventHeader(String code, long timeStamp, EventType eventType) {
    this();
    this.code = code;
    this.timeStamp = timeStamp;
    this.eventType = eventType;
  }

  public EventHeader() {
    this.headers = new HashMap<String, String>();
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  public void addHeader(String headerKey, String headerValue) {
    headers.put(headerKey, headerValue);
  }

  public boolean equalsHeader(String headerKey, String headerValue) {
    if (!headers.containsKey(headerKey)) {
      return false;
    }

    String headerValueToCompare = headers.get(headerKey);
    return headerValueToCompare.equals(headerValue);
  }

  public String getHeader(String headerKey) {
    return headers.get(headerKey);
  }

  public long getTimeStamp() {
    return timeStamp;
  }

  public void setTimeStamp(long timeStamp) {
    this.timeStamp = timeStamp;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public EventType getEventType() {
    return eventType;
  }

  public void setEventType(EventType eventType) {
    this.eventType = eventType;
  }

  @Override
  public String toString() {
    return "EventHeader [code=" + code + ", timeStamp=" + timeStamp + ", eventType=" + eventType
        + "]";
  }

  public boolean isSkipPersistence() {
    return skipPersistence;
  }

  public void setSkipPersistence(boolean skipPersistence) {
    this.skipPersistence = skipPersistence;
  }

}
