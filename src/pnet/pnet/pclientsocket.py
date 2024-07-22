import socket
from socket import SocketType
from typing import Union
from pnet import crc
from pnet import header
from pnet import byteutils


class PClientSocket:

    def __init__(self, client_socket: SocketType = None):
        if (not client_socket is None):
            self.client_socket = client_socket
        self.client_socket = None


    def connect(self, ip: str, port: int) -> None:
        if (self.client_socket is None):
            self.client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.client_socket.connect((ip, port))


    def send(self, payload: Union[bytes, str]) -> int:
        if (self.client_socket is None):
            return -1

        if (isinstance(payload, str)):
            payload = byteutils.str_to_bytes(payload)

        try:
            body: bytes = crc.attach(payload)
            if (body is None):
                return -1
            message: bytes = header.attach(body)
            if (message is None):
                return -1
            return self.client_socket.send(message)
        except socket.error:
            return -1


    def recv(self) -> bytes:
        if (self.client_socket is None):
            return None

        try:
            header_bytes: bytes = self.client_socket.recv(header.SIZE)
            header_size: int = len(header_bytes)
            if (header_size <= 0):
                return None

            info: header.Info = header.validate_and_parse(header_bytes)
            if (info is None):
                return None

            body: bytes = self.client_socket.recv(info.size)
            payload: bytes = crc.check_and_remove(body)
            return payload
        except socket.error:
            return None


    def srecv(self) -> str:
        return byteutils.bytes_to_str(self.recv())


    def close(self) -> None:
        if (not self.client_socket is None):
            try:
                self.client_socket.close()
            except socket.error as e:
                raise RuntimeError(f"cannot close socket: {e}") from e
