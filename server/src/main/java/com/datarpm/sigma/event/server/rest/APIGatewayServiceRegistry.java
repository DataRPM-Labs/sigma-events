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

import java.net.ConnectException;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class APIGatewayServiceRegistry {

  public static final APIGatewayServiceRegistry INSTANCE = new APIGatewayServiceRegistry();
  public static final String RDEFAULT_GATEWAY_ADMIN_URL = "http://localhost:8101/";
  public static final String RDEFAULT_GATEWAY_ADMIN_APIS_URL = RDEFAULT_GATEWAY_ADMIN_URL + "apis/";
  private static final int REGISTRATION_RETRY_SEC = 5;

  private APIGatewayServiceRegistry() {
  }

  public String getGatewayAdminUrl() {
    StringBuffer url = new StringBuffer();
    String host = System.getenv("GATEWAY_HTTP_HOST");
    if (host == null || host.trim().isEmpty()) {
      host = "127.0.0.1";
    }
    url.append("http://").append(host);

    String port = System.getenv("GATEWAY_ADMIN_PORT");
    if (port == null || port.trim().isEmpty()) {
      port = "8101";
    }

    url.append(":").append(port);
    return url.toString();
  }

  public void register(APIGatewayServiceInfo serviceInfo)
      throws APIGatewayServiceRegistrationFailure {

    boolean registrationComplete = false;
    while (!registrationComplete) {
      try {
        registerUpstreamURL(serviceInfo);
        if (!serviceRegistered(serviceInfo)) {
          createServiceRegistry(serviceInfo);
        }
        registrationComplete = true;
      } catch (ProcessingException | ConnectException e) {
        if (e instanceof ProcessingException && !(e.getCause() instanceof ConnectException)) {
          throw (ProcessingException) e;
        }
        System.out.println(
            "failed to connect to api-gateway, retry after " + REGISTRATION_RETRY_SEC + " sec ...");
        try {
          Thread.sleep(TimeUnit.SECONDS.toMillis(REGISTRATION_RETRY_SEC));
        } catch (InterruptedException e1) {
          throw new APIGatewayServiceRegistrationFailure(e);
        }
      }
    }
    System.out.println("service " + serviceInfo.getServiceId() + " registration with api-gateway successful !!");
  }

  private void registerUpstreamURL(APIGatewayServiceInfo serviceInfo)
      throws APIGatewayServiceRegistrationFailure, ConnectException {

    if (!isProdMode() || serviceInfo.isPurgeTargetsFromHost()) {
      if (isUpstreamURLRegistered(serviceInfo)) {
        purgeUpstreamURL(serviceInfo);
      }
      createUpstreamURL(serviceInfo);
    } else if (!isUpstreamURLRegistered(serviceInfo)) {
      createUpstreamURL(serviceInfo);
    }

    if (!isUpstreamTargetRegistered(serviceInfo)) {
      createUpstreamTarget(serviceInfo);
    }
  }

  private boolean isProdMode() {
    String runMode = System.getenv("DATARPM_RUN_MODE");
    return runMode != null && runMode.equalsIgnoreCase("prod");
  }

  private void purgeUpstreamURL(APIGatewayServiceInfo serviceInfo)
      throws APIGatewayServiceRegistrationFailure {
    String serviceId = serviceInfo.getServiceId();
    WebTarget webTarget = ClientBuilder.newClient()
        .target(getGatewayAdminUrl() + "/upstreams/" + serviceId);
    Response response = webTarget.request().delete();
    int status = response.getStatus();
    if (status != 204) {
      throw new APIGatewayServiceRegistrationFailure(response.readEntity(String.class));
    }
  }

  private void createUpstreamTarget(APIGatewayServiceInfo serviceInfo)
      throws APIGatewayServiceRegistrationFailure {
    String serviceTarget = serviceInfo.getServiceHost() + ":" + serviceInfo.getServicePort();
    String serviceId = serviceInfo.getServiceId();
    WebTarget webTarget = ClientBuilder.newClient()
        .target(getGatewayAdminUrl() + "/upstreams/" + serviceId + "/targets");
    Form form = new Form();
    form.param("target", serviceTarget);
    Response response = webTarget.request()
        .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED));

    int status = response.getStatus();
    if (status != 201) {
      throw new APIGatewayServiceRegistrationFailure(response.readEntity(String.class));
    }
  }

  private boolean isUpstreamTargetRegistered(APIGatewayServiceInfo serviceInfo)
      throws APIGatewayServiceRegistrationFailure {
    String serviceTarget = serviceInfo.getServiceHost() + ":" + serviceInfo.getServicePort();
    String serviceId = serviceInfo.getServiceId();
    WebTarget webTarget = ClientBuilder.newClient()
        .target(getGatewayAdminUrl() + "/upstreams/" + serviceId + "/targets/active");
    Response response = webTarget.request().get();
    int status = response.getStatus();
    if (status != 200) {
      throw new APIGatewayServiceRegistrationFailure(response.readEntity(String.class));
    }

    JsonObject jsonResponse = new JsonParser().parse(response.readEntity(String.class))
        .getAsJsonObject();
    int total = jsonResponse.get("total").getAsInt();
    if (total == 0) {
      return false;
    }

    JsonArray targets = jsonResponse.get("data").getAsJsonArray();
    for (JsonElement eachTargetElem : targets) {
      String target = eachTargetElem.getAsJsonObject().get("target").getAsString();
      if (serviceTarget.equals(target)) {
        return true;
      }
    }

    return false;
  }

  private boolean isUpstreamURLRegistered(APIGatewayServiceInfo serviceInfo) {
    String serviceId = serviceInfo.getServiceId();
    WebTarget webTarget = ClientBuilder.newClient()
        .target(getGatewayAdminUrl() + "/upstreams/" + serviceId);
    int status = webTarget.request().get().getStatus();
    return status == 200;
  }

  private void createUpstreamURL(APIGatewayServiceInfo serviceInfo)
      throws APIGatewayServiceRegistrationFailure {
    String serviceId = serviceInfo.getServiceId();
    WebTarget webTarget = ClientBuilder.newClient().target(getGatewayAdminUrl() + "/upstreams/");
    Form form = new Form();
    form.param("name", serviceId);
    Response response = webTarget.request()
        .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED));

    int status = response.getStatus();
    if (status != 201) {
      throw new APIGatewayServiceRegistrationFailure(response.readEntity(String.class));
    }
  }

  public ConsumerInfo consumerRead(String userId) {
    WebTarget webTarget = ClientBuilder.newClient()
        .target(getGatewayAdminUrl() + "/consumers/" + userId + "/jwt");
    Response response = webTarget.request().get();
    if (response == null) {
      throw new IllegalStateException("Connection failed to API Gateway");
    }

    if (response.getStatus() == 404) {
      return null;
    }

    JsonObject responseJson = new JsonParser().parse(response.readEntity(String.class))
        .getAsJsonObject();
    JsonArray tokens = responseJson.get("data").getAsJsonArray();
    if (tokens.size() == 0) {
      return null;
    }

    JsonObject tokenInfo = tokens.get(0).getAsJsonObject();
    String consumerId = tokenInfo.get("consumer_id").getAsString();
    long createdAt = tokenInfo.get("created_at").getAsLong();
    String id = tokenInfo.get("id").getAsString();
    String key = tokenInfo.get("key").getAsString();
    String secret = tokenInfo.get("secret").getAsString();
    return new ConsumerInfo(consumerId, createdAt, id, key, secret);
  }

  public boolean consumerExists(String userId) {
    WebTarget webTarget = ClientBuilder.newClient()
        .target(getGatewayAdminUrl() + "/consumers/" + userId + "/jwt");
    Response response = webTarget.request().get();
    if (response == null) {
      throw new IllegalStateException("Connection failed to API Gateway");
    }
    return (response.getStatus() == 200);
  }

  public boolean consumerDelete(String userId) {
    WebTarget webTarget = ClientBuilder.newClient()
        .target(getGatewayAdminUrl() + "/consumers/" + userId + "/jwt");
    Response response = webTarget.request().get();
    if (response == null) {
      throw new IllegalStateException("Connection failed to API Gateway");
    }
    JsonObject responseJson = new JsonParser().parse(response.readEntity(String.class))
        .getAsJsonObject();
    JsonArray tokens = responseJson.get("data").getAsJsonArray();
    for (JsonElement jsonElement : tokens) {
      String token = jsonElement.getAsJsonObject().get("id").getAsString();
      deleteJWT(userId, token);
    }

    webTarget = ClientBuilder.newClient().target(getGatewayAdminUrl() + "/consumers/" + userId);
    response = webTarget.request().delete();
    return (response.getStatus() == 200);
  }

  private void deleteJWT(String userId, String token) {
    WebTarget webTarget = ClientBuilder.newClient()
        .target(getGatewayAdminUrl() + "/consumers/" + userId + "/jwt/" + token);
    webTarget.request().delete();
  }

  public ConsumerInfo createConsumer(String userId) {

    if (consumerExists(userId)) {
      consumerDelete(userId);
    }

    WebTarget webTarget = ClientBuilder.newClient().target(getGatewayAdminUrl() + "/consumers");
    Form form = new Form();
    form.param("username", userId);
    Response response = webTarget.request()
        .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED));
    if (response.getStatus() != 201) {
      throw new IllegalStateException("API Gateway consumer create failed !!!!");
    }

    webTarget = ClientBuilder.newClient()
        .target(getGatewayAdminUrl() + "/consumers/" + userId + "/jwt");
    response = webTarget.request()
        .post(Entity.entity(new Form(), MediaType.APPLICATION_FORM_URLENCODED));
    if (response.getStatus() != 201) {
      throw new IllegalStateException("API Gateway consumer jwt registration failed !!!!");
    }

    JsonObject responseJson = new JsonParser().parse(response.readEntity(String.class))
        .getAsJsonObject();
    String consumerId = responseJson.get("consumer_id").getAsString();
    long createdAt = responseJson.get("created_at").getAsLong();
    String id = responseJson.get("id").getAsString();
    String key = responseJson.get("key").getAsString();
    String secret = responseJson.get("secret").getAsString();
    return new ConsumerInfo(consumerId, createdAt, id, key, secret);
  }

  public void deleteServiceRegistry(String serviceId) {
    WebTarget webTarget = ClientBuilder.newClient()
        .target(getGatewayAdminUrl() + "/apis/" + serviceId);
    Response response = webTarget.request().delete();
    if (!isSuccess(response)) {
      throw new IllegalStateException("API Gateway registration failed !!!!");
    }
  }

  private void createServiceRegistry(APIGatewayServiceInfo serviceInfo)
      throws APIGatewayServiceRegistrationFailure {
    Form form = new Form();
    form.param("name", serviceInfo.getServiceId());
    form.param("uris", "/" + serviceInfo.getServiceId());
    form.param("upstream_url", serviceInfo.getServiceURL());

    WebTarget webTarget = ClientBuilder.newClient().target(getGatewayAdminUrl() + "/apis");
    Response response = webTarget.request()
        .put(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED));
    if (!isSuccess(response)) {
      throw new APIGatewayServiceRegistrationFailure(response.readEntity(String.class));
    }

    if (serviceInfo.isSkipAuth()) {
      return;
    }

    webTarget = ClientBuilder.newClient()
        .target(getGatewayAdminUrl() + "/apis/" + serviceInfo.getServiceId() + "/plugins");
    Form pluginForm = new Form();
    pluginForm.param("name", "jwt");
    Response pluginResponse = webTarget.request()
        .post(Entity.entity(pluginForm, MediaType.APPLICATION_FORM_URLENCODED));
    if (pluginResponse.getStatus() != 201) {
      throw new APIGatewayServiceRegistrationFailure(response.readEntity(String.class));
    }
  }

  private boolean isSuccess(Response response) {
    if (response == null)
      return false;

    int status = response.getStatus();
    if (status >= 200 && response.getStatus() <= 226) {
      return true;
    }

    return false;
  }

  private boolean serviceRegistered(APIGatewayServiceInfo serviceInfo)
      throws APIGatewayServiceRegistrationFailure {
    WebTarget webTarget = ClientBuilder.newClient()
        .target(getGatewayAdminUrl() + "/apis/" + serviceInfo.getServiceId());
    Response response = webTarget.request().get();
    if (response == null) {
      throw new APIGatewayServiceRegistrationFailure("Connection failed to API Gateway");
    }
    return (response.getStatus() == 200);
  }

}
