"""
C-style server template with SSL security support.

This class provides the framework to create a server compatible with the processes of this library.
For more information, see the doc-comments of the Java Networking-Lib implementation of this class
(jnet.secure.JSSLServer).

Author: Jonathan Uhler
"""


from threading import Thread, Event
from pnet.pserver import PServer
from pnet.secure.psslserversocket import PSSLServerSocket


class PSSLServer(PServer):
    """
    A secure implementation of `PServer`.
    """

    def __init__(self, ip: str, port: int, certfile: str, keyfile: str):
        """
        Constructs a new `PSSLServer` with a given IP address and port.

        Arguments:
         ip (str)        the IP address to start the server on.
         port (int):     the port to start the server on.
         certfile (str): the path to the SSL certificate file.
         keyfile (str):  the path to the SSL keyfile.

        Raises:
         socket.error: if a network error occurs.
        """

        self.certfile = certfile
        self.keyfile = keyfile
        super().__init__(ip, port)


    def _bind(self) -> None:
        """
        Binds this server's socket to the server's address.
        """

        self.server_socket = PSSLServerSocket()
        self.server_socket.bind(self.ip, self.port, PServer.BACKLOG, self.certfile, self.keyfile)

        self.accept_interrupter: Event = Event()
        accept_thread: Thread = Thread(target = self._accept)
        accept_thread.start()
