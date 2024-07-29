import socket
from socket import SocketType
from threading import Thread, Event
from typing import Union, Final
from abc import ABC
from pnet import header
from pnet import byteutils
from pnet.pclientsocket import PClientSocket
from pnet.pserversocket import PServerSocket


class PServer(ABC):

    BACKLOG: Final = 50


    def __init__(self, ip: str, port: int):
        self.ip = ip
        self.port = port

        self.client_connections = {}

        self._bind()


    def _bind(self) -> None:
        self.server_socket = PServerSocket()
        self.server_socket.bind(self.ip, self.port, PServer.BACKLOG)

        accept_thread: Thread = Thread(target = self.accept)
        accept_thread.start()


    def get_ip(self) -> str:
        return self.ip


    def get_port(self) -> int:
        return self.port


    def _accept(self) -> None:
        while (True):
            try:
                client_connection: SocketType = self.server_socket.accept()
                self._add(client_connection)
            except socket.error:
                continue


    def _add(self, client_connection: str) -> None:
        if (client_connection is None):
            return

        client_socket: PClientSocket = PClientSocket(client_connection)
        try:
            client_socket.connect(self.ip, self.port)
        except socket.error:
            return

        thread_interrupter: Event = Event()
        client_thread: Thread = Thread(target = self._listen_on_client,
                                       args = (client_socket, thread_interrupter))
        self.client_connections[client_socket] = (client_thread, thread_interrupter)
        self.client_connected(client_socket)
        client_thread.start()


    @abstractmethod
    def client_connected(self, client_socket: PClientSocket) -> None:
        ...


    @abstractmethod
    def client_disconnected(self, client_socket: PClientSocket) -> None:
        ...


    def _listen_on_client(self, client_socket: PClientSocket, interrupter: Event) -> None:
        while (not interrupter.is_set()):
            recv: bytes = None
            try:
                recv = self.server_socket.recv(client_socket)
            except socket.error:
                continue

            if (recv is None):
                if (not interrupter.is_set()):
                    self.client_disconnected(client_socket)
                    del self.client_connections.remove[client_socket]
                return

            self.client_communicated(recv, client_socket)


    @abstractmethod
    def client_communicated(self, recv: bytes, client_socket: PClientSocket) -> None:
        ...


    def send(self, payload: Union[bytes, str], client_socket: PClientSocket) -> None:
        if (payload is None):
            raise TypeError("payload cannot be None")
        if (client_socket is None):
            raise TypeError("client_socket cannot be None")

        if (isinstance(payload, str)):
            payload = byteutils.str_to_bytes(payload)

        self.server_socket.send(payload, client_socket)


    def send_all(self, payload: Union[bytes, str]) -> None:
        for client_socket in self.client_connections:
            if (client_socket is not None):
                self.send(payload, client_socket)


    def remove(self, client_socket: PClientSocket) -> None:
        if (client_socket is None):
            raise TypeError("client_socket cannot be None")

        self._clean_up_client(client_socket)
        del self.client_connections[client_socket]


    def _clean_up_client(self, client_socket: PClientSocket) -> None:
        client_thread, thread_interrupter = self.client_connections.get(client_socket)
        if (client_thread is not None):
            thread_interrupter.set()
        client_socket.close()


    def close(self) -> None:
        for client_socket in self.client_connections:
            self._clean_up_client(client_socket)

        self.client_connections.clear()
        self.server_socket.close()
