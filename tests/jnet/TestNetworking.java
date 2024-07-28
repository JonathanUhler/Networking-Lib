import java.util.Date;
import java.util.ArrayList;
import java.io.IOException;
import org.junit.Test;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.After;
import jnet.Bytes;
import jnet.JServer;
import jnet.JClientSocket;


public class TestNetworking {

    private class EchoServer extends JServer {
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


    private JServer server;


    @Test
    public void testServerConnectDisconnect() throws IOException {
        // We do not expect any exception here
        JServer server = new EchoServer("localhost", 9000);
        server.close();
    }

    @Test
    public void testServerGetters() throws IOException {
        JServer server = new EchoServer("localhost", 10000);
        Assert.assertEquals(server.getIP(), "localhost");
        Assert.assertEquals(server.getPort(), 10000);
        server.close();
    }

    @Test
    public void testClientSendBytesWhileDisconnected() throws IOException {
        JClientSocket client = new JClientSocket();
        int bytesSent = client.send(new byte[] {});
        Assert.assertEquals(bytesSent, -1);
    }

    @Test
    public void testClientSendStringWhileDisconnected() throws IOException {
        JClientSocket client = new JClientSocket();
        int bytesSent = client.send("");
        Assert.assertEquals(bytesSent, -1);
    }

    @Test
    public void testClientConnectDisconnect() throws IOException {
        JServer server = new EchoServer("localhost", 9000);
        JClientSocket client = new JClientSocket();
        client.connect(server.getIP(), server.getPort());
        client.close();
        server.close();
    }

    @Test
    public void testClientSendNullBytes() throws IOException {
        JClientSocket client = new JClientSocket();
        Assert.assertThrows(NullPointerException.class, () -> client.send((byte[]) null));
    }

    @Test
    public void testClientSendNullString() throws IOException {
        JClientSocket client = new JClientSocket();
        Assert.assertThrows(NullPointerException.class, () -> client.send((String) null));
    }

    @Test
    public void testClientSendRecvBytes() throws IOException {
        JServer server = new EchoServer("localhost", 9000);
        JClientSocket client = new JClientSocket();
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
    public void testClientSendRecvZeroBytes() throws IOException {
        JServer server = new EchoServer("localhost", 9000);
        JClientSocket client = new JClientSocket();
        client.connect(server.getIP(), server.getPort());

        byte[] expected = new byte[0];
        client.send(expected);
        byte[] generated = client.recv();
        Assert.assertArrayEquals(generated, expected);

        client.close();
        server.close();
    }

    @Test
    public void testClientSendRecvString() throws IOException {
        JServer server = new EchoServer("localhost", 9000);
        JClientSocket client = new JClientSocket();
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

    @Test
    public void testSendToNull() throws IOException {
        JServer server = new EchoServer("localhost", 9000);
        Assert.assertThrows(NullPointerException.class, () -> server.send(new byte[0], null));
        server.close();
    }

    @Test
    public void testSendNullPayload() throws IOException {
        JServer server = new EchoServer("localhost", 9000);
        Assert.assertThrows(NullPointerException.class, () -> server.send((byte[]) null, null));
        server.close();
    }

    @Test
    public void testSendToDisconnectedClient() throws IOException {
        JServer server = new EchoServer("localhost", 9000);
        JClientSocket client = new JClientSocket();
        Assert.assertThrows(IOException.class, () -> server.send(new byte[0], client));
        server.close();
    }

}
