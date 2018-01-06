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

import java.security.Principal;

import javax.ws.rs.core.SecurityContext;

public class APISecurityContext implements SecurityContext {
  private static final boolean ALLOW_ALL_APIS = true;
  private String scheme;
  private User user;

  public APISecurityContext(String scheme, User user) {
    super();
    this.scheme = scheme;
    this.user = user;
  }

  @Override
  public boolean isUserInRole(String role) {
    String userRole = user.getRole();
    if (userRole.equals("internal")) {
      return ALLOW_ALL_APIS;
    }
    return (userRole.equals(role));
  }

  @Override
  public Principal getUserPrincipal() {
    return user;
  }

  @Override
  public String getAuthenticationScheme() {
    return SecurityContext.BASIC_AUTH;
  }

  @Override
  public boolean isSecure() {
    return "https".equals(this.scheme);
  }

}
