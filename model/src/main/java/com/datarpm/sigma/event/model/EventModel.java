/**
 * 
 */
package com.datarpm.sigma.event.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.DateBridge;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Resolution;
import org.hibernate.search.annotations.Store;

import com.datarpm.sigma.event.core.Event;
import com.datarpm.sigma.event.core.EventDetail;
import com.datarpm.sigma.event.core.EventHeader;

/**
 * @author vishal
 *
 */
@Entity
@Indexed
public class EventModel {
  @Id
  private String id;

  @OneToOne(orphanRemoval = true, fetch = FetchType.LAZY)
  private EventHeaderModel header;
  @OneToOne(orphanRemoval = true, fetch = FetchType.LAZY)
  private EventDetailModel eventDetail;
  @ElementCollection(fetch = FetchType.LAZY)
  private Map<String, String> params;

  private Date createdAt;
  @Field(analyze = Analyze.NO, store = Store.YES)
  @DateBridge(resolution = Resolution.MILLISECOND)
  private Date updatedAt;

  EventModel() {
  }

  EventModel(EventHeader header, EventDetail eventDetail) {
    this.header = new EventHeaderModel(header.getCode(), header.getTimeStamp(),
        header.getEventType(), header.getHeaders());
    if (eventDetail != null) {
      this.eventDetail = new EventDetailModel(eventDetail.getEventType(),
          eventDetail.getSystemEventDetail(), eventDetail.getUserEventDetail());
    }
  }

  public EventModel(Event event) {
    this(event.getHeader(), event.getEventDetail());
    this.id = event.getId();
    this.params = new HashMap<>(event.getParams());
  }

  public Map<String, String> getParams() {
    return params;
  }

  public void setParams(Map<String, String> params) {
    this.params = new HashMap<>(params);
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public EventHeaderModel getHeader() {
    return header;
  }

  public void setHeader(EventHeaderModel header) {
    this.header = header;
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

  public EventDetailModel getEventDetail() {
    return eventDetail;
  }

  public void setEventDetail(EventDetailModel eventDetail) {
    this.eventDetail = eventDetail;
  }

}
