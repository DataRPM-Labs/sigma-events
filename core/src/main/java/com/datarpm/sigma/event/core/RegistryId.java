/**
 * 
 */
package com.datarpm.sigma.event.core;

import java.util.UUID;

/**
 * @author vishal
 *
 */
public class RegistryId {
  private String id;

  public RegistryId() {
    id = UUID.randomUUID().toString();
  }

  public RegistryId(String id) {
    super();
    this.id = id;
  }

  public String getId() {
    return id;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    RegistryId other = (RegistryId) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "RegistryId [id=" + id + "]";
  }

}
