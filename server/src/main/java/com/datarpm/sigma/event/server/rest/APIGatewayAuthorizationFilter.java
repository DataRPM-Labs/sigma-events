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

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@PreMatching
public class APIGatewayAuthorizationFilter implements ContainerRequestFilter {

  @Override
  public void filter(ContainerRequestContext context) throws IOException {

    if (context.getMethod().equalsIgnoreCase("options")) {
      return;
    }

    String consumerUserName = context.getHeaderString("x-consumer-username");
    if (consumerUserName == null || consumerUserName.isEmpty()) {
      StatusMessage statusMessage = new StatusMessage();
      statusMessage.setStatus(Status.FORBIDDEN.getStatusCode());
      statusMessage.setMessage("Access Denied for this functionality !!!");
      context.abortWith(
          Response.status(Status.FORBIDDEN.getStatusCode()).entity(statusMessage).build());
      return;
    }

    User user = new User(consumerUserName, "api");
    String scheme = "http";
    APISecurityContext securityContext = new APISecurityContext(scheme, user);
    context.setSecurityContext(securityContext);

    return;
  }

  public static class StatusMessage {

    private int status;
    private String message;

    public String getMessage() {
      return message;
    }

    public int getStatus() {
      return status;
    }

    public void setStatus(int status) {
      this.status = status;
    }

    public void setMessage(String message) {
      this.message = message;
    }

  }

}
