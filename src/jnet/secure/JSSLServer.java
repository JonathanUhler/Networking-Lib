package jnet.secure;


import javax.net.ssl.SSLSocket;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;
import jnet.JServer;


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
public abstract class JSSLServer extends JServer {
    
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


    public JSSLServer(String ip, int port) throws IOException {
        super(ip, port);
    }
    

    @Override
    protected void bind() throws IOException {
        JSSLServerSocket sslServerSocket = new JSSLServerSocket();
        sslServerSocket.bind(super.ip, super.port, JServer.BACKLOG,
                             JSSLServer.PROTOCOLS, JSSLServer.CIPHER_SUITES);
        super.serverSocket = sslServerSocket;
        
        // Start the accept method in a new thread. This allows more constructor code to be
        // added is desired
        Thread acceptThread = new Thread(() -> super.accept());
        acceptThread.start();
    }
    
}
