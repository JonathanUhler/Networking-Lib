package jnet;


import java.net.Socket;
import java.net.UnknownHostException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;


/**
 * C-style socket wrapper for clients.
 *
 * @author Jonathan Uhler
 */
public class JClientSocket {

	private Socket clientSocket;
	private InputStream in;
	private OutputStream out;


	/**
	 * Constructs an uninitialized {@code JClientSocket} object. The internal {@code Socket} representation
	 * will only be initialized upon the call to {@code JClientSocket::connect()}.
	 *
	 * @see connect
	 */
	public JClientSocket() {}


	/**
	 * Constructs a partially initialized {@code JClientSocket} object. The {@code JClientSocket::connect()} method
	 * must still be called to fully initialize this object, but the optional argument allows for a
	 * pre-defined connection to be used instead.
	 *
	 * @param clientSocket a java {@code Socket} object used to begin the initialization of a 
	 *        {@code JClientSocketet} object.
	 *
	 * @see connect
	 */
	public JClientSocket(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}
	

	/**
	 * Returns the InputStream object from the locally stored java Socket object. This is provided so
	 * servers can "listen" and "send" on the client socket. This function, unlike the C methods, is done
	 * in java through the I/O streams, which needs to be taken from the client socket for the listen/send
	 * calls.
	 *
	 * @return an {@code InputStream} object connected to the internal {@code Socket} object.
	 */
	public InputStream getInputStream() {
		if (this.clientSocket == null || this.in == null)
			return null;
		return this.in;
	}


	/**
	 * Returns the OutputStream object from the locally stored java Socket object.
	 *
	 * @return an {@code OutputStream} object connected to the internal {@code Socket} object.
	 *
	 * @see getInputStream
	 */
	public OutputStream getOutputStream() {
		if (this.clientSocket == null || this.out == null)
			return null;
		return this.out;
	}


	/**
	 * Connects this socket to a destination address. Performs the action of the C socket {@code connect()}
	 * by wrapping the creation of the java I/O streams. If the connection fails, an error is logged.
	 *
	 * @param ip an IP address to connect to.
	 * @param port a port to connect to.
	 *
	 * @throws IOException if the java {@code Socket} object cannot be initialized.
	 *
	 * @see jnet.Log
	 */
	public void connect(String ip, int port) throws IOException {
		try {
			// Because a constructor is provided to take in an existing socket object, a null check is put
			// here to confirm the socket needs to be created. Otherwise, it is assumed the passed socket
			// contains the correct IP/port. Although, this method is not actually called in the case
			// where the second constructor is used
			if (this.clientSocket == null)
				this.clientSocket = new Socket(ip, port);

			// This is java's way of handling socket I/O. The main purpose of this method is to wrap
			// the creation of these stream objects to make for a cleaner implementation
			this.in = this.clientSocket.getInputStream();
			this.out = this.clientSocket.getOutputStream();
		}
		catch (IOException e) {
			Log.stdlog(Log.ERROR, "JClientSocket", "IOException thrown when initializing socket");
			Log.stdlog(Log.ERROR, "JClientSocket", "\t" + e);
			throw e;
		}
	}


	/**
	 * Sends a payload across the socket. This method integrates CRC and header options on its own.
	 * These elements should not be added by the caller (if they are, they will be ignored and treated
	 * as part of the message payload). The payload must contain at least 1 byte. If an error occurs
	 * with the CRC generation, header generation, payload structure, or send operation, a
	 * corresponding error will be logged and -1 returned.
	 *
	 * @param payload a byte array to send.
	 *
	 * @return the number of bytes sent, including the length of the attached crc, header, and possible pad
	 *         bytes.
	 *
	 * @see jnet.Log
	 * @see jnet.CRC
	 * @see jnet.Header
	 */
	public int send(byte[] payload) {
		if (this.out == null) {
			Log.stdlog(Log.ERROR, "JClientSocket", "OUT was null, no message sent");
			return -1;
		}
		
		try {
			byte[] body = CRC.attach(payload);
			if (body == null)
				return -1;
			byte[] message = Header.attach(body);
			if (message == null)
				return -1;
			this.out.write(message);
			this.out.flush();
			return message.length;
		}
		catch (IOException e) {
			Log.stdlog(Log.ERROR, "JClientSocketet", "cannot send bytes: " + e);
			return -1;
		}
	}


	/**
	 * Sends a string across the socket. Identical to {@code JClientSocket::send(Bytes.stringToBytes(payload))}.
	 *
	 * @param payload a string to send
	 *
	 * @return the number of bytes sent, including the length of the attached crc, header, and possible pad
	 *         bytes.
	 *
	 * @see send(byte[])
	 */
	public int send(String payload) {
		return this.send(Bytes.stringToBytes(payload));
	}


	/**
	 * Receives bytes across the socket. If the read could not be performed or either the header or crc check
	 * fails, {@code null} is returned an an error is logged. This method validates and detaches the
	 * crc and header bytes if valid.
	 * <p>
	 * When reading, {@code Header.SIZE} bytes are first read and parsed as a {@code Header.Info} object.
	 * If the header CRC check passes, {@code Header.Info::size} bytes are read. If the CRC check
	 * passes for this body, the payload is returned.
	 *
	 * @return the received bytes.
	 *
	 * @see jnet.Log
	 */
	public byte[] recv() {
		if (this.in == null) {
			Log.stdlog(Log.ERROR, "JClientSocket", "IN was null, no message received");
			return null;
		}
		
		try {
			// Read header
			byte[] header = new byte[Header.SIZE];
			int headerSize = this.in.read(header);
			if (headerSize <= 0) {
				Log.stdlog(Log.WARN, "JClientSocket", "Received no header bytes, server probably closed");
				return null;
			}

			// Validate header
		    Header.Info info = Header.validateAndParse(header);
			if (info == null)
				return null;

			// Read message
			byte[] body = new byte[info.size];
			int bodySize = this.in.read(body);
			if (bodySize != body.length) {
				Log.stdlog(Log.ERROR, "JClientSocket", "Unable to recv full body. Expected " + body.length +
						   " bytes, found " + bodySize + " bytes");
				return null;
			}

			// Validate and return message
			byte[] payload = CRC.checkAndRemove(body);
			return payload;
		}
		catch (IOException e) {
			Log.stdlog(Log.ERROR, "JClientSocket", "IOException thrown from IN.readLine(), returning null");
			return null;
		}
	}


	/**
	 * Receives bytes across the socket and parses them as a string. Identical to 
	 * {@code Bytes.bytesToString(JClientSocket::recv())}. This implies {@code null} may be returned if the bytes
	 * cannot be parsed as a string for any reason.
	 *
	 * @return a string representation of the bytes read.
	 *
	 * @see recv
	 */
	public String srecv() {
		return Bytes.bytesToString(this.recv());
	}


	/**
	 * Closes the socket connection. If the connection cannot be closed, an error is logged.
	 *
	 * @see jnet.Log
	 */
	public void close() {
		if (this.clientSocket != null) {
			try {
				this.in.close();
				this.out.close();
				this.clientSocket.close();
			}
			catch (IOException e) {
				Log.stdlog(Log.ERROR, "JClientSocket", "clientSocket could not be closed");
				Log.stdlog(Log.ERROR, "JClientSocket", "\t" + e);
			}
		}
	}
	
}
