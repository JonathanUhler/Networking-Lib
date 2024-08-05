"""
Utilities to handle CRC32 generation, application, and verification for network safety.

For more information on the `header.py` utilities, see the equivalent implements and doc-comments
in the Java Networking-Lib implementation (jnet.Header).

Author: Jonathan Uhler
"""


from typing import Final
from pnet import crc
from pnet import byteutils
from pnet.error import MissingDataError, MalformedDataError


SIZE: Final = 8 + crc.NUM_BYTES
HEADER_BYTE: Final = b"\x68"
BODY_LENGTH_OFFSET: Final = 4
BODY_LENGTH_SIZE: Final = 4
CRC_OFFSET: Final = 8
CRC_SIZE: Final = crc.NUM_BYTES


class Info:
    """
    Wrapper structure for the byte-array representation of a header.

    The constructor of this class automatically validates the header byte-array for correctness.

    Params:
     id (bytes): a single byte representing the identifier for this type of header.
     size (int): the number of bytes in the payload (including any CRC).
     crc (int):  the checksum for the header, which is validated by the constructor.
    """

    def __init__(self, header: bytes):
        """
        Constructs a `header.Info` object from a header byte array.

        This constructor validates the information contained within the argument `header`. If the
        data can be validated the instance variables of this class are set appropriately. Upon
        error an exception is raised.

        Arguments:
         header (bytes): the header to validate and parse.

        Raises:
         TypeError:          if `header` is None.
         MissingDataError:   if the header length is not `Header.SIZE`.
         MalformedDataError: if the CRC in the header is invalid.
         MalformedDataError: if the payload size in the header is negative.
        """

        if (header is None):
            raise TypeError("header cannot be None")
        if (len(header) != SIZE):
            raise MissingDataError(f"invalid header length, expected {SIZE}, found {len(header)}")
        if (not crc.check(header)):
            raise MalformedDataError("invalid header crc")

        size_bytes: bytes = header[BODY_LENGTH_OFFSET:(BODY_LENGTH_OFFSET + BODY_LENGTH_SIZE)]
        crc_bytes: bytes = header[CRC_OFFSET:(CRC_OFFSET + CRC_SIZE)]

        self.id = bytes([header[0]])
        self.size = byteutils.bytes_to_int(size_bytes)
        self.crc = byteutils.bytes_to_int(crc_bytes)

        if (self.size < 0):
            raise MalformedDataError(f"invalid payload size: {self.size}")



def _generate(body: bytes) -> bytes:
    """
    Generates a header for a given body.

    This is a private function and should not be called outside of `header.py`.
    
    The CRC bytes attached to `body` WILL be validated by this method. If the CRC is found to be
    invalid, an exception is thrown.
    
    Arguments:
     body (bytes): a payload and crc to generate a header for

    Returns:
     bytes: a byte array containing the header for `body`.

    Raises:
     MalformedDataError: if the body has an invalid CRC.
    """

    has_valid_crc: bool = crc.check(body)
    if (not has_valid_crc):
        raise MalformedDataError("body has invalid crc")

    length: int = len(body)
    length_bytes: bytes = byteutils.int_to_bytes(length)

    header: bytes = HEADER_BYTE + b"\x00\x00\x00" + length_bytes
    header = crc.attach(header)
    return header


def attach(body: bytes) -> bytes:
    """
    Creates and attaches a header to the argument body.

    A new byte array is returned containing `header + body` in that order. The contents of `body`
    are not modified.

    Arguments:
     body (bytes): the message body (which must contain a crc) to generate and attach a header to.

    Returns:
     bytes: a new array containing `header + body` in that order.

    Raises:
     TypeError:          if `body` is None.
     MalformedDataError: if the body has an invalid CRC.
    """

    if (body is None):
        raise TypeError("body cannot be None")

    header: bytes = _generate(body)
    message: bytes = header + body
    return message


def validate_and_parse(header: bytes) -> Info:
    """
    Validates and parses the content of a given header as a `header.Info` object.

    "Validation" includes a check of the header crc. Upon any validation error, an exception
    is thrown. If the validation succeeds, the data in the struct is guaranteed to be valid.

    Arguments:
     header (bytes): the header to validate.

    Returns:
     Info: a `header.Info` struct containing all the information of the header in more accessible
           public instance variables.
    """

    return Info(header)
