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
package com.datarpm.sigma.event.model;

import java.util.HashMap;
import java.util.Map;

import com.datarpm.sigma.event.core.conf.Configuration;

public class MongoDBPersistencePropertyParser implements PersistencePropertyParser {

  @Override
  public Map<String, String> parse(Configuration configuration) {
    Map<String, String> map = new HashMap<>();
    map.put("hibernate.ogm.datastore.provider", "mongodb");
    String host = configuration.get("events.storage.mongodb.host");
    if (host == null || host.trim().isEmpty()) {
      host = "localhost";
    }
    map.put("hibernate.ogm.datastore.host", host);

    String port = configuration.get("events.storage.mongodb.port");
    if (port == null || port.trim().isEmpty()) {
      port = "localhost";
    }
    map.put("hibernate.ogm.datastore.port", port);

    String database = configuration.get("events.storage.mongodb.database");
    if (database != null && !database.trim().isEmpty()) {
      map.put("hibernate.ogm.datastore.database", database);
    }

    return map;
  }

}
