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
package com.datarpm.sigma.event.model;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;

import com.datarpm.sigma.event.core.Event;
import com.datarpm.sigma.event.core.Event.EventBuilder;
import com.datarpm.sigma.event.core.SystemEventDetail;
import com.datarpm.sigma.event.model.da.EventModelAccessProvider;

import junit.framework.TestCase;

public class TestEventPersist extends TestCase {
    
  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }
  
  public void testSave() throws NotSupportedException, SystemException {

    EventModelAccessProvider accessProvider = new EventModelAccessProvider();
    
    EventBuilder eventBuilder = new Event.EventBuilder("ETL_DATASET_LOAD");
    String eventId = UUID.randomUUID().toString();
    eventBuilder.addHeader("eventId", eventId);
    eventBuilder.addParam("eventId", eventId);
    SystemEventDetail systemEventDetails = new SystemEventDetail();
    eventBuilder.generateSystemEvent(systemEventDetails);
    Event event = eventBuilder.toEvent();
    
    EventModel eventModel = new EventModel(event);
    EventModel persistedEvent = accessProvider.create(eventModel);
    assertNotNull(persistedEvent);
    assertNotNull(persistedEvent.getId());

    EventModel persistedEventModel = accessProvider.findById(persistedEvent.getId());
    assertNotNull(persistedEventModel);
    assertNotNull(persistedEventModel.getHeader());
    assertEquals(persistedEventModel.getHeader().getHeaders().get("eventId"), eventId);

    accessProvider.deleteById(persistedEventModel.getId());

    EventModel deletedModel = accessProvider.findById(persistedEventModel.getId());
    assertNull(deletedModel);
  }

  public void testQueryByTime() throws NotSupportedException, SystemException, InterruptedException {

    long time1 = System.currentTimeMillis();
    EventModelAccessProvider accessProvider = new EventModelAccessProvider();

    EventBuilder eventBuilder = new Event.EventBuilder("TEST_QUERY_BY_TIME");
    String eventId = UUID.randomUUID().toString();
    eventBuilder.addHeader("eventId", eventId);
    eventBuilder.addParam("time", time1 + "");
    SystemEventDetail systemEventDetails = new SystemEventDetail();
    eventBuilder.generateSystemEvent(systemEventDetails);
    Event event = eventBuilder.toEvent();
    EventModel eventModel = new EventModel(event);
    accessProvider.create(eventModel);

    long time2 = System.currentTimeMillis();

    eventBuilder = new Event.EventBuilder("TEST_QUERY_BY_TIME");
    eventId = UUID.randomUUID().toString();
    eventBuilder.addHeader("eventId", eventId);
    eventBuilder.addParam("time", time2 + "");
    eventBuilder.generateSystemEvent(new SystemEventDetail());
    event = eventBuilder.toEvent();
    eventModel = new EventModel(event);
    accessProvider.create(eventModel);

    eventBuilder = new Event.EventBuilder("TEST_QUERY_BY_TIME");
    eventId = UUID.randomUUID().toString();
    eventBuilder.addHeader("eventId", eventId);
    eventBuilder.addParam("time", time2 + "");
    eventBuilder.generateSystemEvent(new SystemEventDetail());
    event = eventBuilder.toEvent();
    eventModel = new EventModel(event);
    accessProvider.create(eventModel);
    long time3 = System.currentTimeMillis();
    
    Thread.sleep(TimeUnit.SECONDS.toMillis(10));
    
    List<EventModel> time1Batch = accessProvider.fetchByTime(time1, time2);
    assertNotNull(time1Batch);
    assertEquals(1, time1Batch.size());

    List<EventModel> time2Batch = accessProvider.fetchByTime(time2, time3);
    assertNotNull(time2Batch);
    assertEquals(2, time2Batch.size());

    long deleteByTime = System.currentTimeMillis();
    accessProvider.deleteByTime(deleteByTime);

    List<EventModel> noDataBatch = accessProvider.fetchByTime(time1, deleteByTime);
    assertNotNull(noDataBatch);
    assertEquals(0, noDataBatch.size());

  }

}
