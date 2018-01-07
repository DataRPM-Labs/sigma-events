package com.datarpm.sigma.event.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;

import org.hibernate.annotations.GenericGenerator;

import com.datarpm.sigma.event.core.SystemEventDetail;

@Entity
public class SystemEventDetailModel {
  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  private String id;

  private String productName;
  private String moduleName;
  private String action;
  private String macId;
  private String processId;
  private String processName;
  @Lob
  private String generatorTrace;
  private String vmDetail;

  public SystemEventDetailModel() {
  }

  public SystemEventDetailModel(SystemEventDetail detail) {
    this.action = detail.getProcessId();
    this.generatorTrace = detail.getProcessId();
    this.macId = detail.getProcessId();
    this.moduleName = detail.getProcessId();
    this.processId = detail.getProcessId();
    this.processName = detail.getProcessId();
    this.productName = detail.getProcessId();
    this.vmDetail = detail.getProcessId();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
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

  public String getMacId() {
    return macId;
  }

  public void setMacId(String macId) {
    this.macId = macId;
  }

  public String getProcessId() {
    return processId;
  }

  public void setProcessId(String processId) {
    this.processId = processId;
  }

  public String getProcessName() {
    return processName;
  }

  public void setProcessName(String processName) {
    this.processName = processName;
  }

  public String getGeneratorTrace() {
    return generatorTrace;
  }

  public void setGeneratorTrace(String generatorTrace) {
    this.generatorTrace = generatorTrace;
  }

  public String getVmDetail() {
    return vmDetail;
  }

  public void setVmDetail(String vmDetail) {
    this.vmDetail = vmDetail;
  }

}
