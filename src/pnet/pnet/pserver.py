"""
C-style server template.

This class provides the framework to create a server compatible with the processes of this library.
For more information, see the doc-comments of the Java Networking-Lib implementation (jnet.JServer).

Author: Jonathan Uhler
"""


import socket
from socket import SocketType
from threading import Thread, Event
from typing import Union, Final
from abc import ABC, abstractmethod
from pnet import byteutils
from pnet.pclientsocket import PClientSocket
from pnet.pserversocket import PServerSocket


class PServer(ABC):
    """
    An abstract class to create servers.
    """

    BACKLOG: Final = 50


    def __init__(self, ip: str, port: int):
        """
        Constructs a new `PServer` with a given IP address and port.

        Arguments:
         ip (str)    the IP address to start the server on.
         port (int): the port to start the server on.

        Raises:
         socket.error: if a network error occurs.
        """

        self.ip = ip
        self.port = port

        self.client_connections = {}
        self.accept_interrupter = None

        self._bind()


    def _bind(self) -> None:
        """
        Binds this server's socket to the server's address.

        This is a private function and should not be called outside of `pserver.py`.
        """

        self.server_socket = PServerSocket()
        self.server_socket.bind(self.ip, self.port, PServer.BACKLOG)

        self.accept_interrupter: Event = Event()
        accept_thread: Thread = Thread(target = self._accept)
        accept_thread.start()


    def get_ip(self) -> str:
        """
        Returns the IP address the server has been bound to.

        Returns:
         str: the IP address the server has been bound to.
        """

        return self.ip


    def get_port(self) -> int:
        """
        Returns the port the server has been bound to.

        Returns:
         int: the port the server has been bound to.
        """

        return self.port


    def _accept(self) -> None:
        """
        Waits for and accepts incoming client connections.

        This is a private function and should not be called outside of `pserver.py`.

        This method runs in the main thread of this `PServer` object. Other operations are
        managed by separate secondary threads.
        """

        while (not self.accept_interrupter.is_set()):
            try:
                client_connection: SocketType = self.server_socket.accept()
                self._add(client_connection)
            except socket.error:
                continue


    def _add(self, client_connection: SocketType) -> None:
        """
        Processes a new client connection.

        This is a private function and should not be called outside of `pserver.py`.

        Arguments:
         client_connection (SocketType): the client connection.
        """

        if (client_connection is None):
            return

        thread_interrupter: Event = Event()
        client_thread: Thread = Thread(target = self._listen_on_client,
                                       args = (client_connection, thread_interrupter))
        self.client_connections[client_connection] = (client_thread, thread_interrupter)
        self.client_connected(client_connection)
        client_thread.start()


    @abstractmethod
    def client_connected(self, client_socket: PClientSocket) -> None:
        """
        Performs an arbitrary action when a client first connects.

        Called by the private `PServer._add` method before a client's thread is started.

        Arguments:
         client_socket (PClientSocket): the client that connected.
        """


    @abstractmethod
    def client_disconnected(self, client_socket: PClientSocket) -> None:
        """
        Performs an arbitrary action when a client disconnects.

        Called by the private `PServer._listen_on_client` method before the client is removed
        from this `PServer`'s scope. At the time this method is called, the client may or may
        not be reachable through `PServer.send`.

        Arguments:
         client_socket (PClientSocket): the client that disconnected.
        """


    def _listen_on_client(self, client_socket: PClientSocket, interrupter: Event) -> None:
        """
        Listens on a specific client.

        This is a private function and should not be called outside of `pserver.py`.

        Arguments:
         client_socket (PClientSocket): the client to listen to.
         interrupter (Event):           an interrupter that, when set, will stop the listening
                                        loop and exit this method.
        """
        while (not interrupter.is_set()):
            recv: bytes = None
            try:
                recv = self.server_socket.recv(client_socket)
            except socket.error:
                continue

            if (recv is None):
                if (not interrupter.is_set()):
                    self.client_disconnected(client_socket)
                    self.client_connections[client_socket] = (None, None)
                return

            self.client_communicated(recv, client_socket)


    @abstractmethod
    def client_communicated(self, recv: bytes, client_socket: PClientSocket) -> None:
        """
        Performs an arbitrary action when a client sends a message.

        Called by the private `PServer._listen_on_client` method after validating the received
        message. The argument `recv` is guaranteed to contain a valid, non-null message.

        Arguments:
         recv (bytes):                  the message received from the client.
         client_socket (PClientSocket): the client who sent the message.
        """


    def send(self, payload: Union[bytes, str], client_socket: PClientSocket) -> None:
        """
        Sends a message to a client as a byte array.

        Arguments:
         payload (Union[bytes, str]):   the message to send.
         client_socket (PClientSocket): the client to send to.

        Raises:
         TypeError:    if either argument is None.
         socket.error: if a network error occurs.
        """

        if (payload is None):
            raise TypeError("payload cannot be None")
        if (client_socket is None):
            raise TypeError("client_socket cannot be None")

        if (isinstance(payload, str)):
            payload = byteutils.str_to_bytes(payload)

        self.server_socket.send(payload, client_socket)


    def send_all(self, payload: Union[bytes, str]) -> None:
        """
        Sends a message to all clients connected to this `PServer`.

        Arguments:
         payload (Union[bytes, str]): the message to send.

        Raises:
         TypeError:    if `payload` is None.
         socket.error: if a network error occurs.
        """

        for client_socket in self.client_connections:
            if (client_socket is not None):
                self.send(payload, client_socket)


    def remove(self, client_socket: PClientSocket) -> None:
        """
        Disconnects and removes a connected client.

        This method also interrupts the internal thread that was running to listen to the given
        client.

        Arguments:
         client_socket (PClientSocket): the client connection to remove.

        Raises:
         TypeError: if `client_socket` is None.
        """

        if (client_socket is None):
            raise TypeError("client_socket cannot be None")

        self._clean_up_client(client_socket)
        del self.client_connections[client_socket]


    def _clean_up_client(self, client_socket: PClientSocket) -> None:
        """
        Cleans up the resources for a given client, but does not remove the client.

        The clean up process involves disconnecting the client and terminating its listen thread.
        To avoid possible concurrency issues (between threads or while iterating over clients),
        this method does not modify the `client_connections` map.

        Arguments:
         client_socket (PClientSocket): the client to clean up.
        """

        client_thread, thread_interrupter = self.client_connections.get(client_socket)
        if (client_thread is not None):
            thread_interrupter.set()
        client_socket.close()


    def close(self) -> None:
        """
        Unbinds this server from its port.

        Before the server is closed, any clients are disconnected.
        """

        for client_socket in self.client_connections:
            self._clean_up_client(client_socket)

        self.accept_interrupter.set()
        self.client_connections.clear()
        self.server_socket.close()
