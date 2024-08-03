import random
import pytest
import conftest
import socket
from pnet import byteutils
from pnet.pserver import PServer
from pnet.pclientsocket import PClientSocket


class EchoServer(PServer):
    def client_connected(self, client_socket: PClientSocket):
        pass

    def client_disconnected(self, client_socket: PClientSocket):
        pass

    def client_communicated(self, recv: bytes, client_socket: PClientSocket):
        self.send(recv, client_socket)
        


class TestNetworking:

    def test_server_connect_disconnect(self):
        server: PServer = EchoServer("localhost", 9000)
        server.close()

    def test_server_getters(self):
        server: PServer = EchoServer("localhost", 9000)
        assert server.get_ip() == "localhost"
        assert server.get_port() == 9000
        server.close()

    def test_client_send_bytes_while_disconnected(self):
        client: PClientSocket = PClientSocket()
        bytes_sent: int = client.send(bytes())
        assert bytes_sent == -1

    def test_client_send_string_while_disconnected(self):
        client: PClientSocket = PClientSocket()
        bytes_sent: int = client.send("")
        assert bytes_sent == -1

    def test_client_connect_disconnect(self):
        server: PServer = EchoServer("localhost", 9000)
        client: PClientSocket = PClientSocket()
        client.connect(server.get_ip(), server.get_port())
        client.close()
        server.close()

    def test_client_send_null_payload(self):
        client: PClientSocket = PClientSocket()
        with pytest.raises(TypeError):
            client.send(None)

    def test_client_send_recv_bytes(self):
        server: PServer = EchoServer("localhost", 9000)
        client: PClientSocket = PClientSocket()
        client.connect(server.get_ip(), server.get_port())

        for i in range(conftest.RANDOM_TEST_AMOUNT):
            expected: bytes = conftest.random_bytes(random.randint(0, 1024))
            client.send(expected)
            generated: bytes = client.recv()
            assert generated == expected

        client.close()
        server.close()

    def test_client_send_recv_zero_bytes(self):
        server: PServer = EchoServer("localhost", 9000)
        client: PClientSocket = PClientSocket()
        client.connect(server.get_ip(), server.get_port())

        expected: bytes = bytes()
        client.send(expected)
        generated: bytes = client.recv()
        assert generated == expected

        client.close()
        server.close()

    def test_client_send_recv_str(self):
        server: PServer = EchoServer("localhost", 9000)
        client: PClientSocket = PClientSocket()
        client.connect(server.get_ip(), server.get_port())

        for i in range(conftest.RANDOM_TEST_AMOUNT):
            expected: str = conftest.random_str(random.randint(0, 1024))
            client.send(expected)
            generated: str = client.srecv()
            assert generated == expected

        client.close()
        server.close()

    def test_many_clients_then_close(self):
        server: PServer = EchoServer("localhost", 9000)
        clients: list = []

        for i in range(PServer.BACKLOG):
            client: PClientSocket = PClientSocket()
            clients.append(client)
            client.connect(server.get_ip(), server.get_port())

            expected: str = conftest.random_str(random.randint(0, 1024))
            client.send(expected)
            generated: str = client.srecv()
            assert generated == expected

        for client in clients:
            client.close()
        server.close()

    def test_send_to_null(self):
        server: PServer = EchoServer("localhost", 9000)
        with pytest.raises(TypeError):
            server.send(bytes(), None)
        server.close()

    def test_send_null_payload(self):
        server: PServer = EchoServer("localhost", 9000)
        with pytest.raises(TypeError):
            server.send(None, None)
        server.close()
