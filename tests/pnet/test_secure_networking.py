import os
import random
import pytest
import conftest
import socket
from pnet import byteutils
from pnet.secure.psslserver import PSSLServer
from pnet.secure.psslclientsocket import PSSLClientSocket


class EchoServer(PSSLServer):
    def client_connected(self, client_socket: PSSLClientSocket):
        pass

    def client_disconnected(self, client_socket: PSSLClientSocket):
        pass

    def client_communicated(self, recv: bytes, client_socket: PSSLClientSocket):
        self.send(recv, client_socket)


certfile: str = os.path.abspath(os.environ["PNET_CERTFILE"])
keyfile: str = os.path.abspath(os.environ["PNET_KEYFILE"])


class TestNetworking:

    def test_server_connect_disconnect(self):
        server: PSSLServer = EchoServer("localhost", 9000, certfile, keyfile)
        server.close()

    def test_client_connect_disconnect(self):
        server: PSSLServer = EchoServer("localhost", 9000, certfile, keyfile)
        client: PSSLClientSocket = PSSLClientSocket(certfile)
        client.connect(server.get_ip(), server.get_port())
        client.close()
        server.close()

    def test_client_send_recv_bytes(self):
        server: PSSLServer = EchoServer("localhost", 9000, certfile, keyfile)
        client: PSSLClientSocket = PSSLClientSocket(certfile)
        client.connect(server.get_ip(), server.get_port())

        for i in range(conftest.RANDOM_TEST_AMOUNT):
            expected: bytes = conftest.random_bytes(random.randint(0, 1024))
            client.send(expected)
            generated: bytes = client.recv()
            assert generated == expected

        client.close()
        server.close()

    def test_client_send_recv_str(self):
        server: PSSLServer = EchoServer("localhost", 9000, certfile, keyfile)
        client: PSSLClientSocket = PSSLClientSocket(certfile)
        client.connect(server.get_ip(), server.get_port())

        for i in range(conftest.RANDOM_TEST_AMOUNT):
            expected: str = conftest.random_str(random.randint(0, 1024))
            client.send(expected)
            generated: str = client.srecv()
            assert generated == expected

        client.close()
        server.close()
