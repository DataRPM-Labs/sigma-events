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