package jnet.secure;


import jnet.Bytes;
import javax.net.ssl.SSLSocket;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;


/**
 * C-style server template. This class provides the framework to create a server compatible with the
 * processes of this library. By default, the server runs on 127.0.0.1:9000, but this can be changed
 * with the second constructor. This server supports SSL security.
 * <p>
 * Some notable abstract methods include:
 * <table style="border: 1px solid black">
 *  <caption>Abstract Methods</caption>
 *  <tr style="border: 1px solid black">
 *   <th style="border: 1px solid black"> Method
 *   <th style="border: 1px solid black"> Commentary
 *  </tr>
 *  <tr style="border: 1px solid black">
 *   <td style="border: 1px solid black"> {@code clientConnected}
 *   <td style="border: 1px solid black"> Called when a new client connects to the server, passing 
 *                                        in the client's socket object so messages can be sent.
 *  </tr>
 *  <tr style="border: 1px solid black">
 *   <td style="border: 1px solid black"> {@code clientCommunicated}
 *   <td style="border: 1px solid black"> Called when any client sends a message to the server, 
 *                                        passing in the bytes sent and the client's socket object.
 *  </tr>
 * </table>
 * <p>
 * Some notable threads created by this class include:
 * <table style="border: 1px solid black">
 *  <caption>Threads</caption>
 *  <tr style="border: 1px solid black">
 *   <th style="border: 1px solid black"> Thread
 *   <th style="border: 1px solid black"> Commentary
 *  </tr>
 *  <tr style="border: 1px solid black">
 *   <td style="border: 1px solid black"> {@code clientThread}
 *   <td style="border: 1px solid black"> Created by {@code Server::add}. Runs the 
 *                                        {@code Server::listenOnClient} method for a given client 
 *                                        connection, so that the server can continue accepting 
 *                                        new clients in parallel.
 *  </tr>
 * </table>
 * <p>
 * The security protocols and cipher suites are defined in the {@code PROTOCOLS} and
 * {@code CIPHER_SUITES} respectively. These cannot currently be changed by the end user during
 * class construction.
 *
 * @see clientConnected
 * @see clientCommunicated
 *
 * @author Jonathan Uhler
 */
public abstract class JSSLServer {
    
