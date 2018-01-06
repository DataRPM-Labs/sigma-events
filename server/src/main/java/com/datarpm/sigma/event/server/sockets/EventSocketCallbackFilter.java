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

import java.util.Map;
import java.util.Set;

import org.glassfish.grizzly.websockets.WebSocket;

import com.datarpm.sigma.event.core.Event;
import com.datarpm.sigma.event.core.EventCallBack;
import com.datarpm.sigma.event.core.EventHeader;
import com.datarpm.sigma.event.core.EventMatchFilter;
import com.google.gson.Gson;

public class EventSocketCallbackFilter implements EventCallBack, EventMatchFilter {

  private WebSocket socket;
  private EventQuery query;
  private String uiRegistryId;

  public EventSocketCallbackFilter(String uiRegistryId, EventQuery query, WebSocket socket) {
    this.query = query;
    this.socket = socket;
    this.uiRegistryId = uiRegistryId;
  }

  @Override
  public boolean allow(EventHeader eventHeader) {
    if (!query.getCode().equals(eventHeader.getCode())) {
      return false;
    }

    Map<String, String> queryHeaders = query.getHeaders();
    if (queryHeaders == null || queryHeaders.isEmpty()) {
      return true;
    }

    Map<String, String> eventHeaderMap = eventHeader.getHeaders();
    Set<String> queryKeys = queryHeaders.keySet();
    for (String eachQueryKey : queryKeys) {
      String headerCompare = queryHeaders.get(eachQueryKey);
      if (!eventHeaderMap.containsKey(eachQueryKey)) {
        return false;
      }

      if (!eventHeaderMap.get(eachQueryKey).equals(headerCompare)) {
        return false;
      }
    }

    return true;
  }

  @Override
  public void onEvent(Event event) {
    EventOpMessage eventOpMessage = new EventOpMessage();
    eventOpMessage.setEvent(event);
    eventOpMessage.setRegistryId(uiRegistryId);
    eventOpMessage.setOpCode(EventOpCode.CALLBACK);
    String jsonStr = new Gson().toJson(eventOpMessage);
    socket.send(jsonStr);
  }

}
