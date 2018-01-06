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
package com.datarpm.sigma.event;

import org.junit.Test;

import com.datarpm.sigma.event.core.Event;
import com.datarpm.sigma.event.core.Event.EventBuilder;
import com.datarpm.sigma.event.core.EventDetail;
import com.datarpm.sigma.event.core.SystemEventDetail;

import junit.framework.TestCase;

public class EventDetailTest extends TestCase {
  @Test
  public void testSimpleCapture() throws Exception {
    String code = "E-01";
    EventBuilder builder = new Event.EventBuilder(code);
    SystemEventDetail systemEventDetails = new SystemEventDetail();
    builder.generateSystemEvent(systemEventDetails);
    Event event = builder.toEvent();
    EventDetail eventDetail = event.getEventDetail();
    SystemEventDetail systemEventDetail = eventDetail.getSystemEventDetail();
    assertNotNull(systemEventDetail.getProcessId());
    assertNotNull(systemEventDetail.getProcessName());
    assertNotNull(systemEventDetail.getGeneratorTrace());
    assertNotNull(systemEventDetail.getVmDetail());

    System.out.println("Process-Details : {processid = " + systemEventDetail.getProcessId()
        + ", processname = " + systemEventDetail.getProcessName() + ", vmdetails = "
        + systemEventDetail.getVmDetail() + "}");
  }
}