    /** The default IP address for the server. */
    public static final String DEFAULT_IP_ADDR = "127.0.0.1";
    /** The default port for the server. */
    public static final int DEFAULT_PORT = 9000;
    /** The maximum number of clients that can be placed in the backlog buffer. */
    public static final int BACKLOG = 50;
    /** The default security protocols. */
    public static final String[] PROTOCOLS = new String[] {"TLSv1.2", "TLSv1.1", "TLSv1"};
    /** The default cipher suites used for secure connections. */
    public static final String[] CIPHER_SUITES = {"TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA",
                                                  "TLS_RSA_WITH_AES_128_CBC_SHA",
                                                  "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
                                                  "TLS_RSA_WITH_AES_128_CBC_SHA256",
                                                  "TLS_RSA_WITH_AES_128_GCM_SHA256",
                                                  "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
                                                  "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
                                                  "TLS_RSA_WITH_AES_256_CBC_SHA",
                                                  "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA",
                                                  "TLS_RSA_WITH_AES_256_CBC_SHA256",
                                                  "TLS_RSA_WITH_AES_256_GCM_SHA384",
                                                  "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384",
                                                  "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384"};
    
    
    private Map<JSSLClientSocket, Thread> clientConnections;
    private JSSLServerSocket serverSocket;
    private String ip;
    private int port;
    
    
    /**
     * Default constructor for a server. This constructor uses the default IP address and port 
     * defined as instance variables of this class.
     *
     * @throws IOException  if the server cannot be bound to the IP address and port.
     */
    public JSSLServer() throws IOException {
        this(JSSLServer.DEFAULT_IP_ADDR, JSSLServer.DEFAULT_PORT);
    }
    
    
    /**
     * Constructs a {@code JSSLServer} object with a given IP address and port.
     *
     * @param ip    the IP address to start the server on.
     * @param port  the port to start the server on.
     *
     * @throws IOException  if the server cannot be bound to the IP address and port.
     */
    public JSSLServer(String ip, int port) throws IOException {
        this.ip = ip;
        this.port = port;
	
        this.clientConnections = new HashMap<>();
	
        this.serverSocket = new JSSLServerSocket();
        this.serverSocket.bind(this.ip, this.port, JSSLServer.BACKLOG,
                               JSSLServer.PROTOCOLS, JSSLServer.CIPHER_SUITES);
        
        // Start the accept method in a new thread. This allows more constructor code to be
        // added is desired
        Thread acceptThread = new Thread(() -> this.accept());
        acceptThread.start();
    }
    
    
    /**
     * Returns the IP address the server has been binded to.
     *
     * @return the IP address the server has been binded to.
     */
    public String getIP() {
        return this.ip;
    }
    
    
    /**
     * Returns the port this server has been binded to.
     *
     * @return the port this server has been binded to.
     */
    public int getPort() {
        return this.port;
    }
    
    
    /**
     * Waits for and accepts incoming client connections. This method runs in the main thread of 
     * this {@code JSSLServer} object. Other operations are managed by separate secondary threads.
     */
    private void accept() {
        // Unconditionally wait for and accept client connections, then assign them an id/place in
        // the array and create a ClientSock object to represent that conneciton and allow .send()
        // calls towards that client
        while (true) {
            SSLSocket clientConnection = this.serverSocket.accept();
            
            // Add the client to the list of connected clients, and start listening
            this.add(clientConnection);
        }
    }
    
    
    /**
     * Processes a new client connection. This includes creating a new thread to handle 
     * communication with that client and putting the client into the {@code clientConnections} 
     * map. Because the {@code JSSLClientSocket::connect} method is volatile under normal use, an 
     * IOException may be thrown. This error is caught by this method, although it should never 
     * occur if the server was able to bind successfully to the IP address and port given in the 
     * constructor.
     *
     * @param clientConnection  a java {@code SSLSocket} object for the client that connected.
     */
    private void add(SSLSocket clientConnection) {
        if (clientConnection == null)
            return;
        
        JSSLClientSocket clientSocket = new JSSLClientSocket(clientConnection);
        try {
            clientSocket.connect(this.ip, this.port,
                                 JSSLServer.PROTOCOLS,
                                 JSSLServer.CIPHER_SUITES);
        }
        catch (IOException e) {
            return;
        }
        
        // Start each client with an individual thread that calls a localized message parsing
        // method in this Server object. This allows the server to sit each client in a while(true)
        // loop calling recv to get data from the client until something comes back.
        Thread clientThread = new Thread(() -> this.listenOnClient(clientSocket));
        
        // Add the new client to the list of connected clients
        this.clientConnections.put(clientSocket, clientThread);
        
        // Allow the child class to take any required actions to intialize this connection
        this.clientConnected(clientSocket);
        
        // Start the thread
        clientThread.start();
    }
    
    
    /**
     * Performs an arbitrary action when a client first connects. Called by the private 
     * {@code JSSLServer::add} method before the client's thread is started.
     *
     * @param clientSocket  the client that connected.
     */
    public abstract void clientConnected(JSSLClientSocket clientSocket);
    
    
    /**
     * Performs an arbitrary action when a client disconnects. Called by the private 
     * {@code JSSLServer::listenOnClient} method before the client is removed from this 
     * {@code JSSLServer}'s scope.
     *
     * @param clientSocket  the client that disconnected.
     */
    public abstract void clientDisconnected(JSSLClientSocket clientSocket);
    
    
    /**
     * Listens on a specific client.
     *
     * @param clientSocket  the {@code JSSLClientSocket} object to listen to.
     */
    private void listenOnClient(JSSLClientSocket clientSocket) {
        while (true) {
            byte[] recv = this.serverSocket.recv(clientSocket);
            if (recv == null) {
                this.clientDisconnected(clientSocket);
                this.clientConnections.remove(clientSocket);
                return;
            }
            
            this.clientCommunicated(recv, clientSocket);
        }
    }
    
    
    /**
     * Performs an arbitrary action when a client sends a message. Called by the private 
     * {@code JSSLServer::listenOnClient} method after validating the received message. The 
     * argument {@code recv} is guaranteed to contain a valid, non-null message
     *
     * @param recv          the message received from the client
     * @param clientSocket  the client who sent the message
     */
    public abstract void clientCommunicated(byte[] recv, JSSLClientSocket clientSocket);
    
    
    /**
     * Sends a message to a client as a byte array.
     *
     * @param payload       a byte array to send.
     * @param clientSocket  the client to send to.
     *
     * @see jnet.JServerSocket
     */
    public void send(byte[] payload, JSSLClientSocket clientSocket) {
        // The JClientSocket objects here allow use of the IN and OUT buffers to read/write
        // messages. It is up to the actual client on the client-side to call the .recv() method
        // of ClientSock in order to receive the data sent by the server.
        if (clientSocket != null)
            this.serverSocket.send(payload, clientSocket);
    }
    
    
    /**
     * Sends a message to a client as a string.
     *
     * @param payload       a string to send.
     * @param clientSocket  the client to send to.
     *
     * @see jnet.JServerSocket
     */
    public void send(String payload, JSSLClientSocket clientSocket) {
        this.send(Bytes.stringToBytes(payload), clientSocket);
    }
    
    
    /**
     * Sends a message to all clients as a byte array.
     *
     * @param payload  a byte array to send to all clients.
     *
     * @see jnet.JServerSocket
     */
    public void sendAll(byte[] payload) {
        // Send a message to all clients based on the JClientSocket representations. The ClientSock
        // objects here allow use of the IN and OUT buffers to read/write messages. It is up to the
        // actual client on the client-side to call the .recv() method of ClientSock in order to
        // receive the data sent by the server.
        for (JSSLClientSocket clientSocket : this.clientConnections.keySet()) {
            if (clientSocket != null)
                this.serverSocket.send(payload, clientSocket);
        }
    }
    
    
    /**
     * Sends a message to all clients as a string.
     *
     * @param payload  a string to send to all clients.
     *
     * @see jnet.JServerSocket
     */
    public void sendAll(String payload) {
        this.sendAll(Bytes.stringToBytes(payload));
    }
    
    
    /**
     * Removes a connected client. If the argument {@code clientSocket} is null, no action is
     * taken. This method also interrupts the thread that was running for the given client.
     *
     * @param clientSocket  the client connection to remove.
     */
    public void remove(JSSLClientSocket clientSocket) {
        if (clientSocket == null)
            return;
        
        Thread clientThread = this.clientConnections.get(clientSocket);
        if (clientThread != null) {
            clientThread.interrupt();
            clientSocket.close();
            this.clientConnections.remove(clientSocket);
        }
    }
    
    
    /**
     * Unbinds this server server from its port. Before the server is closed, any clients
     * connected are disconnected with the {@code remove} method of this server.
     *
     * @see remove
     */
    public void close() {
        for (JSSLClientSocket clientSocket : this.clientConnections.keySet())
            this.remove(clientSocket);
        
        this.serverSocket.close();
    }
    
}
