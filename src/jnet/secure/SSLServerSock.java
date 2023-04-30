/*
package jnet.secure;


import jnet.Log;
import jnet.CRC;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.net.InetAddress;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;


// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// public class SSLServerSock
//
// Secure C-style server socket wrapper
//
public class SSLServerSock {

	private SSLServerSocket serverSocket;


	// ====================================================================================================
	// public void bind
	//
	// Binds to a given ip/port and sets the listen backlog
	//
	// Arguments--
	//
	//  ip:           the ip address to bind to
	//
	//  port:         the port to bind to
	//
	//  backlog:      the number of pending connections to hold onto in the queue
	//
	//  protocols:    the supported TLS protocols
	//
	//  cipherSuites: the supported TLS ciphers
	//
	public void bind(String ip, int port, int backlog, String[] protocols, String[] cipherSuites) {
		try {
			this.serverSocket = (SSLServerSocket) SSLServerSocketFactory
				.getDefault()
				.createServerSocket(port, backlog, InetAddress.getByName(ip));
			this.serverSocket.setEnabledProtocols(protocols);
			this.serverSocket.setEnabledCipherSuites(cipherSuites);
		}
		catch (IOException e) {
			Log.stdlog(Log.ERROR, "SSLServerSock", "IOException thrown when initializing socket");
		    Log.stdlog(Log.ERROR, "SSLServerSock", "\t" + e);
		}
	}
	// end: public void bind


	// ====================================================================================================
	// public Socket accept
	//
	// C-style accept method
	//
	// Returns--
	//
	//  A Socket object if the connection was successful, otherwise null
	//
	public SSLSocket accept() {
		try {
			if (this.serverSocket != null)
				return (SSLSocket) this.serverSocket.accept();
		}
		catch (IOException e) {
			Log.stdlog(Log.ERROR, "SSLServerSock", "IOException thrown on accept call, returning null");
			return null;
		}

	    Log.stdlog(Log.ERROR, "SSLServerSock", "no socket returned in accept(), serverSocket might be null");
		return null;
	}
	// end: public Socket accept


	// ====================================================================================================
	// public void send
	//
	// C-style send method
	//
	// Arguments--
	//
	//  message:          the message to send
	//
	//  clientConnection: the client to send to
	public void send(String message, SSLClientSock clientConnection) {
		try {
			PrintWriter out = new PrintWriter(clientConnection.getOutputStream(), true); // true for autoFlush
			message += CRC.getFor(message); // Add checksum
			out.println(message);
		}
		catch (NullPointerException e) {
		    Log.stdlog(Log.WARN, "SSLServerSock", "NullPointerException during send, client dropped the connection");
		}
	}
	// end: public void send


	// ====================================================================================================
	// public String recv
	//
	// C-style recv method
	//
	// Arguments--
	//
	//  clientConnection: the client to receive from
	//
	// Returns--
	//
	//  The received message if successful, otherwise null
	//
	public String recv(SSLClientSock clientConnection) {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));
			String recv = in.readLine(); // Convert from char array to string

			if (recv != null) {
				boolean crcValid = CRC.check(recv); // Check checksum
				if (!crcValid) {
					Log.stdlog(Log.ERROR, "SSLServerSock", "CRC check failed, disconnecting client");
					return null;
				}
				return recv.substring(0, recv.length() - CRC.NUM_NIBBLES);
			}

			// If recv is null, the client disconnected
			return null;
		}
		catch (IOException e) {
		    Log.stdlog(Log.WARN, "SSLServerSock", "IOException thrown from readLine(), client probably disconnected");
		    Log.stdlog(Log.WARN, "SSLServerSock", "\t" + e);
			return null;
		}
		catch (NullPointerException e) {
		    Log.stdlog(Log.WARN, "SSLServerSock", "NullPointException during recv, client dropped the connection");
			return null;
		}
	}
	// end: public String recv
	
}
// end: public class SSLServerSock
*/
