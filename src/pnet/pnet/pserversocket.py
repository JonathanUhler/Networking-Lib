import socket
from socket import SocketType
from pnet import crc
from pnet import header
from pnet import byteutils
from pnet.pclientsocket import PClientSocket


class PServerSocket:

    def bind(self, ip: str, port: int, backlog: int) -> None:
        raise NotImplementedError("pserver is not yet implemented in pnet")


    def accept(self) -> SocketType:
        raise NotImplementedError("pserver is not yet implemented in pnet")


    def send(self, payload: bytes, client_connection: PClientSocket) -> int:
        raise NotImplementedError("pserver is not yet implemented in pnet")


    def recv(self, client_connection: PClientSocket) -> bytes:
        raise NotImplementedError("pserver is not yet implemented in pnet")


    def close(self) -> None:
        raise NotImplementedError("pserver is not yet implemented in pnet")

    
