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
    
    protected Socket clientSocket;
    protected InputStream in;
    protected OutputStream out;
    
    
    /**
     * Constructs an uninitialized {@code JClientSocket} object.
     *
     * The internal {@code Socket} representation will only be initialized upon the call to
     * {@code JClientSocket::connect()}.
     *
     * @see connect
     */
    public JClientSocket() {}
    
    
    /**
     * Constructs a partially initialized {@code JClientSocket} object.
     *
     * The {@code JClientSocket::connect()} method must still be called to fully initialize this
     * object, but the optional argument allows for a pre-defined connection to be used instead.
     *
     * @param clientSocket  a java {@code Socket} object used to begin the initialization of a 
     *                      {@code JClientSocketet} object.
     *
     * @see connect
     */
    public JClientSocket(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }
    
    
    /**
     * Returns the InputStream object from the locally stored java Socket object.
     *
     * This is provided so servers can "listen" and "send" on the client socket. This function,
     * unlike the C methods, is done in java through the I/O streams, which need to be taken from
     * the client socket for the listen/send calls.
     *
     * If this client socket is not yet connected, {@code null} will be returned.
     *
     * @return an {@code InputStream} object connected to the internal {@code Socket} object.
     */
    public InputStream getInputStream() {
        if (this.clientSocket == null || this.in == null) {
            return null;
        }
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
        if (this.clientSocket == null || this.out == null) {
            return null;
        }
        return this.out;
    }
    
    
    /**
     * Connects this socket to a destination address.
     *
     * Performs the action of the C socket {@code connect()} by wrapping the creation of the
     * java I/O streams. If the connection fails, an {@code IOException} is thrown.
     *
     * @param ip    an IP address to connect to.
     * @param port  a port to connect to.
     *
     * @throws IOException  if the java {@code Socket} object cannot be initialized.
     */
    public void connect(String ip, int port) throws IOException {
        // Because a constructor is provided to take in an existing socket object, a null
        // check is put here to confirm the socket needs to be created. Otherwise, it is
        // assumed the passed socket contains the correct IP/port. Although, this method is
        // not actually called in the case where the second constructor is used
        if (this.clientSocket == null) {
            this.clientSocket = new Socket(ip, port);
        }
	
        // This is java's way of handling socket I/O. The main purpose of this method is to
        // wrap the creation of these stream objects to make for a cleaner implementation
        this.in = this.clientSocket.getInputStream();
        this.out = this.clientSocket.getOutputStream();
    }
    
    
    /**
     * Sends a byte payload across the socket.
     *
     * This method integrates CRC and header parsing on its own. These elements should not be
     * added by the caller (if they are, they will be ignored and treated as part of the message
     * payload). An exception will be thrown upon error.
     *
     * @param payload  a byte array to send.
     *
     * @return the number of bytes sent, including the length of the attached crc, header, 
     *         and possible pad bytes. If this socket is not yet initialized, -1 is returned.
     *
     * @throws NullPointerException    if {@code payload} is null.
     * @throws IOException             if a network error occurs.
     * @throws MalformedDataException  if an error occurs with CRC generation.
     *
     * @see jnet.CRC
     * @see jnet.Header
     */
    public int send(byte[] payload) throws IOException {
        if (payload == null) {
            throw new NullPointerException("payload cannot be null");
        }
        if (this.out == null) {
            return -1;
        }

        byte[] body = CRC.attach(payload);
        byte[] message = Header.attach(body);

        this.out.write(message);
        this.out.flush();
        return message.length;
    }
    
    
    /**
     * Sends a string across the socket.
     *
     * This is identical to {@code JClientSocket::send(Bytes.stringToBytes(payload))}.
     *
     * @param payload  a string to send
     *
     * @return the number of bytes sent, including the length of the attached crc, header, 
     *         and possible pad bytes.
     *
     * @see send(byte[])
     *
     * @throws IOException  if a network error occurs.
     */
    public int send(String payload) throws IOException {
        return this.send(Bytes.stringToBytes(payload));
    }
    
    
    /**
     * Receives bytes across the socket.
     *
     * If the read could not be performed or either the header or crc check fails, an exception
     * is thrown. This method validates and detaches the crc and header bytes if valid.
     *
     * When reading, {@code Header.SIZE} bytes are first read and parsed as a {@code Header.Info} 
     * object. If the header CRC check passes, {@code Header.Info::size} bytes are read. If the 
     * CRC check passes for this body, the payload is returned.
     *
     * If this client socket is not connected or there isn't a message to be received,
     * {@code null} is returned.
     *
     * @return the received bytes.
     *
     * @throws IOException             if a network error occurs.
     * @throws MalformedDataException  if a CRC check fails.
     */
    public byte[] recv() throws IOException {
        if (this.in == null) {
            return null;
        }

        byte[] header = new byte[Header.SIZE];
        int headerSize = this.in.read(header);
        if (headerSize == -1) {
            return null;
        }
        Header.Info info = Header.validateAndParse(header);

        byte[] body = new byte[info.size];
        int bodySize = this.in.read(body);

        byte[] payload = CRC.checkAndRemove(body);
        return payload;
    }
    
    
    /**
     * Receives bytes across the socket and parses them as a string.
     *
     * This is identical to {@code Bytes.bytesToString(JClientSocket::recv())}.
     *
     * @return a string representation of the bytes read.
     *
     * @throws IOException  if a network error occurs.
     *
     * @see recv
     */
    public String srecv() throws IOException {
        return Bytes.bytesToString(this.recv());
    }
    
    
    /**
     * Closes the socket connection.
     *
     * If the connection cannot be closed, a {@code RuntimeException} is thrown.
     *
     * @throws RuntimeException  if the connection cannot be closed.
     */
    public void close() {
        if (this.clientSocket == null) {
            return;
        }

        try {
            this.in.close();
            this.out.close();
            this.clientSocket.close();
        }
        catch (IOException e) {
            throw new RuntimeException("cannot close socket: " + e);
        }
    }

}
