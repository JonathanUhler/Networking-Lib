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

    def __init__(self, header: bytes):
        if (header is None):
            raise TypeError("header cannot be None")
        if (len(header) != SIZE):
            raise MissingDataError(f"invalid header length, expected {SIZE}, found {len(header)}")
        if (not crc.check(header)):
            raise MalformedDataError("invalid header crc")

        size_bytes: bytes = header[BODY_LENGTH_OFFSET:(BODY_LENGTH_OFFSET + BODY_LENGTH_SIZE)]
        crc_bytes: bytes = header[CRC_OFFSET:(CRC_OFFSET + CRC_SIZE)]

        self.id = header[0]
        self.size = byteutils.bytes_to_int(size_bytes)
        self.crc = byteutils.bytes_to_int(crc_bytes)

        if (self.size < 0):
            raise MalformedDataError(f"invalid payload size: {self.size}")



def _generate(body: bytes) -> bytes:
    has_valid_crc: bool = crc.check(body)
    if (not has_valid_crc):
        raise MalformedDataError("body has invalid crc")

    length: int = len(body)
    length_bytes: bytes = byteutils.int_to_bytes(length)

    header: bytes = HEADER_BYTE + b"\x00\x00\x00" + length_bytes
    header = crc.attach(header)
    return header


def attach(body: bytes) -> bytes:
    if (body is None):
        raise TypeError("body cannot be None")

    header: bytes = _generate(body)
    message: bytes = header + body
    return message


def validate_and_parse(header: bytes) -> Info:
    return Info(header)
