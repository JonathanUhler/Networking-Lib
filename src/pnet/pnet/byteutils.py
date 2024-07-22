"""
Utilities to handle byte-string operations.

Author: Jonathan Uhler
"""


from typing import Final


ORDER: Final = "little"
ENCODING: Final = "utf-8"
BYTES_PER_INT: Final = 4


def int_to_bytes(n: int) -> bytes:
    """
    Converts an integer to a byte array. This operator will always produce a byte array `b`
    such that `b is not None and len(b) == 4`.

    Args:
     n (Int): the integer to convert.

    Returns:
     bytes: a little endian byte array representation of `n`.
    """

    return n.to_bytes(BYTES_PER_INT, ORDER)


def bytes_to_int(b: bytes) -> int:
    """
    Converts a byte array to an integer. The passed array must not be null and must contain 
    exactly 4 bytes.

    Args:
     b (bytes): the little endian byte array to convert.

    Returns:
     int: the integer representation of `b`.

    Raises:
     TypeError:  if `b is None`.
     ValueError: if `len(b) != sizeof(int) / sizeof(byte) == 4`.
    """

    if (b is None):
        raise TypeError("b cannot be None")
    if (len(b) != BYTES_PER_INT):
        raise ValueError(f"invalid num bytes: expected {BYTES_PER_INT}, found {len(b)}")

    return int.from_bytes(b, ORDER)


def str_to_bytes(string: str) -> bytes:
    """
    Converts a string to a byte array. This method is a wrapper for `bytes(string)`. If 
    `string is None`, `None` will be returned without an error being logged or thrown.
    
    Args:
     string (str): the string to convert.

    Returns:
     bytes: a little endian byte-array representation of `string`.
    """

    if (string is None):
        return None
    return bytes(string, ENCODING)


def bytes_to_str(b: bytes) -> str:
    """
    Converts a byte array to a string. This method is a wrapper for `b.decode`. If `b is None`,
    `None` will be returned without an error being logged or thrown.
    
    Args:
     b (bytes): the byte array to convert.

    Returns:
     str: a string representation of `b`.
    """

    if (b is None):
        return None
    return b.decode(ENCODING)
