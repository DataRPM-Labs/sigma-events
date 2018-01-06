/**
 * 
 */
package com.datarpm.sigma.event.core;

import java.io.Serializable;

/**
 * @author vishal
 *
 */
public class UserEventDetail implements Serializable {

  private static final long serialVersionUID = 1L;
  private String userId;
  private String userAgent;
  private String ipAddress;
  private String actionUrl;

  public UserEventDetail() {
  }

  public UserEventDetail(String userId, String userAgent, String ipAddress, String actionUrl) {
    super();
    this.userId = userId;
    this.userAgent = userAgent;
    this.ipAddress = ipAddress;
    this.actionUrl = actionUrl;
  }

  public String getActionUrl() {
    return actionUrl;
  }

  public void setActionUrl(String actionUrl) {
    this.actionUrl = actionUrl;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  @Override
  public String toString() {
    return "UserEventDetail [userId=" + userId + ", userAgent=" + userAgent + ", ipAddress="
        + ipAddress + ", actionUrl=" + actionUrl + "]";
  }

  public String getUserAgent() {
    return userAgent;
  }

  public void setUserAgent(String userAgent) {
    this.userAgent = userAgent;
  }

}
