"""
C-style socket wrapper for servers.

Author: Jonathan Uhler
"""


import socket
from socket import SocketType
from pnet import crc
from pnet import header


class PServerSocket:
    """
    Socket wrapper for servers.
    """

    def __init__(self):
        """
        Constructs a new unbound `PServerSocket` object.
        """
        self.server_socket = None


    def bind(self, ip: str, port: int, backlog: int) -> None:
        """
        Binds to a given IP address and port.

        Arguments:
         ip (str):      the IP address to bind to.
         port (int):    the port to bind to.
         backlog (int): the number of pending connections to hold onto in a queue.
        """

        self.server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.server_socket.bind((ip, port))
        self.server_socket.listen(backlog)


    def accept(self) -> SocketType:
        """
        Waits for and accepts an incoming client connection.

        Returns:
         SocketType: the connecting client.

        Raises:
         socket.error: if a network error occurs.
        """

        client_connection, _ = self.server_socket.accept()
        return client_connection


    def send(self, payload: bytes, client_connection: SocketType) -> int:
        """
        Sends a message across the socket to a specific client.

        This method handles the use of a CRC and header automatically. The argument `payload`
        should not contain these elements. If they are present they will be ignored and treated as
        part of the payload. The argument byte array must also abide by any other preconditions
        demanded by the CRC and Header routines. An exception will be thrown upon any error.

        Arguments:
         payload (bytes):                a byte array to send.
         client_connection (SocketType): the client to send to.

        Returns:
         int: the number of bytes sent, including the length of the attached crc, header, and 
              possible pad bytes.

        Raises:
         TypeError:    if either argument is None.
         socket.error: if a network error occurs.
        """

        if (payload is None):
            raise TypeError("payload cannot be None")
        if (client_connection is None):
            raise TypeError("client_connection cannot be None")

        body: bytes = crc.attach(payload)
        message: bytes = header.attach(body)
        return client_connection.send(message)


    def recv(self, client_connection: SocketType) -> bytes:
        """
        Receives bytes across the socket.

        If the read could not be performed or either the header or crc check fails, an exception is
        thrown. This method validates and detaches the crc and header bytes if valid.

        When reading, `header.SIZE` bytes are first read and parsed as a `header.Info` object.
        If the header CRC check passes, `header.Info.size` bytes are read. If the CRC check passes
        for this body, the payload is returned.

        Arguments:
         client_connection (SocketType): the client to receive from.

        Returns:
         bytes: the latest message in the client's buffer. If there is no message to be received,
                `None` is returned.

        Raises:
         TypeError:    if `client_connection` is None.
         socket.error: if a network error occurs.
        """

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
        """
        Closes the socket connection.

        Raises:
         RuntimeError: if the connection cannot be closed.
        """

        if (self.server_socket is None):
            return

        try:
            self.server_socket.close()
        except socket.error as e:
            raise RuntimeError(f"cannot close socket: {e}") from e
