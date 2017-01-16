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
package com.datarpm.sigma.event.server;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.servlet.DispatcherType;
import javax.ws.rs.core.Application;

import org.apache.cxf.jaxrs.servlet.CXFNonSpringJaxrsServlet;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.AbstractConnector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.datarpm.sigma.event.core.config.EventEngineConfig;
import com.datarpm.sigma.event.handler.EventEngineHandler;
import com.datarpm.sigma.event.server.rest.EventEngineRestApi;

/**
 * @author vinay
 *
 */
public class EventEngineServer extends Application {

  private static final Logger _logger = Logger.getLogger(EventEngineServer.class);

  public static void main(String[] args) throws Exception {

    EventEngineHandler.eventPersister().start();
    EventEngineHandler.eventArchiver().start();
    EventEngineHandler.eventPurger().start();

    final Server jettyServer = setupJettyServer();
    ServletContextHandler restApi = createAPIHandler();
    ContextHandlerCollection contexts = new ContextHandlerCollection();
    contexts.setHandlers(new Handler[] { restApi });
    jettyServer.setHandler(contexts);

    _logger.info("Start event-engine server");
    try {
      jettyServer.start();
    } catch (Exception e) {
      _logger.error("Error while running jettyServer", e);
      System.exit(-1);
    }
    _logger.info("Started event-engine server");

    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        _logger.info("Shutting down Event Engine Server ... ");
        EventEngineHandler.eventPersister().stop();
        EventEngineHandler.eventArchiver().stop();
        EventEngineHandler.eventPurger().stop();
        try {
          jettyServer.stop();
        } catch (Exception e) {
          _logger.error("Error while stopping servlet container", e);
        }
        _logger.info("Bye");
      }
    });

    jettyServer.join();
  }

  private static ServletContextHandler createAPIHandler() {
    final ServletHolder cxfServletHolder = new ServletHolder(new CXFNonSpringJaxrsServlet());
    cxfServletHolder.setInitParameter("javax.ws.rs.Application", EventEngineServer.class.getName());
    cxfServletHolder.setName("rest");
    cxfServletHolder.setForcedPath("rest");

    final ServletContextHandler cxfContext = new ServletContextHandler();
    cxfContext.setSessionHandler(new SessionHandler());
    cxfContext.setContextPath("/api");
    cxfContext.addServlet(cxfServletHolder, "/*");

    cxfContext.addFilter(new FilterHolder(CorsFilter.class), "/*",
        EnumSet.allOf(DispatcherType.class));
    return cxfContext;
  }

  @Override
  public Set<Object> getSingletons() {
    Set<Object> singletons = new HashSet<Object>();
    EventEngineRestApi eventEngineApi = new EventEngineRestApi();
    singletons.add(eventEngineApi);
    return singletons;
  }

  private static Server setupJettyServer() throws Exception {
    Properties serverProperties = EventEngineConfig.INSTANCE.getServerConfig();
    AbstractConnector connector = new SelectChannelConnector();
    // Set some timeout options to make debugging easier.
    int timeout = 1000 * 30;
    connector.setMaxIdleTime(timeout);
    connector.setSoLingerTime(-1);
    connector.setHost(serverProperties.getProperty("event.rest.server.host"));
    int port = Integer.parseInt(serverProperties.getProperty("event.rest.server.port"));
    connector.setPort(port);

    final Server server = new Server();
    server.addConnector(connector);

    return server;
  }
}