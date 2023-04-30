/*
package jnet.secure;


import jnet.Log;
import javax.net.ssl.SSLSocket;
import java.lang.Thread;
import java.util.Map;
import java.util.HashMap;


// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// public abstract class SSLServer
//
// Example server for an SSL connection. Runs on 127.0.0.1 unless otherwise specified by the second
// constructor.
//
// Notable threads--
//
//  private void add(Socket) --> clientThread:   runs the SSLServer.listenOnClient(SSLClientSock) method
//                                               so the SSLServer.accept() method can run at the same
//                                               time
//
public abstract class SSLServer {
	
	public static final String IP_ADDR = "127.0.0.1";
	public static final int PORT = 9000;
	public static final int BACKLOG = 50;
	public static final String[] PROTOCOLS = new String[] {"TLSv1.2", "TLSv1.1", "TLSv1"};
    public static final String[] CIPHER_SUITES = new String[] {"TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA",
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
	

	private Map<SSLClientSock, Thread> clientConnections;
	private SSLServerSock serverSocket;
	private String ip;
	private int port;


	// ----------------------------------------------------------------------------------------------------
	// public SSLServer
	//
	public SSLServer() {
		this(SSLServer.IP_ADDR, SSLServer.PORT);
	}
	// end: public SSLServer


	// ----------------------------------------------------------------------------------------------------
	// public SSLServer
	//
	// Arguments--
	//
	//  ip:   the ip to start the server on
	//
	//  port: the port to start the server on
	//
	public SSLServer(String ip, int port) {
		this.ip = ip;
		this.port = port;
		
		this.clientConnections = new HashMap<>();
		
		this.serverSocket = new SSLServerSock();
		this.serverSocket.bind(this.ip, this.port, SSLServer.BACKLOG,
							   SSLServer.PROTOCOLS, SSLServer.CIPHER_SUITES);
		
		Thread acceptThread = new Thread(() -> this.accept());
		acceptThread.start();
	}
	// end: public SSLServer


	// ====================================================================================================
	// private void accept
	//
	// Accepts and adds incoming client connections
	//
	private void accept() {
		while (true) {
			Log.stdout(Log.INFO, "Server", "accept :: ready to handle incoming connection");
			SSLSocket clientConnection = this.serverSocket.accept();

			this.add(clientConnection);
		}
	}
	// end: private void accept


	// ====================================================================================================
	// private void add
	//
	// Adds a new client connection, spawns threads to handle communication with that client, and sends
	// the new client the current state of the board and their assigned color
	//
	// Arguments--
	//
	//  clientConnection: a java Socket object for the client that connected
	//
	private void add(SSLSocket clientConnection) {
		if (clientConnection == null)
			return;

		SSLClientSock clientSocket = new SSLClientSock(clientConnection);
		clientSocket.connect(this.ip, this.port, SSLServer.PROTOCOLS, SSLServer.CIPHER_SUITES);

		Thread clientThread = new Thread(() -> this.listenOnClient(clientSocket));
		this.clientConnections.put(clientSocket, clientThread);
		clientThread.start();
	}
	// end: private void add


	// ====================================================================================================
	// private void listenOnClient
	//
	// Listens on a specific client
	//
	// Arguments--
	//
	//  clientSocket: the ClientSock object to listen to
	//
	private void listenOnClient(SSLClientSock clientSocket) {
		while (true) {
			Log.stdlog(Log.INFO, "Server", "listenOnClient :: ready to process message from " + clientSocket);
			String recv = this.serverSocket.recv(clientSocket);
			if (recv == null) {
				this.clientConnections.remove(clientSocket);
				return; // Ignore this client once it has disconnected
			}

			Log.stdout(Log.INFO, "Server", "Received information from client");
			Log.stdout(Log.INFO, "Server", "\t" + recv);

			this.actOnClient(recv, clientSocket);
		}
	}
	// end: private void listenOnClient


	// ====================================================================================================
	// public abstract void actOnClient
	//
	// Performs some action given a client message. Responsible for using send or sendAll to give a response
	//
	// Arguments--
	//
	//  recv: the message from the client
	//
	public abstract void actOnClient(String recv, SSLClientSock clientSocket);
	// end: public abstract void actOnClient


	// ====================================================================================================
	// public void send
	//
	// Send a message to the given client
	//
	// Arguments--
	//
	//  messasge: the message to send
	//
	public void send(String message, SSLClientSock clientSocket) {
		// The SSLClientSock objects here allow use of the IN and OUT buffers to read/write messages. It is up to
		// the actual client on the client-side to call the .recv() method of ClientSock in order to receive the
		// data sent by the server.
		if (clientSocket != null)
			this.serverSocket.send(message, clientSocket);
	}
	// end: public void send
	

	// ====================================================================================================
	// public void sendAll
	//
	// Send a message to all connected clients
	//
	// Arguments--
	//
	//  messasge: the message to send
	//
	public void sendAll(String message) {
		// Send a message to all clients based on the SSLClientSock representations. The SSLClientSock objects
		// here allow use of the IN and OUT buffers to read/write messages. It is up to the actual client on the
		// client-side to call the .recv() method of ClientSock in order to receive the data sent by the server.
		for (SSLClientSock clientSocket : this.clientConnections.keySet()) {
			if (clientSocket != null)
				this.serverSocket.send(message, clientSocket);
		}
	}
	// end: public void sendAll


	// ====================================================================================================
	// public void remove
	//
	// Removes a client connection
	//
	// Argument--
	//
	//  clientSocket: the client connection to remove
	//
	public void remove(SSLClientSock clientSocket) {
		if (clientSocket == null)
			return;

		Thread clientThread = this.clientConnections.get(clientSocket);
		if (clientThread != null) {
			clientThread.interrupt();
			clientSocket.close();
		}
	}
	// end: public void remove

}
// end: public class SSLServer
*/
