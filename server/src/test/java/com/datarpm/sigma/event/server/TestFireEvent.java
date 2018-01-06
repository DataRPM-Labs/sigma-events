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
package com.datarpm.sigma.event.server;

import com.datarpm.sigma.event.core.Event.EventBuilder;
import com.datarpm.sigma.event.core.SystemEventDetail;

public class TestFireEvent {
  public static void main(String[] args) {
    String code = "EVENT_CODE1";
    EventBuilder eventBuilder = new EventBuilder(code);
    eventBuilder.addHeader("header3", "headerValue1");
    SystemEventDetail systemEventDetails = new SystemEventDetail();
    eventBuilder.generateSystemEvent(systemEventDetails);
    eventBuilder.fireEvent();
  }
}
