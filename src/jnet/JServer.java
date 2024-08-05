package jnet;


import java.net.Socket;
import java.lang.Thread;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.io.IOException;


/**
 * C-style server template. This class provides the framework to create a server compatible with the
 * processes of this library.
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
 *
 * @see clientConnected
 * @see clientCommunicated
 *
 * @author Jonathan Uhler
 */
public abstract class JServer {

    /** The maximum number of clients that can be placed in the backlog buffer. */
    public static final int BACKLOG = 50;
    
    
    private Map<JClientSocket, Thread> clientConnections;
    protected JServerSocket serverSocket;
    protected String ip;
    protected int port;
    
    
    /**
     * Constructs a new {@code JServer} with a given IP address and port.
     *
     * @param ip    the IP address to start the server on.
     * @param port  the port to start the server on.
     *
     * @throws IOException  if the server cannot be bound to the IP address and port.
     */
    public JServer(String ip, int port) throws IOException {
        this.ip = ip;
        this.port = port;
	
        this.clientConnections = new HashMap<>();

        this.bind();
    }


    /**
     * Binds this server's socket to the server's address.
     */
    protected void bind() throws IOException {
        this.serverSocket = new JServerSocket();
        this.serverSocket.bind(this.ip, this.port, JServer.BACKLOG);
        
        // Start the accept method in a new thread. This allows more constructor code to be
        // added is desired
        Thread acceptThread = new Thread(() -> this.accept());
        acceptThread.start();
    }
    
    
    /**
     * Returns the IP address the server has been bound to.
     *
     * @return the IP address the server has been bound to.
     */
    public String getIP() {
        return this.ip;
    }
    
    
    /**
     * Returns the port this server has been bound to.
     *
     * @return the port this server has been bound to.
     */
    public int getPort() {
        return this.port;
    }
    
    
    /**
     * Waits for and accepts incoming client connections.
     *
     * This method runs in the main thread of this {@code JServer} object. Other operations are
     * managed by separate secondary threads.
     */
    protected void accept() {
        // Unconditionally wait for and accept client connections, then assign them an id/place in
        // the array and create a ClientSock object to represent that conneciton and allow .send()
        // calls towards that client
        while (true) {
            try {
                Socket clientConnection = this.serverSocket.accept();
                this.add(clientConnection);
            }
            catch (IOException e) {
                continue;
            }
        }
    }
    
    
    /**
     * Processes a new client connection.
     *
     * Processing includes creating a new thread to handle communication with that client and
     * putting the client into the {@code clientConnections} map. Because the
     * {@code JClientSocket::connect} method is volatile under normal use, an IOException may be
     * thrown. This error is caught by this method, although it should never occur if the server
     * was able to bind successfully to the IP address and port given in the constructor.
     *
     * @param clientConnection  a java {@code Socket} object for the client that connected.
     */
    private void add(Socket clientConnection) {
        if (clientConnection == null) {
            return;
        }
        
        JClientSocket clientSocket = new JClientSocket(clientConnection);
        try {
            clientSocket.connect(this.ip, this.port);
        }
        catch (IOException e) {
            return;
        }
        
        // Start each client with an individual thread that calls a localized message parsing
        // method in this Server object. This allows the server to sit each client in a while(true)
        // loop calling recv to get data from the client until something comes back.
        Thread clientThread = new Thread(() -> this.listenOnClient(clientSocket));

        this.clientConnections.put(clientSocket, clientThread);
        this.clientConnected(clientSocket);
        clientThread.start();
    }
    
    
    /**
     * Performs an arbitrary action when a client first connects.
     *
     * Called by the private {@code JServer::add} method before the client's thread is started.
     *
     * @param clientSocket  the client that connected.
     */
    public abstract void clientConnected(JClientSocket clientSocket);
    
    
    /**
     * Performs an arbitrary action when a client disconnects.
     *
     * Called by the private {@code JServer::listenOnClient} method before the client is removed
     * from this {@code JServer}'s scope. At the time this method is called, the client may
     * or may not be reachable through {@code JServer::send}.
     *
     * @param clientSocket  the client that disconnected.
     */
    public abstract void clientDisconnected(JClientSocket clientSocket);
    
    
    /**
     * Listens on a specific client.
     *
     * @param clientSocket  the {@code JClientSocket} object to listen to.
     */
    protected void listenOnClient(JClientSocket clientSocket) {
        while (!Thread.currentThread().isInterrupted()) {
            byte[] recv;
            try {
                recv = this.serverSocket.recv(clientSocket);
            }
            catch (IOException e) {
                continue;
            }

            if (recv == null) {
                if (!Thread.currentThread().isInterrupted()) {
                    this.clientDisconnected(clientSocket);
                    this.clientConnections.remove(clientSocket);
                }
                return;
            }
            
            this.clientCommunicated(recv, clientSocket);
        }
    }
    
    
    /**
     * Performs an arbitrary action when a client sends a message.
     *
     * Called by the private {@code Server::listenOnClient} method after validating the received
     * message. The argument {@code recv} is guaranteed to contain a valid, non-null message.
     *
     * @param recv          the message received from the client
     * @param clientSocket  the client who sent the message
     */
    public abstract void clientCommunicated(byte[] recv, JClientSocket clientSocket);
    
    
    /**
     * Sends a message to a client as a byte array.
     *
     * @param payload       a byte array to send.
     * @param clientSocket  the client to send to.
     *
     * @throws NullPointerException  if either argument is null.
     * @throws IOException           if a network error occurs.
     *
     * @see jnet.JServerSocket
     */
    public void send(byte[] payload, JClientSocket clientSocket) throws IOException {
        if (payload == null) {
            throw new NullPointerException("payload cannot be null");
        }
        if (clientSocket == null) {
            throw new NullPointerException("clientSocket cannot be null");
        }

        // The JClientSocket objects here allow use of the IN and OUT buffers to read/write
        // messages. It is up to the actual client on the client-side to call the .recv() method
        // of ClientSock in order to receive the data sent by the server.
        this.serverSocket.send(payload, clientSocket);
    }
    
    
    /**
     * Sends a message to a client as a string.
     *
     * @param payload       a string to send.
     * @param clientSocket  the client to send to.
     *
     * @throws IOException  if a network error occurs.
     *
     * @see jnet.JServerSocket
     */
    public void send(String payload, JClientSocket clientSocket) throws IOException {
        this.send(Bytes.stringToBytes(payload), clientSocket);
    }
    
    
    /**
     * Sends a message to all clients as a byte array.
     *
     * @param payload  a byte array to send to all clients.
     *
     * @throws IOException  if a network error occurs.
     *
     * @see jnet.JServerSocket
     */
    public void sendAll(byte[] payload) throws IOException {
        // Send a message to all clients based on the JClientSocket representations. The ClientSock
        // objects here allow use of the IN and OUT buffers to read/write messages. It is up to the
        // actual client on the client-side to call the .recv() method of ClientSock in order to
        // receive the data sent by the server.
        for (JClientSocket clientSocket : this.clientConnections.keySet()) {
            if (clientSocket != null) {
                this.serverSocket.send(payload, clientSocket);
            }
        }
    }
    
    
    /**
     * Sends a message to all clients as a string.
     *
     * @param payload  a string to send to all clients.
     *
     * @throws IOException  if a network error occurs.
     *
     * @see jnet.JServerSocket
     */
    public void sendAll(String payload) throws IOException {
        this.sendAll(Bytes.stringToBytes(payload));
    }
    
    
    /**
     * Removes a connected client.
     *
     * This method also interrupts the internal thread that was running to listen to the given
     * client.
     *
     * @param clientSocket  the client connection to remove.
     *
     * @throws NullPointerException  if {@code clientSocket} is null.
     */
    public void remove(JClientSocket clientSocket) {
        if (clientSocket == null) {
            throw new NullPointerException("clientSocket cannot be null");
        }
        
        this.cleanUpClient(clientSocket);
        this.clientConnections.remove(clientSocket);
    }


    /**
     * Cleans up the resources for a given client, but does not remove the client.
     *
     * The clean up process involves disconnecting the client and terminating its listen thread.
     * To avoid possible concurrency issues (between threads or while iterating over clients),
     * this method does not modify the {@code clientConnections} map.
     *
     * @param clientSocket  the client to clean up.
     */
    private void cleanUpClient(JClientSocket clientSocket) {
        Thread clientThread = this.clientConnections.get(clientSocket);
        if (clientThread != null) {
            clientThread.interrupt();
        }
        clientSocket.close();
    }
    
    
    /**
     * Unbinds this server from its port.
     *
     * Before the server is closed, any clients connected are disconnected with the
     * {@code remove} method of this server.
     *
     * @see remove
     */
    public void close() { 
        for (JClientSocket clientSocket : this.clientConnections.keySet()) {
            // We don't use JServer::remove here because doing so would cause a case of concurrent
            // modification with this loop. Instead, we simply clean up each client socket, and
            // clear out the list of client connections after iteration is complete.
            this.cleanUpClient(clientSocket);
        }

        this.clientConnections.clear();
        this.serverSocket.close();
    }
    
}
