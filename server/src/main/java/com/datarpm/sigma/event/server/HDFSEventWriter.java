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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;

import com.datarpm.sigma.event.core.EventEnvironment;
import com.datarpm.sigma.event.core.EventType;
import com.datarpm.sigma.event.model.EventDetailModel;
import com.datarpm.sigma.event.model.EventHeaderModel;
import com.datarpm.sigma.event.model.EventModel;
import com.datarpm.sigma.event.model.SystemEventDetailModel;
import com.datarpm.sigma.event.model.UserEventDetailModel;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import parquet.avro.AvroSchemaConverter;
import parquet.avro.AvroWriteSupport;
import parquet.hadoop.ParquetWriter;
import parquet.hadoop.metadata.CompressionCodecName;
import parquet.schema.MessageType;

class HDFSEventWriter {
  private static final int ONE_MB = 1024 * 1024;
  private static final String EVENT_TABLE_NAME = "Events";

  private String hdfsUrl;
  private String archivePath;
  private Configuration hadoopConfiguration;
  private String dfsUrl;
  private Schema avroSchema;
  @SuppressWarnings("rawtypes")
  private ParquetWriter parquetWriter;

  public HDFSEventWriter() {
    Properties archiverConfig = EventEnvironment.INSTANCE.getArchiverConfig();
    hdfsUrl = archiverConfig.getProperty("events.archive.hdfs.address", "hdfs://127.0.0.1:8020");
    archivePath = archiverConfig.getProperty("events.archive.hdfs.path", "/user/datarpm/data/events/");
    hadoopConfiguration = new Configuration();
    hadoopConfiguration.set("dfs.http.address", hdfsUrl);
    hadoopConfiguration.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");
    hadoopConfiguration.setClassLoader(HDFSEventWriter.class.getClassLoader());
    dfsUrl = hdfsUrl + archivePath + getCurrentYear() + "/" + getPartFileName();
  }

