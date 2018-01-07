package com.datarpm.sigma.event.model;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.datarpm.sigma.event.core.conf.Configuration;

public class DefaultDBPersistenceProperty implements PersistencePropertyParser {

  @Override
  public PeristenceConfig parse(Configuration configuration) {
    Map<String, String> properties = new HashMap<>();
    properties.put("hibernate.archive.autodetection", "class");
    properties.put("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
    properties.put("hibernate.connection.driver_class", "org.hsqldb.jdbcDriver");
    String databasePath = "./data/hsqldb/eventdb";
    if (System.getenv("SIGMA_EVENTENGINE_HOME") != null) {
      databasePath = System.getenv("SIGMA_EVENTENGINE_HOME") + "/data/hsqldb/eventdb";
    }

    databasePath = new File(databasePath).getAbsolutePath();

    properties.put("hibernate.connection.url",
        "jdbc:hsqldb:file:" + databasePath + ";shutdown=true,ifexists=true");
    properties.put("hibernate.flushMode", "FLUSH_AUTO");
    properties.put("hibernate.hbm2ddl.auto", "update");
    return new PeristenceConfig(properties, "event-engine-rdbms");
  }

}
