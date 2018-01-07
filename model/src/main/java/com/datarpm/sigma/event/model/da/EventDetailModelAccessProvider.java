/*******************************************************************************
 * Copyright 2017 DataRPM
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.datarpm.sigma.event.model.da;

import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;

import com.datarpm.sigma.event.model.EventDetailModel;
import com.datarpm.sigma.event.model.SystemEventDetailModel;
import com.datarpm.sigma.event.model.UserEventDetailModel;

public class EventDetailModelAccessProvider extends QueryBuilder<EventDetailModel> {

  public EventDetailModelAccessProvider() {
    super(EventDetailModel.class);
  }

  @Override
  public EventDetailModel create(EventDetailModel entity)
      throws NotSupportedException, SystemException {

    SystemEventDetailModel systemEventDetail = entity.getSystemEventDetail();
    if (systemEventDetail != null && systemEventDetail.getId() == null) {
      systemEventDetail = new SystemEventDetailModelAccessProvider().create(systemEventDetail);
      entity.setSystemEventDetail(systemEventDetail);
    }

    UserEventDetailModel userEventDetail = entity.getUserEventDetail();
    if (userEventDetail != null && userEventDetail.getId() == null) {
      userEventDetail = new UserEventDetailModelAccessProvider().create(userEventDetail);
      entity.setUserEventDetail(userEventDetail);
    }
    return super.create(entity);
  }
}
