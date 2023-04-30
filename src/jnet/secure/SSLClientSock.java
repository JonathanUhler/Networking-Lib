/*
package jnet.secure;


import jnet.Log;
import jnet.CRC;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.net.UnknownHostException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;


// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// public class SSLClientSock
//
// Secure C-style socket wrapper for clients
//
public class SSLClientSock {

	private SSLSocket clientSocket;
	private BufferedReader in;
	private PrintWriter out;


	// ----------------------------------------------------------------------------------------------------
	// public SSLClientSock
	//
	// Allows construction of a SSLClientSock object as usual. Using the second constructor, an existing
	// java Socket object can be provided instead
	//
	public SSLClientSock() {}
	// end: public SSLClientSock


	// ----------------------------------------------------------------------------------------------------
	// public SSLClientSock
	//
	// Arguments--
	//
	//  clientSock: a java Socket object used to begin the initialization of a SSLClientSock object
	//
	public SSLClientSock(SSLSocket clientSocket) {
		this.clientSocket = clientSocket;
	}
	// end: public SSLClientSock
	

	// ====================================================================================================
	// public InputStream getInputStream
	//
	// Returns the InputStream object from the locally stored java Socket object. This is provided so
	// servers can "listen" and "send" on the client socket. This function, unlike the C methods, is done
	// in java through the I/O streams, which needs to be taken from the client socket for the listen/send
	// calls
	//
	// Returns--
	//
	//  An InputStream object connected to the internal Socket object
	//
	public InputStream getInputStream() {
		if (this.clientSocket != null) {
			try {
				return this.clientSocket.getInputStream();
			}
			catch (IOException e) {
			    Log.stdlog(Log.ERROR, "SSLClientSock", "IOException thrown by getInputStream, returning null");
				Log.stdlog(Log.ERROR, "SSLClientSock", "\t" + e);
				return null;
			}
		}
		return null;
	}
	// end: public InputStream getInputStream


	// ====================================================================================================
	// public OutputStream getOutputStream
	//
	// Returns the OutputStream object from the local Socket object, similarly to the getInputStream method
	//
	// Returns--
	//
	//  An OutputStream object connected to the internal Socket object
	public OutputStream getOutputStream() {
		if (this.clientSocket != null) {
			try {
				return this.clientSocket.getOutputStream();
			}
			catch (IOException e) {
			    Log.stdlog(Log.ERROR, "SSLClientSock", "IOException thrown by getOutputStream, returning null");
			    Log.stdlog(Log.ERROR, "SSLClientSock", "\t" + e);
				return null;
			}
		}
		return null;
	}
	// end: public OutputStream getOutputStream


	// ====================================================================================================
	// public void connect
	//
	// Performs the action of the C socket connect() function, and wraps the creation of the java
	// I/O streams for communication
	//
	// Arguments--
	//
	//  ip:           an IP address to connect to
	//
	//  port:         a port to connect to
	//
	//  protocols:    supported TLS protocols
	//
	//  cipherSuites: supported TLS ciphers
	public void connect(String ip, int port, String[] protocols, String[] cipherSuites) {
		try {
			// Because a constructor is provided to take in an existing socket object, a null check is put
			// here to confirm the socket needs to be created. Otherwise, it is assumed the passed socket
			// contains the correct IP/port. Although, this method is not actually called in the case
			// where the second constructor is used
			if (this.clientSocket == null) {
				this.clientSocket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(ip, port);
				this.clientSocket.setEnabledProtocols(protocols);
				this.clientSocket.setEnabledCipherSuites(cipherSuites);
			}

			// This is java's way of handling socket I/O. The main purpose of this method is to wrap
			// the creation of these stream objects to make for a cleaner implementation
			this.in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
			this.out = new PrintWriter(this.clientSocket.getOutputStream(), true); // true for autoFlush
		}
		catch (IOException e) {
		    Log.stdlog(Log.ERROR, "SSLClientSock", "IOException thrown when initializing socket");
		    Log.stdlog(Log.ERROR, "SSLClientSock", "\t" + e);
		}
	}
	// end: public void connect


	// ====================================================================================================
	// public void send
	//
	// C-style send method
	//
	// Arguments--
	//
	//  msg: the message to send
	//
	public void send(String msg) {		
		if (this.out != null) {
			msg += CRC.getFor(msg); // Add checksum
			this.out.println(msg);
			return;
		}
		Log.stdlog(Log.ERROR, "SSLClientSock", "OUT was null, no message sent");
	}
	// end: public void send


	// ====================================================================================================
	// public String recv
	//
	// C-style receive method
	//
	// Returns--
	//
	//  A string if one was received, or null upon error
	//
	public String recv() {
		if (this.in != null) {
			try {
				String recv = this.in.readLine();

				if (recv != null) {
					boolean crcValid = CRC.check(recv); // Check checksum
					if (!crcValid) {
						Log.stdlog(Log.ERROR, "SSLClientSock", "CRC check failed");
						return null;
					}
					return recv.substring(0, recv.length() - CRC.NUM_NIBBLES);
				}

			    Log.stdlog(Log.WARN, "SSLClientSock", "Received \"null\", server probably closed");
				return null;
			}
			catch (IOException e) {
			    Log.stdlog(Log.ERROR, "SSLClientSock", "IOException thrown from IN.readLine(), returning null");
				Log.stdlog(Log.ERROR, "SSLClientSock", "\t" + e);
				return null;
			}
		}
	    Log.stdlog(Log.ERROR, "SSLClientSock", "IN was null, returning null");
		return null;
	}
	// end: public String recv


	// ====================================================================================================
	// public void close
	//
	// C-style close method
	//
	public void close() {
		if (this.clientSocket != null) {
			try {
				this.in.close();
				this.out.close();
				this.clientSocket.close();
			}
			catch (IOException e) {
			    Log.stdlog(Log.ERROR, "SSLClientSock", "clientSocket could not be closed");
			    Log.stdlog(Log.ERROR, "SSLClientSock", "\t" + e);
			}
		}
	}
	// end: public void close
	
}
// end: public class SSLClientSock
*/
