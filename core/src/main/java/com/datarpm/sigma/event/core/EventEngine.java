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
package com.datarpm.sigma.event.core;

import java.io.IOException;

import com.datarpm.sigma.event.core.channel.EventChannel;
import com.datarpm.sigma.event.core.channel.EventChannelFactory;

/**
 * @author vishal
 *
 */
public class EventEngine implements EventEngineDef {

  public static final EventEngine INSTANCE = new EventEngine();
  private EventChannel eventChannel;

  private EventEngine() {
    try {
      eventChannel = new EventChannelFactory().createChannel();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public void generateEvent(Event event) {
    eventChannel.publishEvent(event);
  }

  @Override
  public RegistryId addListner(final EventMatchFilter matchFilter, final EventCallBack callback) {
    RegistryId registryId = new RegistryId();
    eventChannel.registerListner(registryId, new EventCallBack() {
      @Override
      public void onEvent(Event event) {
        if (matchFilter.allow(event.getHeader())) {
          callback.onEvent(event);
        }
      }
    });
    return registryId;
  }

  @Override
  public void removeListner(RegistryId registryId) {
    eventChannel.removeListner(registryId);
  }

}
