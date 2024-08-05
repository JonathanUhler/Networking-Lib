"""
C-style socket wrapper for servers with SSL security support.

Author: Jonathan Uhler
"""


import ssl
from ssl import SSLContext
import socket
from socket import SocketType
from pnet.pserversocket import PServerSocket


class PSSLServerSocket(PServerSocket):
    """
    A secure implementation of `PServerSocket`.
    """

    def bind(self, ip: str, port: int, backlog: int, certfile: str, keyfile: str) -> None:
        """
        Binds to a given IP address and port.

        Arguments:
         ip (str):       the IP address to bind to.
         port (int):     the port to bind to.
         backlog (int):  the number of pending connections to hold onto in a queue.
         certfile (str): the path to the SSL certificate file.
         keyfile (str):  the path to the SSL keyfile.
        """

        ssl_context: SSLContext = SSLContext(ssl.PROTOCOL_TLS_SERVER)
        ssl_context.load_cert_chain(certfile = certfile, keyfile = keyfile)
        unsecure_socket: SocketType = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.server_socket = ssl_context.wrap_socket(unsecure_socket, server_side = True)
        self.server_socket.bind((ip, port))
        self.server_socket.listen(backlog)
