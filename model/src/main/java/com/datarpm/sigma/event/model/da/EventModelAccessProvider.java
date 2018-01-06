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
package com.datarpm.sigma.event.model.da;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;

import com.datarpm.sigma.event.model.EventDetailModel;
import com.datarpm.sigma.event.model.EventHeaderModel;
import com.datarpm.sigma.event.model.EventModel;

public class EventModelAccessProvider extends QueryBuilder<EventModel> {

  public EventModelAccessProvider() {
    super(EventModel.class);
  }

  @Override
  public EventModel create(EventModel entity) throws NotSupportedException, SystemException {
    EventHeaderModel header = entity.getHeader();
    if (header.getId() == null) {
      header = new EventHeaderModelAccessProvider().create(header);
      entity.setHeader(header);
    }

    EventDetailModel eventDetail = entity.getEventDetail();
    if (eventDetail != null) {
      if (eventDetail.getId() == null) {
        eventDetail = new EventDetailModelAccessProvider().create(eventDetail);
        entity.setEventDetail(eventDetail);
      }
    }

    return super.create(entity);
  }

  public List<EventModel> fetchByTime(long startTime, long endTime) {
    EntityManager entityManager = emf.createEntityManager();
    TypedQuery<EventModel> createQuery = entityManager.createQuery(
        "FROM EventModel WHERE updatedAt BETWEEN :startTime AND :endTime", EventModel.class);
    createQuery.setParameter("startTime", new Date(startTime));
    createQuery.setParameter("endTime", new Date(endTime));
    return createQuery.getResultList();
  }

  public void deleteByTime(long purgeOlderThanMillis)
      throws NotSupportedException, SystemException {

    List<EventModel> deleteBatch = fetchByTime(-1, purgeOlderThanMillis);
    if (deleteBatch == null || deleteBatch.isEmpty()) {
      return;
    }

    for (EventModel eventModel : deleteBatch) {
      delete(eventModel);
    }
  }

}
