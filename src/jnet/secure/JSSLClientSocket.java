package jnet.secure;


import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.net.UnknownHostException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import jnet.CRC;
import jnet.Header;
import jnet.Bytes;
import jnet.JClientSocket;


/**
 * C-style socket wrapper for clients with SSL security support.
 *
 * @author Jonathan Uhler
 */
public class JSSLClientSocket extends JClientSocket {

    /**
     * Constructs an uninitialized {@code JSSLClientSocket} object. The internal {@code SSLSocket} 
     * representation will only be initialized upon the call to {@code JSSLClientSocket::connect()}.
     *
     * @see connect
     */
    public JSSLClientSocket() {}
    
    
    /**
     * Constructs a partially initialized {@code JSSLClientSocket} object. The 
     * {@code JSSLClientSocket::connect()} method must still be called to fully initialize this 
     * object, but the optional argument allows for a pre-defined connection to be used instead.
     *
     * @param clientSocket  a java {@code SSLSocket} object used to begin the initialization of a 
     *                      {@code JSSLClientSocketet} object.
     *
     * @see connect
     */
    public JSSLClientSocket(SSLSocket clientSocket) {
        super(clientSocket);
    }


    /**
     * Connects this socket to a destination address using the default {@code JSSLServer}
     * protocols and cipher suites.
     *
     * @param ip    an IP address to connect to.
     * @param port  a port to connect to.
     *
     * @throws IOException  if the java {@code SSLSocket} object cannot be initialized.
     */
    @Override
    public void connect(String ip, int port) throws IOException {
        this.connect(ip, port, JSSLServer.PROTOCOLS, JSSLServer.CIPHER_SUITES);
    }
    
    
    /**
     * Connects this socket to a destination address. Performs the action of the C socket 
     * {@code connect()} by wrapping the creation of the java I/O streams.
     *
     * @param ip            an IP address to connect to.
     * @param port          a port to connect to.
     * @param protocols     a list of security protocol names to use.
     * @param cipherSuites  a list of cipher names to use.
     *
     * @throws IOException  if the java {@code SSLSocket} object cannot be initialized.
     */
    public void connect(String ip,
                        int port,
                        String[] protocols,
                        String[] cipherSuites) throws IOException
    {
        // Because a constructor is provided to take in an existing socket object, a null
        // check is put here to confirm the socket needs to be created. Otherwise, it is
        // assumed the passed socket contains the correct IP/port. Although, this method is
        // not actually called in the case where the second constructor is used
        if (super.clientSocket == null) {
            SSLSocket sslClientSocket =
                (SSLSocket) SSLSocketFactory.getDefault().createSocket(ip, port);
            sslClientSocket.setEnabledProtocols(protocols);
            sslClientSocket.setEnabledCipherSuites(cipherSuites);
            super.clientSocket = sslClientSocket;
        }
	
        // This is java's way of handling socket I/O. The main purpose of this method is to
        // wrap the creation of these stream objects to make for a cleaner implementation
        super.in = super.clientSocket.getInputStream();
        super.out = super.clientSocket.getOutputStream();
    }
    
}
