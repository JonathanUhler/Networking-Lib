"""
Utilities to handle CRC32 generation, application, and verification for network safety.

For more information on the `crc.py` utilities, see the equivalent implements and doc-comments
in the Java Networking-Lib implementation (jnet.CRC).

Author: Jonathan Uhler
"""


import binascii
import struct
from typing import Final
from pnet import byteutils
from pnet.error import MissingDataError, MalformedDataError


NUM_NIBBLES: Final = 8
NUM_BYTES: Final = NUM_NIBBLES // 2


def crc32(b: bytes) -> int:
    """
    Generates a 4 byte CRC as an integer.

    Arguments:
     b (bytes): the bytes to generate a checksum for.

    Returns:
     int: a 4 byte checksum as an int.

    Raises:
     TypeError: if `b` is None.
    """

    if (b is None):
        raise TypeError("b cannot be None")
    return binascii.crc32(b)


def _generate(payload: bytes) -> bytes:
    """
    Generates a 4-byte CRC as a byte array.

    This is a private function and should not be called outside of `crc.py`.

    Arguments:
     payload (bytes): the payload to generate a crc for.

    Returns:
     bytes: a little endian byte array whose length is exactly 4 and, when converted to an
            integer, is the crc for `payload`.
    """

    crc32_int: int = crc32(payload)
    return struct.pack("<I", crc32_int)


def attach(payload: bytes) -> bytes:
    """
    Adds a 4-byte checksum to the end of the argument `payload` and returns the combined byte array.

    Arguments:
     payload (bytes): the byte array to generate a CRC for.

    Returns:
     bytes: a new byte array containing `payload + crc` in that order.

    Raises:
     TypeError: if `payload` is None.
    """

    if (payload is None):
        raise TypeError("payload cannot be None")

    crc: bytes = _generate(payload)
    body: bytes = payload + crc
    return body


def _extract_payload(body: bytes) -> bytes:
    """
    Extracts the payload from a byte array.

    This is a private function and should not be called outside of `crc.py`.
    
    The argument is assumed to contain a checksum as the last 4 bytes. This assumption is
    validated by `crc.check()`. The argument is expected to be non-null, which must be validated
    by the caller.

    Arguments:
     body (bytes): a byte array with a trailing 4-byte crc.

    Returns:
     bytes: the payload contained within `body` as defined by `body[0, body.length - 4]`.
    """

    payload: bytes = body[:(len(body) - NUM_BYTES)]
    return payload


def _extract_crc(body: bytes) -> bytes:
    """
    Extracts the CRC from a byte array.

    This is a private function and should not be called outside of `crc.py`.
    
    The argument is assumed to contain a checksum as the last 4 bytes. This assumption is
    validated by `crc.check()`. The argument is expected to be non-null, which must be validated
    by the caller.

    Arguments:
     body (bytes): a byte array with a trailing 4-byte crc.

    Returns:
     bytes: the payload contained within `body` as defined by `body[body.length - 4, body.length]`.
    """

    crc: bytes = body[(len(body) - NUM_BYTES):]
    return crc


def check(body: bytes) -> bool:
    """
    Checks for a valid CRC checksum on a byte array. This method assumes the last 4 bytes of the
    argument represent the CRC valid for the first `n - 4` bytes of the array.

    `body` is guaranteed to remain unmodified; only `True` or `False` is returned if the checksum
    is valid, no bytes are ever changed. An exception will be thrown upon encountering any error
    state or precondition violation.

    Arguments:
     body (bytes): the byte array to check the CRC for.

    Returns:
     bool: `True` if the given and generated CRC of `body` match, otherwise `False`.

    Raises:
     TypeError:        if `body` is None.
     MissingDataError: if `body` does not contain at least 4 bytes of CRC.
    """

    if (body is None):
        raise TypeError("body cannot be noen")
    if (len(body) < NUM_BYTES):
        raise MissingDataError(f"byte[{len(body)}] is too short")

    payload: bytes = _extract_payload(body)
    crc: bytes = _extract_crc(body)
    gen: bytes = _generate(payload)

    return crc == gen


def check_and_remove(body: bytes) -> bytes:
    """
    Checks a byte array for a valid CRC and returns the payload.

    Arguments:
     body (bytes): the byte array to check the crc for.

    Returns:
     bytes: the payload of `body`.

    Raises:
     MalformedDataError: if the CRC is invalid.
    """

    valid: bool = check(body)
    if (not valid):
        raise MalformedDataError("crc is not valid")
    return _extract_payload(body)
