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

import com.datarpm.sigma.event.core.Event;

public class EventOpMessage {
  private EventOpCode opCode;
  private EventQuery query;
  private String registryId;
  private Event event;

  public EventOpCode getOpCode() {
    return opCode;
  }

  public void setOpCode(EventOpCode opCode) {
    this.opCode = opCode;
  }

  public EventQuery getQuery() {
    return query;
  }

  public void setQuery(EventQuery query) {
    this.query = query;
  }

  public String getRegistryId() {
    return registryId;
  }

  public void setRegistryId(String registryId) {
    this.registryId = registryId;
  }

  public Event getEvent() {
    return event;
  }

  public void setEvent(Event event) {
    this.event = event;
  }

}
