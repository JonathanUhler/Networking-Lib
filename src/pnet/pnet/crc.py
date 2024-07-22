"""
Utilities to handle CRC32 generation, application, and vertification for network safety.

Throughout the methods within this class, the following notation is used to refer to data 
contained in byte arrays:

`header` - a header to allow for safe data I/O. See jnet.Header.
`payload` - a byte array containing the data being sent or received through the  socket. This is
            what is ultimately expected by the programmer and is the  only component of the message
            returned by the send/recv methods of the socket-oriented classes in this library.
`crc` - a 4-byte checksum of `payload`. The argument to the method  `crc32` is referred to as
        `bytearr` to provide a more generic name, but this method is almost exclusively used to
        generate a checksum for `payload`.
`body` - a byte array containing `payload + crc` in that order.
`message` - the full message sent across the socket, comprised of `header + body` in that order.

Author: Jonathan Uhler
"""


import binascii
from typing import Final
from pnet import byteutils


NUM_NIBBLES: Final = 8
NUM_BYTES: Final = NUM_NIBBLES // 2


def crc32(bytearr: bytes) -> int:
    """
    Generates a 4 byte CRC as an integer. This function is a wrapper for the use
    of `binascii.crc32`.

    Args:
     bytearr (bytes): the bytes to generate a checksum for

    Returns:
     int: a 4 byte checksum as an int that is assumed to be unsigned.
    """

    return binascii.crc32(bytearr)


def _generate(payload: bytes) -> bytes:
    """
    Generates a 4-byte CRC as a byte array. This method has the same effect as 
    `byteutils.int_to_bytes(crc.crc32(payload))`. The condition `payload is None` is caught by
    this method and `None` is returned. `None` is also returned if the payload has no length.
    
    Args:
     payload (bytes): the payload to generate a crc for.

    Returns:
     bytes: a little endian byte array whose length is exactly 4 and, when converted to an integer,
            is the crc for `payload`.
    """

    if (payload is None or len(payload) == 0):
        return None

    # Get crc from bytesg
    crc32_int: int = crc32(payload)

    # Get crc as a 4 byte array
    return byteutils.int_to_bytes(crc32_int)


def attach(payload: bytes) -> bytes:
    """
    Adds a 4-byte checksum to the end of the argument `payload`. If a problem occurs with 
    generating the checksum, `None` is returned and no error is logged or thrown. `payload`
    must contain at least 1 byte, or no checksum will be generated.
    
    Args:
     payload (bytes): the byte array to generate a CRC for.
    
    Returns:
     bytes: a new byte array containing `payload + crc` in that order.
    """

    crc: bytes = _generate(payload)
    if (crc is None):
        return None

    body: bytes = payload + crc
    return body


def _extract_payload(body: bytes) -> bytes:
    """
    Extracts the payload from a byte array. The argument is assumed to contain a checksum as the
    last 4 bytes. This assumption is validated by `crc::check()`. If the argument `body` does not
    contain at least 5 bytes, then `None` is returned.
    
    Args:
     body (bytes): a byte array with a trailing 4-byte crc.
    
    Returns:
     bytes: the payload contained within {@code body} as defined by `body[0 : len(body) - 4]`.
    """

    if (body is None or len(body) <= NUM_BYTES):
        return None

    payload: bytes = body[:(len(body) - NUM_BYTES)]
    return payload


def _extract_crc(body: bytes) -> bytes:
    """
    Extracts the CRC from a byte array. The argument is assumed to contain a checksum as the 
    last 4 bytes. This assumption is validated by `crc::check()`. If the argument `body` does not
    contain at least 5 bytes, then null is returned.
    
    Args:
     body (bytes): a byte array with a trailing 4-byte crc.
    
    Returns:
     bytes: the CRC of `bytes` as defined by `body[len(body) - 4, len(body)]`.
    """

    if (body is None or len(body) <= NUM_BYTES):
        return None

    crc: bytes = body[(len(body) - NUM_BYTES):]
    return crc


def check(body: bytes) -> bool:
    """
    Checks for a valid CRC checksum on a byte array. This method assumes the last 4 bytes of the
    argument represent the CRC valid for the first `n - 4` bytes of the array.
    
    `body` is guaranteed to remain unmodified; only `True` or `False` is returned if the checksum
    is valid, no bytes are ever changed. Note that `False` may be returned upon error.
    
    Args:
     body (bytes): the byte array to check the CRC for.

    Returns:
     bool: `True` if the given and generated CRC of `body` match, otherwise `False`.
    """

    if (body is None or len(body) <= NUM_BYTES):
        return None

    payload: bytes = _extract_payload(body)
    crc: bytes = _extract_crc(body)
    gen: bytes = _generate(payload)

    if (payload is None or crc is None or gen is None):
        return False

    passed: bool = crc == gen
    return passed


def check_and_remove(body: bytes) -> bytes:
    """
    Checks a byte array for a valid CRC and returns the payload. If the checksum cannot be
    validated, `None` is returned.
    
    Args:
     body (bytes): the byte array to check the crc for.
    
    Returns:
     bytes: the payload of `body`.
    """

    valid: bool = check(body)
    if (not valid):
        return None
    return _extract_payload(body)
