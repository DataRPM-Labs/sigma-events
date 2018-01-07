package com.datarpm.sigma.event.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.GenericGenerator;

import com.datarpm.sigma.event.core.UserEventDetail;

@Entity
public class UserEventDetailModel {
  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  private String id;

  private String userId;
  private String userAgent;
  private String ipAddress;
  private String actionUrl;

  public UserEventDetailModel() {
  }

  public UserEventDetailModel(UserEventDetail detail) {
    if (detail == null) {
      return;
    }
    this.actionUrl = detail.getActionUrl();
    this.ipAddress = detail.getIpAddress();
    this.userAgent = detail.getUserAgent();
    this.userId = detail.getUserId();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getUserAgent() {
    return userAgent;
  }

  public void setUserAgent(String userAgent) {
    this.userAgent = userAgent;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public String getActionUrl() {
    return actionUrl;
  }

  public void setActionUrl(String actionUrl) {
    this.actionUrl = actionUrl;
  }

}
