from typing import Final
from pnet.error import MissingDataError


ORDER: Final = "little"
ENCODING: Final = "utf-8"
BYTES_PER_INT: Final = 4


def int_to_bytes(n: int) -> bytes:
    return n.to_bytes(BYTES_PER_INT, ORDER, signed = True)


def bytes_to_int(b: bytes) -> int:
    if (b is None):
        raise TypeError("b cannot be None")
    if (len(b) != BYTES_PER_INT):
        raise MissingDataError(f"cannot convert byte[{len(b)}] to int")

    return int.from_bytes(b, ORDER, signed = True)


def str_to_bytes(string: str) -> bytes:
    if (string is None):
        raise TypeError("string cannot be None")
    return bytes(string, ENCODING)


def bytes_to_str(b: bytes) -> str:
    if (b is None):
        raise TypeError("b cannot be None")
    return b.decode(ENCODING)
