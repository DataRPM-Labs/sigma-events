#-------------------------------------------------------------------------------
# Copyright 2017 DataRPM
# 
# Licensed under the Apache License, Version 2.0 (the "License"); you may not
# use this file except in compliance with the License.  You may obtain a copy
# of the License at
# 
#   http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
# License for the specific language governing permissions and limitations under
# the License.
#-------------------------------------------------------------------------------
/*
Event Model structure
  id: String,
  header: {
    code: String,
    timeStamp: Long, // event creation timestamp
    eventType: System | User,
    headers: {
      "":"" // Key-Value pair
    }
  },
  eventDetail: {
    systemEventDetail: {
      moduleName: "",
      action: ""  
    }
  },
  params: {
    "":"" // Key-Value pair
  }
*/
window.EventEngine = (function() {

  function EventEngine(options) {
    var self = this;
    this.eventRegistry = {};
    this.socketOpen = false;

    if(!options.url) {
      throw "event stream url missing"
    }

    this.eventStream = new ReconnectingWebSocket(options.url);
    this.eventStream.onopen = function(e) {
      self.socketOpen = true;
      for(var registryId in self.eventRegistry) {
        var eachCallbackRegistry = self.eventRegistry[registryId];
        self.addListener(eachCallbackRegistry.query, eachCallbackRegistry.callback, registryId)
      }
    }

    this.eventStream.onmessage = function(e) {
      var eventDataJsonStr = e.data;
      var eventData = JSON.parse(eventDataJsonStr);
      if(eventData.opCode === "CALLBACK") {
        var registryInfo = self.eventRegistry[eventData.registryId];
        if(registryInfo) {
          registryInfo.callback(eventData.event);
        }
      }
    }

    this.eventStream.onclose = function(e) {
      self.socketOpen = false;
    }

    this.keepalive();
  }

  EventEngine.prototype.keepalive = function(){
    if(this.socketOpen) {
      this.eventStream.send(JSON.stringify({
        "opCode": "PING"
      }))
    }
    var self = this;
    setTimeout(function(){ self.keepalive(); }, 30000);
  }

  /*
  Query Structure
    query: {
      code: "", // This field should be used to filter event by code
      headers: { // This field should be used to filter event by header key/value 
        "":"" // Key-Value pair
      }
    }
  
  callback(event //Event)
  */
  EventEngine.prototype.addListener = function(query, callback, uiRegistryId){

    if(!callback) {
      throw "callback function not provided"
    }

    if(!query) {
      throw "query not provided"
    }

    if(!uiRegistryId) {
      uiRegistryId = Math.random().toString(36);
    }

    this.eventRegistry[uiRegistryId] = {
      "query": query,
      "callback":callback
    }

    if(this.socketOpen) {
      this.eventStream.send(JSON.stringify({
        "opCode": "REGISTER_CALLBACK",
        "query": query,
        "registryId": uiRegistryId
      }))
    }
    return uiRegistryId;
  };

  EventEngine.prototype.removeListener = function(registryId) {
    if(this.socketOpen) {
      this.eventStream.send(JSON.stringify({
        "opCode": "UNREGISTER_CALLBACK",
        "registryId": registryId
      }))
    } else {

    }
  }
  
  var eventEngine = {
    get: function (options){
      return new EventEngine(options);
    }
  };
  return eventEngine;
})();
