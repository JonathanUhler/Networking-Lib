# Networking-Lib
A C-style, byte-oriented networking library for Java. Networking-Lib provides safety utilities and
a more convenient API on top of the existing `java.net.[Server]Socket` and
`javax.net.ssl.SSL[Server]Socket` classes.


# Dependencies
To use the library, only [Java](https://www.oracle.com/java/technologies/downloads/) version 17
or above is required.

Networking-Lib is built with GNU Make, and has only been tested with Make 3.8 and later, although
older versions may also work.


# Building from Source
To build the project from source, use `make jnet`. To run unit tests, use `make test_jnet`. A full
list of make targets, expect utilities targets (e.g. creating build directories), follows:

| Target         | Commentary                                                                              |
|----------------|-----------------------------------------------------------------------------------------|
| `compile_jnet` | Compiles the Java source files into class files and places them in the `obj` directory. |
| `jar_jnet`     | Creates a JAR file from the compiled class files and puts it in the `bin` directory.    |
| `jnet`         | Wraps the calls to `compile_jnet` and `jar_jnet` in one target.                         |
| `gen_jks`      | Creates a keystore, certificate, and truststore to use for unit tests of `jnet.secure`. |
| `test_jnet`    | Compiles and runs unit tests for jnet.                                                  |
| `javadoc`      | Builds javadoc documentation for jnet.                                                  |
| `clean`        | Removes all generated files (the `bin`, `obj`, and `javadoc` directories).              |


# Using Networking-Lib
Networking-Lib supports client and server communication both with and without a secure connection.
For more implementation details of Networking-Lib communications, see "Networking-Lib-Spec.md" in
the docs directory. API documentation is included as javadoc comments, which can be viewed by
building the docs with `make javadoc`.

## Establishing Connections
There are two important classes for both secure and non-secure connections, `jnet.JClientSocket`
and `jnet.JServer`, with the secure variants `jnet.secure.JSSLClientSocket` and `jnet.secure.JSSLServer` that are children of the non-secure classes.

`JServer` is an abstract class that handles server socket functionality and has the ability to
bind to an address and port with `bind(String, int)`. There are three methods that must be implemented:

| Method                                      | Commentary                                             |
|---------------------------------------------|--------------------------------------------------------|
| `clientConnected(JClientSocket)`            | Called when a new client connects to the server.       |
| `clientCommunicated(byte[], JClientSocket)` | Called when any client sends a message to the server.  |
| `clientDisconnected(JClientSocket)`         | Called when a client has disconnected from the server. |

`JClientSocket`s do not have any methods that need to be implemented. A client socket can be connected
to an address with `connect(String, int)`.

A basic server-client connection might look like:

```java
public class MyServer extends JServer {
    public MyServer(String ip, int port) {
        super(ip, port);
    }
    
    @Override
    public void clientConnected(JClientSocket clientSocket) {
        this.sendAll("A new client has connected");
    }
    
    @Override
    public void clientCommunicated(byte[] recv, JClientSocket clientSocket) {
        this.send(recv, clientSocket);  // Echo to client
    }
    
    @Override
    public void clientDisconnected(JClientSocket clientSocket) {
        // We can't be certain that `clientSocket` can receive messages at this point, but we can
        // use the `JClientSocket` object for any final cleanup operations (e.g. removing from
        // a list of clients that we add to our `JServer` implementation).
        this.sendAll("A client has disconnected");
    }
}


JServer server = new MyServer("localhost", 9000);  // The server will bind at construction-time
JClientSocket client = new JClientSocket();
client.connect("localhost", 9000);  // We must explicitly connect the client.

int bytesSent = client.send("Hello, world!");
if (bytesSent < 0) {  // Always check return values for status information
    System.exit(1);
}
String recv = client.srecv();
System.out.println("Server responded: '" + recv + "'");
client.close();
server.close();
```

Secure connections require the usual definition of a keystore file and keystore password (and possibly a
truststore file for use with self-signed certificates), but the API is otherwise the same for both clients
and sockets.

## Sending the Receiving Information
Networking-Lib uses byte arrays for all communications. Internally, CRC checks are performed on all
messages, and `send`/`recv` methods have the potential to throw a `jnet.MalformedDataException` if a CRC
check fails. The `jnet.Bytes` provides a set of utility methods to convert between bytes and integers,
strings, or arbitrary serializable objects.

The server and client `send` methods are overloaded to support sending a byte array directory or a string,
which acts as a wrapper for `Bytes.stringToBytes`. The `recv` method will always return a byte array, that
can then be interpreted by the caller. If the value of a communication is known to be a string, `srecv`
can be used instead to interpret result of `recv` as a string.
