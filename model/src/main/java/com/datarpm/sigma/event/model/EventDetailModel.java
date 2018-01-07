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
package com.datarpm.sigma.event.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;

import org.hibernate.annotations.GenericGenerator;

import com.datarpm.sigma.event.core.EventType;
import com.datarpm.sigma.event.core.SystemEventDetail;
import com.datarpm.sigma.event.core.UserEventDetail;

@Entity
public class EventDetailModel {
  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  private String id;

  private EventType eventType;
  @OneToOne(orphanRemoval = true, fetch = FetchType.LAZY)
  private SystemEventDetailModel systemEventDetail;
  @OneToOne(orphanRemoval = true, fetch = FetchType.LAZY)
  private UserEventDetailModel userEventDetail;
  private Date createdAt;
  private Date updatedAt;

  public EventDetailModel(EventType eventType, SystemEventDetail systemEventDetail,
      UserEventDetail userEventDetail) {
    super();
    this.eventType = eventType;
    this.systemEventDetail = new SystemEventDetailModel(systemEventDetail);
    this.userEventDetail = new UserEventDetailModel(userEventDetail);
  }

  public EventDetailModel() {
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public EventType getEventType() {
    return eventType;
  }

  public void setEventType(EventType eventType) {
    this.eventType = eventType;
  }

  public SystemEventDetailModel getSystemEventDetail() {
    return systemEventDetail;
  }

  public void setSystemEventDetail(SystemEventDetailModel systemEventDetail) {
    this.systemEventDetail = systemEventDetail;
  }

  public UserEventDetailModel getUserEventDetail() {
    return userEventDetail;
  }

  public void setUserEventDetail(UserEventDetailModel userEventDetail) {
    this.userEventDetail = userEventDetail;
  }

  @PrePersist
  void updateDate() {
    updatedAt = new Date();
    if (createdAt == null) {
      createdAt = new Date();
    }
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public Date getUpdatedAt() {
    return updatedAt;
  }

}
