package jnet.secure;


import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.net.InetAddress;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import jnet.CRC;
import jnet.Header;
import jnet.JServerSocket;


/**
 * C-style socket wrapper for servers with SSL security support.
 *
 * @author Jonathan Uhler
 */
public class JSSLServerSocket extends JServerSocket {

    /**
     * Binds to a given IP address and port with the default {@code JServer} protocols and
     * cipher suites.
     *
     * @param ip       the IP address to bind to.
     * @param port     the port to bind to.
     * @param backlog  the number of pending connections to hold onto in a queue.
     *
     * @throws IOException  if the java {@code ServerSocket} cannot be created.
     */
    @Override
    public void bind(String ip, int port, int backlog) throws IOException {
        this.bind(ip, port, backlog, JSSLServer.PROTOCOLS, JSSLServer.CIPHER_SUITES);
    }

    
    /**
     * Binds to a given IP address and port. This method performs a secure bind using the
     * specified security protocols and cipher suites.
     *
     * @param ip            the IP address to bind to.
     * @param port          the port to bind to.
     * @param backlog       the number of pending connections to hold onto in a queue.
     * @param protocols     a list of security protocol names to use.
     * @param cipherSuites  a list of cipher names to use.
     *
     * @throws IOException  if the java {@code ServerSocket} cannot be created.
     *
     * @see jnet.secure.JSSLServer
     */
    public void bind(String ip,
                     int port,
                     int backlog,
                     String[] protocols,
                     String[] cipherSuites) throws IOException
    {
        
        SSLServerSocket sslServerSocket =
            (SSLServerSocket) SSLServerSocketFactory
            .getDefault()
            .createServerSocket(port, backlog, InetAddress.getByName(ip));
        sslServerSocket.setEnabledProtocols(protocols);
        sslServerSocket.setEnabledCipherSuites(cipherSuites);
        super.serverSocket = sslServerSocket;
    }
    
}
