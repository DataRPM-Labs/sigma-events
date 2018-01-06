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
package com.datarpm.sigma.event.server.rest;

public class APIGatewayServiceInfo {
  private String serviceId;
  private String serviceURL;
  private boolean skipAuth;
  private String serviceHost;
  private int servicePort;
  private boolean purgeTargetsFromHost;

  public APIGatewayServiceInfo(String serviceId, String serviceURL, String serviceHost,
      int servicePort, boolean purgeTargetsFromHost) {
    super();
    this.serviceId = serviceId;
    this.serviceURL = serviceURL;
    this.serviceHost = serviceHost;
    this.servicePort = servicePort;
    this.purgeTargetsFromHost = purgeTargetsFromHost;
  }

  public String getServiceId() {
    return serviceId;
  }

  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }

  public String getServiceURL() {
    return serviceURL;
  }

  public void setServiceURL(String serviceURL) {
    this.serviceURL = serviceURL;
  }

  public boolean isSkipAuth() {
    return skipAuth;
  }

  public void setSkipAuth(boolean skipAuth) {
    this.skipAuth = skipAuth;
  }

  public String getServiceHost() {
    return serviceHost;
  }

  public void setServiceHost(String serviceHost) {
    this.serviceHost = serviceHost;
  }

  public int getServicePort() {
    return servicePort;
  }

  public void setServicePort(int servicePort) {
    this.servicePort = servicePort;
  }

  public boolean isPurgeTargetsFromHost() {
    return purgeTargetsFromHost;
  }

  public void setPurgeTargetsFromHost(boolean purgeTargetsFromHost) {
    this.purgeTargetsFromHost = purgeTargetsFromHost;
  }

}
