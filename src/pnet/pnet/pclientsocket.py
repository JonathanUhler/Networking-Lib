"""
C-style socket wrapper for clients.

Author: Jonathan Uhler
"""


import socket
from socket import SocketType
from typing import Union
from pnet import crc
from pnet import header
from pnet import byteutils


class PClientSocket:
    """
    A socket for client connections.
    """

    def __init__(self, client_socket: SocketType = None):
        """
        Constructs a new `PClientSocket` object.

        The internal `SocketType` representation will only be initialized upon calling `connect`.

        Arguments:
         client_socket (SocketType): an optional socket object used to begin the initialization of
                                     this `PClientSocket` object.
        """

        self.client_socket = client_socket


    def connect(self, ip: str, port: int) -> None:
        """
        Connects this socket to a destination address.

        Arguments:
         ip (str)    an IP address to connect to.
         port (int): a port to connect to.

        Raises:
         socket.error: if the connection cannot be established.
        """

        if (self.client_socket is None):
            self.client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.client_socket.connect((ip, port))


    def send(self, payload: Union[bytes, str]) -> int:
        """
        Sends a byte payload across the socket.

        This method integrates CRC and header parsing on its own. These elements should not be
        added by the caller (if they are, they will be ignored and treated as part of the message
        payload). An exception will be thrown upon error.

        Arguments:
         payload (Union[bytes, str]): a byte array or string (which will be implicitly converted
                                      to a byte array) to send.

        Returns:
         int: the number of bytes sent, including the length of the attached crc, header, and
              possible pad bytes. If the socket is not yet initialized, -1 is returned.

        Raises:
         TypeError:          if `payload` is None.
         socket.error:       if a network error occurs.
         MalformedDataError: if an error occurs with CRC generation.
        """

        if (payload is None):
            raise TypeError("payload cannot be None")
        if (self.client_socket is None):
            return -1

        if (isinstance(payload, str)):
            payload = byteutils.str_to_bytes(payload)

        body: bytes = crc.attach(payload)
        message: bytes = header.attach(body)
        return self.client_socket.send(message)


    def recv(self) -> bytes:
        """
        Receives bytes across the socket.

        If the read could not be performed or either the header or crc check fails, an exception
        is thrown. This method validates and detaches the crc and header bytes if valid.

        When reading, `header.SIZE` bytes are first read and parsed as a `header.Info` object. If
        the header CRC check passes, `header.Info.size` bytes are read. If the CRC check passes for
        the body, the payload is returned.

        Returns:
         bytes: the received bytes.

        Raises:
         socket.error:       if a network error occurs.
         MalformedDataError: if a CRC check fails.
        """

        if (self.client_socket is None):
            return None

        header_bytes: bytes = self.client_socket.recv(header.SIZE)
        header_size: int = len(header_bytes)
        if (header_size <= 0):
            return None
        info: header.Info = header.validate_and_parse(header_bytes)

        body: bytes = self.client_socket.recv(info.size)
        payload: bytes = crc.check_and_remove(body)
        return payload


    def srecv(self) -> str:
        """
        Receives bytes across the socket and parses them as a string.

        Returns:
         str: a string representation of the bytes read.

        Raises:
         socket.error: if a network error occurs

        See:
         recv
        """

        return byteutils.bytes_to_str(self.recv())


    def close(self) -> None:
        """
        Closes the socket connection.

        If the connection cannot be closed, a `RuntimeError` is thrown.

        Raises:
         RunetimeError: if the connection cannot be closed.
        """

        if (self.client_socket is None):
            return

        try:
            self.client_socket.close()
        except socket.error as e:
            raise RuntimeError(f"cannot close socket: {e}") from e
