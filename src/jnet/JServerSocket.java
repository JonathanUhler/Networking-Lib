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
     * Waits for and accepts an incoming client connection.
     *
     * @return a {@code Socket} object of the connecting client.
     *
     * @throws IOException  if a network error occurs.
     */
    public Socket accept() throws IOException {
        return this.serverSocket.accept();
    }
    
    
    /**
     * Sends a message across the socket to a specific client.
     *
     * This method handles the use of a CRC and header automatically. The argument {@code payload}
     * should not contain these elements. If they are present they will be ignored and treated as
     * part of the payload. The argument byte array must also abide by any other preconditions
     * demanded by the CRC and Header routines. An exception will be thrown upon any error.
     *
     * @param payload           a byte array to send.
     * @param clientConnection  the client to send to.
     *
     * @return the number of bytes sent, including the length of the attached crc, header, and 
     *         possible pad bytes.
     *
     * @throws NullPointerException  if either argument is null.
     * @throws IOException           if a network error occurs.
     *
     * @see jnet.CRC
     * @see jnet.Header
     */
    public int send(byte[] payload, JClientSocket clientConnection) throws IOException {
        if (payload == null) {
            throw new NullPointerException("payload cannot be null");
        }
        if (clientConnection == null) {
            throw new NullPointerException("clientConnection cannot be null");
        }

        OutputStream out = clientConnection.getOutputStream();
        if (out == null) {
            throw new IOException("clientConnection output stream is null, client is disconnected");
        }
        byte[] body = CRC.attach(payload);

        byte[] message = Header.attach(body);
        out.write(message);
        out.flush();
        return message.length;
    }
    
    
    /**
     * Receives bytes across the socket.
     *
     * If the read could not be performed or either the header or crc check fails, an exception is
     * thrown. This method validates and detaches the crc and header bytes if valid.
     *
     * When reading, {@code Header.SIZE} bytes are first read and parsed as a {@code Header.Info} 
     * object. If the header CRC check passes, {@code Header.Info::size} bytes are read. If the 
     * CRC check passes for this body, the payload is returned.
     *
     * @param clientConnection  the client to receive from
     *
     * @return the latest message in the client's buffer. If there is no message to be received,
     *         {@code null} will be returned.
     *
     * @throws NullPointerException  if {@code clientConnection} is null.
     * @throws IOException           if a network error occurs.
     */
    public byte[] recv(JClientSocket clientConnection) throws IOException {
        InputStream in = clientConnection.getInputStream();
        if (in == null) {
            throw new IOException("clientConnection input stream is null, client is disconnected");
        }

        byte[] header = new byte[Header.SIZE];
        int headerSize = in.read(header);
        if (headerSize == -1) {
            return null;
        }
        Header.Info info = Header.validateAndParse(header);

        byte[] body = new byte[info.size];
        int bodySize = in.read(body);
        byte[] payload = CRC.checkAndRemove(body);
        return payload;
    }
    
    
    /**
     * Closes the socket connection.
     *
     * @throws RuntimeException  if the connection cannot be closed.
     */
    public void close() {
        try {
            this.serverSocket.close();
        }
        catch (IOException e) {
            throw new RuntimeException("cannot close socket: " + e);
        }
    }
    
}
