"""
C-style socket wrapper for clients with SSL security support.

Author: Jonathan Uhler
"""


import ssl
from ssl import SSLContext
import socket
from socket import SocketType
from pnet.pclientsocket import PClientSocket


class PSSLClientSocket(PClientSocket):
    """
    A secure implementation of `PClientSocket`.
    """

    def __init__(self, cafile: str, client_socket: SocketType = None):
        """
        Constructs a new `PSSLClientSocket` object.

        The internal `SocketType` representation will only be initialized upon calling `connect`.

        Arguments:
         cafile (str):               the path to the certificate authority file for this client to
                                     verify against upon attempting a connection.
         client_socket (SocketType): an optional socket object used to begin the initialization of
                                     this `PClientSocket` object.
        """

        self.ssl_context = SSLContext(ssl.PROTOCOL_TLS_CLIENT)
        self.ssl_context.load_verify_locations(cafile = cafile)
        if (client_socket is not None):
            client_socket = self.ssl_context.wrap_socket(client_socket)
        super().__init__(client_socket = client_socket)


    def connect(self, ip: str, port: int) -> None:
        """
        Connects this socket to a destination address with SSL security support.
        """

        if (self.client_socket is None):
            unsecure_socket: SocketType = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.client_socket = self.ssl_context.wrap_socket(unsecure_socket, server_hostname = ip)
        self.client_socket.connect((ip, port))
