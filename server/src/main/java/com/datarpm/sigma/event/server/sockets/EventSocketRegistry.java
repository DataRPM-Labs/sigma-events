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
