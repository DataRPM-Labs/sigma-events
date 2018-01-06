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

import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;

import com.datarpm.sigma.event.core.Event;
import com.datarpm.sigma.event.core.EventCallBack;
import com.datarpm.sigma.event.core.EventEngine;
import com.datarpm.sigma.event.core.EventHeader;
import com.datarpm.sigma.event.core.EventMatchFilter;
import com.datarpm.sigma.event.model.EventModel;
import com.datarpm.sigma.event.model.da.EventModelAccessProvider;

public class EventPersister {
  public static final EventPersister INSTANCE = new EventPersister();
  private EventModelAccessProvider modelAccessProvider;

  private EventPersister() {
    modelAccessProvider = new EventModelAccessProvider();
  }

  public void start() {
    EventEngine.INSTANCE.addListner(new MatchAllFilter(), new EventPersistCallback());
  }

  private class MatchAllFilter implements EventMatchFilter {
    @Override
    public boolean allow(EventHeader eventHeader) {
      return !eventHeader.isSkipPersistence();
    }
  }

  private class EventPersistCallback implements EventCallBack {

    @Override
    public void onEvent(Event event) {
      EventModel eventModel = new EventModel(event);
      //eventModel.setId(event.getId());
      try {
        modelAccessProvider.create(eventModel);
      } catch (NotSupportedException | SystemException e) {
        throw new IllegalStateException(e);
      }
    }

  }

  public void stop() {

  }
}
