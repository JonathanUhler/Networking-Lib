"""
Utilities to handle byte-string and byte-object operations.

Author: Jonathan Uhler
"""


from typing import Final
from pnet.error import MissingDataError


ORDER: Final = "little"
ENCODING: Final = "utf-8"
BYTES_PER_INT: Final = 4


def int_to_bytes(n: int) -> bytes:
    """
    Converts an integer to a byte array.
    
    This operation will always produce a non-null byte array with exactly four elements,
    assuming the input integer is a valid int32. The returned byte array is always little endian.
    
    Arguments:
     n (int): the integer to convert.

    Returns:
     int: a byte array representation of `n`.
    """

    return n.to_bytes(BYTES_PER_INT, ORDER, signed = True)


def bytes_to_int(b: bytes) -> int:
    """
    Converts a byte array to an integer.

    Arguments:
     b (bytes): the byte array to convert.

    Returns:
     int: the integer representation of {@code b}.

    Raises:
     TypeError:        if `b` is None.
     MissingDataError: if `b` does not contain exactly four bytes.
    """

    if (b is None):
        raise TypeError("b cannot be None")
    if (len(b) != BYTES_PER_INT):
        raise MissingDataError(f"cannot convert byte[{len(b)}] to int")

    return int.from_bytes(b, ORDER, signed = True)


def str_to_bytes(string: str) -> bytes:
    """
    Converts a string to a byte array.

    Arguments:
     string (str): the string to convert.

    Returns:
     bytes: a byte array representation of `string`.

    Raises:
     TypeError: if `string` is None.
    """

    if (string is None):
        raise TypeError("string cannot be None")
    return bytes(string, ENCODING)


def bytes_to_str(b: bytes) -> str:
    """
    Converts a byte array to a string.

    Arguments:
     b (bytes): the byte array to convert.

    Returns:
     str: a string representation of `b`.

    Raises:
     TypeError: if `b` is None.
    """

    if (b is None):
        raise TypeError("b cannot be None")
    return b.decode(ENCODING)
