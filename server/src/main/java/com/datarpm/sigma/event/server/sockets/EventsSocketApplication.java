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
package com.datarpm.sigma.event.server.sockets;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.glassfish.grizzly.websockets.DataFrame;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketApplication;

import com.datarpm.sigma.event.core.EventEngine;
import com.datarpm.sigma.event.core.RegistryId;
import com.datarpm.sigma.event.server.sockets.EventSocketRegistry.RegistryInfo;
import com.google.gson.Gson;

public class EventsSocketApplication extends WebSocketApplication {

  private Map<WebSocket, EventSocketRegistry> socketRegistryMap;
  private ReentrantLock reentrantLock;

  public EventsSocketApplication() {
    socketRegistryMap = new ConcurrentHashMap<>();
    reentrantLock = new ReentrantLock();
  }

  @Override
  public void onMessage(WebSocket socket, String text) {
    if (inValidJsonText(text)) {
      return;
    }

    EventOpMessage opnCode = new Gson().fromJson(text, EventOpMessage.class);
    switch (opnCode.getOpCode()) {
    case REGISTER_CALLBACK: {
      registerCallback(socket, opnCode);
    }
      break;
    case UNREGISTER_CALLBACK: {
      unRegisterCallback(socket, opnCode);
    }
      break;
    case PING: {
      sendPong(socket);
    }
      break;

    default:
      break;
    }
  }

  private void sendPong(WebSocket socket) {
    EventOpMessage opMessage = new EventOpMessage();
    opMessage.setOpCode(EventOpCode.PONG);
    socket.send(new Gson().toJson(opMessage));
  }

  private void unRegisterCallback(WebSocket socket, EventOpMessage opnCode) {
    String uiRegistryId = opnCode.getRegistryId();
    reentrantLock.lock();
    try {
      EventSocketRegistry socketRegistry = socketRegistryMap.get(socket);
      if (socketRegistry == null) {
        return;
      }

      RegistryInfo registryInfo = socketRegistry.get(uiRegistryId);
      if (registryInfo == null) {
        return;
      }

      RegistryId serverRegistryId = registryInfo.getServerRegistryId();
      EventEngine.INSTANCE.removeListner(serverRegistryId);

    } finally {
      reentrantLock.unlock();
    }
  }

  private void registerCallback(WebSocket socket, EventOpMessage opnCode) {
    EventQuery query = opnCode.getQuery();
    String uiRegistryId = opnCode.getRegistryId();
    reentrantLock.lock();
    try {
      EventSocketRegistry socketRegistry = socketRegistryMap.get(socket);
      if (socketRegistry == null) {
        socketRegistry = new EventSocketRegistry();
        socketRegistryMap.put(socket, socketRegistry);
      }

      EventSocketCallbackFilter callbackFilter = new EventSocketCallbackFilter(uiRegistryId, query,
          socket);
      RegistryId serverRegistryId = EventEngine.INSTANCE.addListner(callbackFilter, callbackFilter);
      socketRegistry.put(uiRegistryId, new RegistryInfo(uiRegistryId, serverRegistryId));
    } finally {
      reentrantLock.unlock();
    }
  }

  private boolean inValidJsonText(String text) {
    return text == null || text.trim().isEmpty() || !text.trim().startsWith("{")
        || !text.trim().endsWith("}");
  }

  @Override
  public void onClose(WebSocket socket, DataFrame frame) {
    reentrantLock.lock();
    try {
      EventSocketRegistry socketRegistry = socketRegistryMap.remove(socket);
      if (socketRegistry == null) {
        return;
      }

      List<RegistryInfo> list = socketRegistry.getAll();
      if (list == null || list.isEmpty()) {
        return;
      }

      for (RegistryInfo registryInfo : list) {
        RegistryId serverRegistryId = registryInfo.getServerRegistryId();
        EventEngine.INSTANCE.removeListner(serverRegistryId);
      }

    } finally {
      reentrantLock.unlock();
    }
  }

}
