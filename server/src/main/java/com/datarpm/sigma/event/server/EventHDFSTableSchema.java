/**
 * 
 */
package com.datarpm.sigma.event.server;

/**
 * @author vishal
 *
 */
public interface EventHDFSTableSchema {

  String EVENT_ID = "eventId";
  String EVENT_CODE = "eventCode";
  String EVENT_TYPE = "eventType";
  String EVENT_HEADER_JSON = "eventHeaderJson";
  String EVENT_CREATED_AT_MILLIS = "eventCreatedAtMillis";
  String EVENT_PARAMS_JSON = "eventParamsJson";
  
  // System Event Details
  String PROCESS_ID = "processId";
  String PROCESS_NAME = "processName";
  String VM_DETAILS = "vmDetails";
  String PRODUCT_NAME = "productName";
  String MODULE_NAME = "moduleName";
  String ACTION = "action";
  String MAC_ID = "macId";
  String CALL_TRACE = "callTrace";

  // User Event
  String USER_AGENT = "userAgent";
  String USER_ID = "userId";
  String CLIENT_IP_ADDRESS = "ipAddress";
  String ACTION_URL = "actionUrl";
}
