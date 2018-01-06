/**
 * 
 */
package com.datarpm.sigma.event.server.sockets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.datarpm.sigma.event.core.RegistryId;

/**
 * @author vishalkatkar
 *
 */
public class EventSocketRegistry {

  private Map<String, RegistryInfo> registryMap = null;

  public EventSocketRegistry() {
    registryMap = new ConcurrentHashMap<>();
  }

  public void put(String registryId, RegistryInfo registryInfo) {
    registryMap.put(registryId, registryInfo);
  }

  public RegistryInfo get(String registryId) {
    return registryMap.get(registryId);
  }

  public RegistryInfo remove(String registryId) {
    return registryMap.remove(registryId);
  }
  
  public List<RegistryInfo> getAll() {
    return new ArrayList<>(registryMap.values());
  }

  public static class RegistryInfo {

    private String uiRegistryId;
    private RegistryId serverRegistryId;

    public RegistryInfo(String uiRegistryId, RegistryId serverRegistryId) {
      super();
      this.uiRegistryId = uiRegistryId;
      this.serverRegistryId = serverRegistryId;
    }

    public String getUiRegistryId() {
      return uiRegistryId;
    }

    public void setUiRegistryId(String uiRegistryId) {
      this.uiRegistryId = uiRegistryId;
    }

    public RegistryId getServerRegistryId() {
      return serverRegistryId;
    }

    public void setServerRegistryId(RegistryId serverRegistryId) {
      this.serverRegistryId = serverRegistryId;
    }

  }

}
