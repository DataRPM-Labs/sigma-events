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

import java.io.IOException;

import com.datarpm.sigma.event.core.conf.Configuration;
import com.datarpm.sigma.event.core.conf.ConfigurationReader;

public class PersistencePropertyProvider {
  private static final String SITE_XML = "storage-site.xml";

  public PeristenceConfig preparePersistenceConfig() {
    try {
      Configuration configuration = ConfigurationReader.INSTANCE.readConfiguration(SITE_XML);
      String storage = configuration.get("events.storage");
      if (storage == null) {
        storage = "default";
      }
      return new PersistencePropertyParserFactory().create(storage).parse(configuration);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }
}
