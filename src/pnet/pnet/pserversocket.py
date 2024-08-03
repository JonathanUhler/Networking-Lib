import socket
from socket import SocketType
from pnet import crc
from pnet import header
from pnet.pclientsocket import PClientSocket


class PServerSocket:

    def __init__(self):
        self.server_socket = None

    def bind(self, ip: str, port: int, backlog: int) -> None:
        self.server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.server_socket.bind((ip, port))
        self.server_socket.listen(backlog)


    def accept(self) -> SocketType:
        client_connection, _ = self.server_socket.accept()
        return client_connection


    def send(self, payload: bytes, client_connection: SocketType) -> int:
        if (payload is None):
            raise TypeError("payload cannot be None")
        if (client_connection is None):
            raise TypeError("client_connection cannot be None")

        body: bytes = crc.attach(payload)
        message: bytes = header.attach(body)
        return client_connection.send(message)


    def recv(self, client_connection: SocketType) -> bytes:
        if (client_connection is None):
            raise TypeError("client_connection cannot be None")

        header_bytes: bytes = client_connection.recv(header.SIZE)
        header_size: int = len(header_bytes)
        if (header_size <= 0):
            return None
        info: header.Info = header.validate_and_parse(header_bytes)

        body: bytes = client_connection.recv(info.size)
        payload: bytes = crc.check_and_remove(body)
        return payload


    def close(self) -> None:
        if (self.server_socket is None):
            return

        try:
            self.server_socket.close()
        except socket.error as e:
            raise RuntimeError(f"cannot close socket: {e}") from e

    
