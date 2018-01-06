/**
 * 
 */
package com.datarpm.sigma.event.core;

import java.io.Serializable;

/**
 * @author vishal
 *
 */
public class EventDetail implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private EventType eventType;
  private SystemEventDetail systemEventDetail;
  private UserEventDetail userEventDetail;

  public SystemEventDetail getSystemEventDetail() {
    return systemEventDetail;
  }

  public void setSystemEventDetail(SystemEventDetail systemEventDetail) {
    this.eventType = EventType.System;
    this.systemEventDetail = systemEventDetail;
  }

  public UserEventDetail getUserEventDetail() {
    return userEventDetail;
  }

  public void setUserEventDetail(UserEventDetail userEventDetail) {
    this.eventType = EventType.User;
    this.userEventDetail = userEventDetail;
  }

  public EventType getEventType() {
    return eventType;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((eventType == null) ? 0 : eventType.hashCode());
    result = prime * result + ((systemEventDetail == null) ? 0 : systemEventDetail.hashCode());
    result = prime * result + ((userEventDetail == null) ? 0 : userEventDetail.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    EventDetail other = (EventDetail) obj;
    if (eventType != other.eventType)
      return false;
    if (systemEventDetail == null) {
      if (other.systemEventDetail != null)
        return false;
    } else if (!systemEventDetail.equals(other.systemEventDetail))
      return false;
    if (userEventDetail == null) {
      if (other.userEventDetail != null)
        return false;
    } else if (!userEventDetail.equals(other.userEventDetail))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "EventDetail [eventType=" + eventType + ", systemEventDetail=" + systemEventDetail
        + ", userEventDetail=" + userEventDetail + "]";
  }

}
