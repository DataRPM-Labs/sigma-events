/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
package com.datarpm.sigma.event.core.config;

import java.util.Properties;

/**
 * Holder for event engine configuration properties.
 * 
 * @author vinay
 *
 */
public class EventEngineConfig {

  public static final EventEngineConfig INSTANCE = new EventEngineConfig();

  /**
   * @return Server configurations
   */
  public Properties getServerConfig() {
    return null;
  }

  /**
   * @return MessageQueue configurations
   */
  public Properties getMQChannelConfig() {
    return null;
  }

  /**
   * @return Event Archiver configurations
   */
  public Properties getArchiverConfig() {
    return null;
  }
}