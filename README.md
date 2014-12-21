# JVM Gossiperl client

JVM [gossiperl](https://github.com/radekg/gossiperl) client library.

## Installation

The JVM client is not currently available in Maven. To build from sources an installation of Java and Maven 3 is required. The code should build with Java 6. To build:

    git clone https://github.com/radekg/gossiperl-client-jvm.git
    cd gossiperl-client-jvm/
    mvn clean install

If you don’t have an installation of `gossiperl` running on the box where the client is built, you will have to skip tests:

    mvn clean install -Dmaven.test.skip=true

The build is placed in the `target` directory. Two files are available:

- `gossiperl_client.jar`: client without dependencies
- `gossiperl-client-<version>-jar-with-dependencies.jar`: uber jar, client with all dependencies

## Running

    import com.gossiperl.client.Supervisor;
    import com.gossiperl.client.config.OverlayConfiguration;
    Supervisor supervisor = new Supervisor();

## Connecting to an overlay

    OverlayConfiguration configuration = new OverlayConfiguration();
    configuration.setClientName("jvm-client");
    configuration.setClientPort(54321);
    configuration.setClientSecret("jvm-client-secret");
    configuration.setSymmetricKey("v3JElaRswYgxOt4b");
    configuration.setOverlayName("gossiper_overlay_remote");
    configuration.setOverlayPort(6666);
    supervisor.connect( configuration );

And with a custom listener:

    supervisor.connect( configuration, new CustomListener() );

Where `CustomListener` is a class implementing `com.gossiperl.client.listener.GossiperlClientListener` interface.

A client may be connected to multiple overlays.

## Subscribing / unsubscribing

Subscribing:

    supervisor.subscribe( String overlayName, List<String> events );

Unsubscribing:

    supervisor.unsubscribe( String overlayName, List<String> events );

## Disconnecting from an overlay

    supervisor.disconnect( String overlayName );

## Additional operations

### Checking current client state

    com.gossiperl.client.State.Status supervisor.getCurrentState( String overlayName );

### Get the list of current subscriptions

    List<String> supervisor.getSubscriptions
( String overlayName );

### Sending arbitrary digests

    import com.gossiperl.client.serialization.CustomDigestField;
    
    String overlayName = "gossiper_overlay_remote";
    ArrayList<CustomDigestField> digestData = new ArrayList<CustomDigestField>();
    digestData.put(new CustomDigestField("field_name", "some value for the field", "string", 1));
    digestData.put(new CustomDigestField("integer_field", 1234, "i32", 2));
    supervisor.send( overlayName )

Where `<type>` is one of the supported serializable types:

- `string`: `java.lang.String`
- `bool`: `java.lang.Boolean`
- `byte`: `java.lang.Byte`
- `double`: `java.lang.Double`
- `i16`: `java.lang.Long`
- `i32`: `java.lang.Long`
- `i64`: `java.lang.Long`

Other Thrift types are not supported. `CustomDigestField`s constructor is:

    CustomDigestField(String fieldName, Object value, String type, short fieldOrder)

Where `fieldOrder` is a Thrift field order.

## Running tests

    mvn test

Tests assume an overlay with the details specified in the `test/com/gossiperl/client/ProcessTest.java` running.

## License

The MIT License (MIT)

Copyright (c) 2014 Radoslaw Gruchalski <radek@gruchalski.com>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