  private String getCurrentYear() {
    SimpleDateFormat format = new SimpleDateFormat("yyyy");
    return format.format(new Date());
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private void initializeWriter() {

    if (parquetWriter != null) {
      return;
    }

    try {
      avroSchema = arhiveEventSchemaInfo();
      MessageType parquetSchema = new AvroSchemaConverter().convert(avroSchema);
      AvroWriteSupport writeSupport = new AvroWriteSupport(parquetSchema, avroSchema);
      CompressionCodecName compressionCodecName = CompressionCodecName.SNAPPY;
      // set Parquet file block size and page size values
      int blockSize = 64 * ONE_MB;
      int pageSize = 1 * ONE_MB;
      Path outputPath = new Path(dfsUrl);
      parquetWriter = new ParquetWriter(outputPath, writeSupport, compressionCodecName, blockSize,
          pageSize);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private String getPartFileName() {
    SimpleDateFormat format = new SimpleDateFormat("MMM_dd_yyyy_HH_mm");
    return format.format(new Date());
  }

  @SuppressWarnings("unchecked")
  private void addEvent(EventModel event) {
    GenericRecord avroRow = toAvroRow(event);
    try {
      parquetWriter.write(avroRow);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  public void addEvents(List<EventModel> eventsToLoad) {
    initializeWriter();
    for (EventModel eventModel : eventsToLoad) {
      addEvent(eventModel);
    }
  }

  private GenericRecord toAvroRow(EventModel event) {
    GenericRecordBuilder recordBuilder = new GenericRecordBuilder(avroSchema);
    EventHeaderModel header = event.getHeader();
    recordBuilder.set(EventHDFSTableSchema.EVENT_ID, event.getId().toString());
    recordBuilder.set(EventHDFSTableSchema.EVENT_CODE, header.getCode());
    recordBuilder.set(EventHDFSTableSchema.EVENT_CREATED_AT_MILLIS, header.getTimeStamp());
    recordBuilder.set(EventHDFSTableSchema.EVENT_HEADER_JSON, toJson(header.getHeaders()));
    recordBuilder.set(EventHDFSTableSchema.EVENT_PARAMS_JSON, toJson(event.getParams()));

    EventType eventType = header.getEventType();
    if (eventType != null) {
      recordBuilder.set(EventHDFSTableSchema.EVENT_TYPE, eventType.name());
      EventDetailModel eventDetail = event.getEventDetail();
      if (header.getEventType() == EventType.System) {
        SystemEventDetailModel systemEventDetail = eventDetail.getSystemEventDetail();
        // System Event
        recordBuilder.set(EventHDFSTableSchema.PROCESS_ID, systemEventDetail.getProcessId());
        recordBuilder.set(EventHDFSTableSchema.PROCESS_NAME, systemEventDetail.getProcessName());
        recordBuilder.set(EventHDFSTableSchema.VM_DETAILS, systemEventDetail.getVmDetail());
        recordBuilder.set(EventHDFSTableSchema.CALL_TRACE, systemEventDetail.getGeneratorTrace());
        recordBuilder.set(EventHDFSTableSchema.MAC_ID, systemEventDetail.getMacId());
        recordBuilder.set(EventHDFSTableSchema.PRODUCT_NAME, systemEventDetail.getProductName());
        recordBuilder.set(EventHDFSTableSchema.MODULE_NAME, systemEventDetail.getModuleName());
        recordBuilder.set(EventHDFSTableSchema.ACTION, systemEventDetail.getAction());
      } else {
        UserEventDetailModel userEventDetail = eventDetail.getUserEventDetail();
        // User Event
        recordBuilder.set(EventHDFSTableSchema.USER_ID, userEventDetail.getUserId());
        recordBuilder.set(EventHDFSTableSchema.USER_AGENT, userEventDetail.getUserAgent());
        recordBuilder.set(EventHDFSTableSchema.CLIENT_IP_ADDRESS, userEventDetail.getIpAddress());
        recordBuilder.set(EventHDFSTableSchema.ACTION_URL, userEventDetail.getActionUrl());
      }
    }
    verifyNull(recordBuilder);
    return recordBuilder.build();
  }

  private void verifyNull(GenericRecordBuilder recordBuilder) {
    List<Field> fields = avroSchema.getFields();
    for (Field field : fields) {
      Object value = recordBuilder.get(field);
      if (value == null) {
        recordBuilder.set(field, "");
      }
    }
  }

  private String toJson(Map<String, String> valueMap) {
    if (valueMap == null) {
      valueMap = new HashMap<String, String>();
    }
    return new Gson().toJson(valueMap);
  }

  private Schema arhiveEventSchemaInfo() {
    JsonObject schemaJson = new JsonObject();
    schemaJson.addProperty("type", "record");
    schemaJson.addProperty("name", EVENT_TABLE_NAME);

    JsonArray fields = new JsonArray();
    fields.add(toField(EventHDFSTableSchema.EVENT_ID, "string"));
    fields.add(toField(EventHDFSTableSchema.EVENT_CREATED_AT_MILLIS, "long", false));
    fields.add(toField(EventHDFSTableSchema.EVENT_CODE, "string"));
    fields.add(toField(EventHDFSTableSchema.EVENT_TYPE, "string"));
    fields.add(toField(EventHDFSTableSchema.EVENT_HEADER_JSON, "string"));
    fields.add(toField(EventHDFSTableSchema.EVENT_PARAMS_JSON, "string"));

    // System Event
    fields.add(toField(EventHDFSTableSchema.PROCESS_ID, "string"));
    fields.add(toField(EventHDFSTableSchema.PROCESS_NAME, "string"));
    fields.add(toField(EventHDFSTableSchema.VM_DETAILS, "string"));
    fields.add(toField(EventHDFSTableSchema.CALL_TRACE, "string"));
    fields.add(toField(EventHDFSTableSchema.PRODUCT_NAME, "string"));
    fields.add(toField(EventHDFSTableSchema.MODULE_NAME, "string"));
    fields.add(toField(EventHDFSTableSchema.ACTION, "string"));
    fields.add(toField(EventHDFSTableSchema.MAC_ID, "string"));

    // User Event
    fields.add(toField(EventHDFSTableSchema.USER_ID, "string"));
    fields.add(toField(EventHDFSTableSchema.USER_AGENT, "string"));
    fields.add(toField(EventHDFSTableSchema.ACTION_URL, "string"));
    fields.add(toField(EventHDFSTableSchema.CLIENT_IP_ADDRESS, "string"));
    schemaJson.add("fields", fields);

    return new Schema.Parser().parse(schemaJson.toString());
  }

  private JsonObject toField(String name, String type) {
    return toField(name, type, true);
  }

  private JsonObject toField(String name, String type, boolean setDefault) {
    JsonObject field = new JsonObject();
    field.addProperty("name", name);
    if (setDefault) {
      field.addProperty("default", "");
    }
    field.addProperty("type", type);
    return field;
  }

  public void stop() {
    if (parquetWriter != null) {
      try {
        parquetWriter.close();
      } catch (IOException e) {
      }
    }
  }

}
