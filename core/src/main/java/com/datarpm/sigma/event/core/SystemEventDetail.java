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
package com.datarpm.sigma.event.core;

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

/**
 * @author vishal
 *
 */
public class SystemEventDetail implements Serializable {

  private static final long serialVersionUID = 1L;

  private static String PROCESS_ID = "NA";
  private static String PROCESS_NAME = "NA";
  private static String VM_DETAIL = "NA";

  static {
    RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
    PROCESS_ID = runtimeMXBean.getName();
    PROCESS_NAME = System.getProperty("sun.java.command");
    VM_DETAIL = runtimeMXBean.getVmName() + " " + runtimeMXBean.getVmVendor() + " "
        + runtimeMXBean.getVmVersion();
  }

  private String productName;
  private String moduleName;
  private String action;
  private String macId;
  private String processId;
  private String processName;
  private String generatorTrace;
  private String vmDetail;

  public SystemEventDetail() {
    setProcessDetails(PROCESS_ID, PROCESS_NAME, VM_DETAIL);
  }

  public SystemEventDetail(String productName, String moduleName, String action) {
    this();
    this.productName = productName;
    this.moduleName = moduleName;
    this.action = action;
  }

  void setProcessDetails(String processId, String processName, String vmDetail) {
    this.processId = processId;
    this.processName = processName;
    this.vmDetail = vmDetail;
    this.generatorTrace = generateCallerTrace();
  }

  private String generateCallerTrace() {
    StackTraceElement[] callTrace;
    try {
      throw new IllegalStateException();
    } catch (Exception e) {
      callTrace = e.getStackTrace();
    }

    String ignoreCurrentObjectTrace = this.getClass().getName();
    StringBuffer stace = new StringBuffer();
    for (StackTraceElement stackTraceElement : callTrace) {
      String trace = stackTraceElement.toString();
      if (trace.startsWith(ignoreCurrentObjectTrace)) {
        continue;
      }
      stace.append(stackTraceElement).append("\n");
    }
    return stace.toString();
  }

  public String getProcessId() {
    return processId;
  }

  public String getProcessName() {
    return processName;
  }

  public String getGeneratorTrace() {
    return generatorTrace;
  }

  public String getVmDetail() {
    return vmDetail;
  }

  public String getMacId() {
    return macId;
  }

  public void setMacId(String macId) {
    this.macId = macId;
  }

  public String getProductName() {
    return productName;
  }

  public void setProductName(String productName) {
    this.productName = productName;
  }

  public String getModuleName() {
    return moduleName;
  }

  public void setModuleName(String moduleName) {
    this.moduleName = moduleName;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  @Override
  public String toString() {
    return "SystemEventDetail [productName=" + productName + ", moduleName=" + moduleName
        + ", action=" + action + ", macId=" + macId + ", processId=" + processId + ", processName="
        + processName + ", generatorTrace=" + generatorTrace + ", vmDetail=" + vmDetail + "]";
  }
}