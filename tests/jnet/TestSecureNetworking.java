import java.util.Date;
import java.util.ArrayList;
import java.io.IOException;
import org.junit.Test;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.After;
import jnet.Bytes;
import jnet.secure.JSSLServer;
import jnet.secure.JSSLClientSocket;
import jnet.JClientSocket;


public class TestSecureNetworking {

    private class EchoServer extends JSSLServer {
        public EchoServer(String ip, int port) throws IOException {
            super(ip, port);
        }

        @Override
        public void clientConnected(JClientSocket clientSocket) {}

        @Override
        public void clientDisconnected(JClientSocket clientSocket) {}

        @Override
        public void clientCommunicated(byte[] recv, JClientSocket clientSocket) {
            try {
                this.send(recv, clientSocket);
            }
            catch (IOException e) {
                Assert.fail("send in EchoServer threw IOException: " + e);
            }
        }
    }


    @Test
    public void testServerConnectDisconnect() throws IOException {
        JSSLServer server = new EchoServer("localhost", 9000);
        server.close();
    }

    @Test
    public void testClientConnectDisconnect() throws IOException {
        JSSLServer server = new EchoServer("localhost", 9000);
        JSSLClientSocket client = new JSSLClientSocket();
        client.connect(server.getIP(), server.getPort());
        client.close();
        server.close();
    }

    @Test
    public void testClientSendRecvBytes() throws IOException {
        JSSLServer server = new EchoServer("localhost", 9000);
        JSSLClientSocket client = new JSSLClientSocket();
        client.connect(server.getIP(), server.getPort());

        for (int i = 0; i < TestMain.RANDOM_TEST_AMOUNT; i++) {
            byte[] expected = TestMain.randomBytes(TestMain.RANDOM_DIS.nextInt(1024));
            client.send(expected);
            byte[] generated = client.recv();
            Assert.assertArrayEquals(generated, expected);
        }

        client.close();
        server.close();
    }

    @Test
    public void testClientSendRecvString() throws IOException {
        JSSLServer server = new EchoServer("localhost", 9000);
        JSSLClientSocket client = new JSSLClientSocket();
        client.connect(server.getIP(), server.getPort());

        for (int i = 0; i < TestMain.RANDOM_TEST_AMOUNT; i++) {
            String expected = TestMain.randomString(TestMain.RANDOM_DIS.nextInt(1024));
            client.send(expected);
            String generated = client.srecv();
            Assert.assertEquals(generated, expected);
        }

        client.close();
        server.close();
    }

}
