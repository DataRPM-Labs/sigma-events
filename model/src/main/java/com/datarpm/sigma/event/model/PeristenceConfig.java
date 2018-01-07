package com.datarpm.sigma.event.model;

import java.util.Map;

public class PeristenceConfig {

  private Map<String, String> properties = null;
  private String persistenceUnit = null;

  public PeristenceConfig(Map<String, String> properties, String persistenceUnit) {
    super();
    this.properties = properties;
    this.persistenceUnit = persistenceUnit;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  public void setPersistenceUnit(String persistenceUnit) {
    this.persistenceUnit = persistenceUnit;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public String getPersistenceUnit() {
    return persistenceUnit;
  }
}