/**
 * 
 */
package com.datarpm.sigma.event.model;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author vishal
 *
 */
@Entity
public class EventHdfsDumperRunStats {
  @Id
  private long id;
  private long lastRunMillis;

  public long getLastRunMillis() {
    return lastRunMillis;
  }

  public void setLastRunMillis(long lastRunMillis) {
    this.lastRunMillis = lastRunMillis;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

}
