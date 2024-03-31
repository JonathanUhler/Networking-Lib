package jnet;


import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;


/**
 * C-style socket wrapper for servers.
 *
 * @author Jonathan Uhler
 */
public class JServerSocket {
    
    private ServerSocket serverSocket;
    
    
    /**
     * Binds to a given IP address and port.
     *
     * @param ip       the IP address to bind to.
     * @param port     the port to bind to.
     * @param backlog  the number of pending connections to hold onto in a queue.
     *
     * @throws IOException  if the java {@code ServerSocket} cannot be created.
     */
    public void bind(String ip, int port, int backlog) throws IOException {
        this.serverSocket = new ServerSocket(port, backlog, InetAddress.getByName(ip));
    }
    
    
    /**
     * Waits for and accepts an incoming client connection. If an error occurs, {@code null}
     * is returned.
     *
     * @return a {@code Socket} object of the connecting client.
     */
    public Socket accept() {
        try {
            if (this.serverSocket != null)
                return this.serverSocket.accept();
        }
        catch (IOException e) {
            return null;
        }
	
        return null;
    }
    
    
    /**
     * Sends a message across the socket to a specific client. This method handles the use of a 
     * CRC and header automatically. The argument {@code payload} should not contain these 
     * elements. If they are present they will be ignored and treated as part of the payload. The 
     * argument byte array must also contain at least 1 byte, and abide by any other preconditions 
     * demanded by the CRC and Header routines. Upon any server-side error, {@code -1} will be 
     * returned.
     *
     * @param payload           a byte array to send.
     * @param clientConnection  the client to send to.
     *
     * @return the number of bytes sent, including the length of the attached crc, header, and 
     *         possible pad bytes.
     *
     * @see jnet.CRC
     * @see jnet.Header
     */
    public int send(byte[] payload, JClientSocket clientConnection) {
        if (clientConnection == null)
            return -1;
        
        try {
            OutputStream out = clientConnection.getOutputStream();
            byte[] body = CRC.attach(payload);
            if (body == null)
                return -1;
            byte[] message = Header.attach(body);
            out.write(message);
            out.flush();
            return message.length;
        }
        catch (IOException e) {
            return -1;
        }
    }
    
    
    /**
     * Receives bytes across the socket. If the read could not be performed or either the header 
     * or crc check fails, {@code null} is returned. This method validates and detaches the crc and
     * header bytes if valid.
     * <p>
     * When reading, {@code Header.SIZE} bytes are first read and parsed as a {@code Header.Info} 
     * object. If the header CRC check passes, {@code Header.Info::size} bytes are read. If the 
     * CRC check passes for this body, the payload is returned.
     *
     * @param clientConnection  the client to receive from
     *
     * @return the latest message in the client's buffer.
     */
    public byte[] recv(JClientSocket clientConnection) {
        try {
            InputStream in = clientConnection.getInputStream();
            
            // Read header
            byte[] header = new byte[Header.SIZE];
            int headerSize = in.read(header);
            if (headerSize <= 0)
                return null;
            
            // Validate header
            Header.Info info = Header.validateAndParse(header);
            if (info == null)
                return null;
            
            // Read message
            byte[] body = new byte[info.size];
            int bodySize = in.read(body);
            if (bodySize != body.length)
                return null;
            
            // Validate and return message
            byte[] payload = CRC.checkAndRemove(body);
            return payload;
        }
        catch (IOException e) {
            return null;
        }
        catch (NullPointerException e) {
            return null;
        }
    }
    
    
    /**
     * Closes the socket connection. If the connection cannot be closed, a {@code RuntimeException}
     * is thrown.
     */
    public void close() {
        if (this.serverSocket != null) {
            try {
                this.serverSocket.close();
            }
            catch (IOException e) {
                throw new RuntimeException("cannot close socket: " + e);
            }
        }
    }
    
}
