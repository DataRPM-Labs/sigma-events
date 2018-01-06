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
package com.datarpm.sigma.event.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.websockets.WebSocketAddOn;
import org.glassfish.grizzly.websockets.WebSocketEngine;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import com.datarpm.sigma.event.core.conf.Configuration;
import com.datarpm.sigma.event.core.conf.ConfigurationReader;
import com.datarpm.sigma.event.server.rest.APIGatewayAuthorizationFilter;
import com.datarpm.sigma.event.server.rest.APIGatewayServiceInfo;
import com.datarpm.sigma.event.server.rest.APIGatewayServiceRegistrationFailure;
import com.datarpm.sigma.event.server.rest.APIGatewayServiceRegistry;
import com.datarpm.sigma.event.server.rest.GsonMessageBodyHandler;
import com.datarpm.sigma.event.server.sockets.EventsSocketApplication;

public class EventEngineAPIServiceStarter {

  private static final long SOCKET_TO_CLOSE_TIME_WAIT = TimeUnit.SECONDS.toMillis(1);

  private String serverAccessURL;
  private int servicePort;
  private int websocketServicePort;
  private String serviceHost;

  private boolean authEnabled;

  public void start() throws IOException, APIGatewayServiceRegistrationFailure {
    Configuration configuration = ConfigurationReader.INSTANCE.readConfiguration("core-site.xml");
    authEnabled = Boolean.parseBoolean(
        configuration.get("events.server.gateway.kong.authentication.enabled", "false"));
    startServer();
    System.out.println(String.format(
        "Jersey app started with WADL available at " + "%s/application.wadl", serverAccessURL));
    boolean gatewayRegistration = Boolean
        .parseBoolean(configuration.get("events.server.gateway.kong.enabled", "false"));
    if (gatewayRegistration) {
      APIGatewayServiceRegistry.INSTANCE.register(prepareServiceInfo());
      APIGatewayServiceRegistry.INSTANCE.register(prepareWebsocketServiceInfo());
    }
  }

  private APIGatewayServiceInfo prepareWebsocketServiceInfo() {
    String serviceId = "event.service.websocket";
    String serviceUpstreamURL = "http://" + serviceId;
    APIGatewayServiceInfo serviceInfo = new APIGatewayServiceInfo(serviceId, serviceUpstreamURL,
        serviceHost, websocketServicePort, false);
    serviceInfo.setSkipAuth(!authEnabled);
    return serviceInfo;
  }

  private APIGatewayServiceInfo prepareServiceInfo() {
    String serviceId = "event.service";
    String serviceUpstreamURL = "http://" + serviceId + "/api";
    APIGatewayServiceInfo serviceInfo = new APIGatewayServiceInfo(serviceId, serviceUpstreamURL,
        serviceHost, servicePort, false);
    serviceInfo.setSkipAuth(!authEnabled);
    return serviceInfo;
  }

  private void startServer() throws IOException {
    // create a resource config that scans for JAX-RS resources and
    // providers
    // in com.datarpm.api package
    ResourceConfig rc = new ResourceConfig();
    Set<String> packagesToScan = new HashSet<>();
    packagesToScan.add("com.datarpm.event.server.rest");
    rc.packages(packagesToScan.toArray(new String[packagesToScan.size()]));
    rc.register(GsonMessageBodyHandler.class);
    rc.register(RolesAllowedDynamicFeature.class);
    if (authEnabled)
      rc.register(APIGatewayAuthorizationFilter.class);

    initializeService();
    // create and start a new instance of grizzly http server
    // exposing the Jersey application at BASE_URI
    final HttpServer httpServer = GrizzlyHttpServerFactory
        .createHttpServer(URI.create(serverAccessURL), rc);

    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
      @Override
      public void run() {
        httpServer.shutdownNow();
      }
    }, "Server-Showdown-Thread"));

    registerWebsocket();
  }

  private void registerWebsocket() throws IOException {
    Configuration configuration = ConfigurationReader.INSTANCE.readConfiguration("core-site.xml");
    websocketServicePort = Integer
        .parseInt(configuration.get("events.server.websocket.port", "8082"));
    try {
      ServerSocket s = new ServerSocket(websocketServicePort);
      websocketServicePort = s.getLocalPort();
      s.close();
      waitTillSocketIsClosed(s);
      String webSocketserviceHost = java.net.InetAddress.getLocalHost().getHostAddress();
      final HttpServer server = HttpServer.createSimpleServer(null, webSocketserviceHost,
          websocketServicePort);
      for (NetworkListener listener : server.getListeners()) {
        listener.registerAddOn(new WebSocketAddOn());
      }
      WebSocketEngine.getEngine().register("", "/event-stream", new EventsSocketApplication());
      server.start();

      Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
        @Override
        public void run() {
          server.shutdownNow();
        }
      }, "Server-Showdown-Thread"));

    } catch (IOException | InterruptedException e) {
      throw new IllegalStateException("Failed to start api-service. name = events.service", e);
    }
  }

  private void initializeService() {
    try {
      Configuration configuration = ConfigurationReader.INSTANCE.readConfiguration("core-site.xml");
      int port = Integer.parseInt(configuration.get("events.server.rest.port", "8081"));
      ServerSocket s = new ServerSocket(port);
      servicePort = s.getLocalPort();
      s.close();
      waitTillSocketIsClosed(s);
      serviceHost = java.net.InetAddress.getLocalHost().getHostAddress();
      StringBuffer uri = new StringBuffer("http://").append(serviceHost).append(":")
          .append(servicePort).append("/api");
      serverAccessURL = uri.toString();
    } catch (IOException | InterruptedException e) {
      throw new IllegalStateException("Failed to start api-service. name = events.service", e);
    }
  }

  private void waitTillSocketIsClosed(ServerSocket s) throws InterruptedException {
    while (!s.isClosed()) {
      System.out.println("waiting for socket to get free");
      Thread.sleep(SOCKET_TO_CLOSE_TIME_WAIT);
    }
  }

}
