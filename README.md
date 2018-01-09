# Sigma Event Engine

## Quick start
#### Install
1. Download event-engine-1.6-bin.tar.gz from from [releases](https://github.com/DataRPM-Labs/sigma-events/releases) page
2. Extract
```
tar -xzf event-engine-1.6-bin.tar.gz
```
3. Start service
```
cd event-engine-1.6/
./bin/eventengine-daemon.sh start
```
## Clients
#### Java

Using Maven:
```
<dependency>
    <groupId>com.datarpm.sigma</groupId>
    <artifactId>event-engine-core</artifactId>
    <version>1.6</version>
</dependency>
```

Generate event:
```java
// prepare event builder
// code identifies event (action)
// example: USER_LOGGED_IN, USER_LOGGED_OUT, AUTHENTICATION_FAILURE
String code = "<EVENT_CODE_ID>";
EventBuilder eventBuilder = new EventBuilder(code);
// Set headers
// Used for event filtering
eventBuilder.addHeader("<headerKey>", "<headerValue>");
// Marks it as system event
SystemEventDetail systemEventDetails = new SystemEventDetail();
eventBuilder.generateSystemEvent(systemEventDetails);
eventBuilder.fireEvent();
```

Listen for an event:
```java
EventMatchFilter matchFilter = new EventMatchFilter() {
  /*
   * Tests if specified eventHeader should be used for callback  
   */
  @Override
  public boolean allow(EventHeader eventHeader) {
    // Write filter logic
    return true;
  }
};

EventEngine.INSTANCE.addListner(matchFilter, new EventCallBack() {
  @Override
  public void onEvent(Event event) {
    // Do something
  }
});
```

#### Javascript
Refer to [sigma-events-client-js](https://github.com/DataRPM-Labs/sigma-events-client-js)
