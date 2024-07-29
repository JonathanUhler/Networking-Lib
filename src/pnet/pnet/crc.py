import binascii
from typing import Final
from pnet import byteutils


NUM_NIBBLES: Final = 8
NUM_BYTES: Final = NUM_NIBBLES // 2


def crc32(b: bytes) -> int:
    if (b is None):
        raise TypeError("b cannot be None")
    return binascii.crc32(b)


def _generate(payload: bytes) -> bytes:
    crc32_int: int = crc32(payload)
    return byteutils.int_to_bytes(crc32_int)


def attach(payload: bytes) -> bytes:
    if (payload is None):
        raise TypeError("payload cannot be None")

    crc: bytes = _generate(payload)
    body: bytes = payload + crc
    return body


def _extract_payload(body: bytes) -> bytes:
    payload: bytes = body[:(len(body) - NUM_BYTES)]
    return payload


def _extract_crc(body: bytes) -> bytes:
    crc: bytes = body[(len(body) - NUM_BYTES):]
    return crc


def check(body: bytes) -> bool:
    if (body is None):
        raise TypeError("body cannot be noen")
    if (len(body) < NUM_BYTES):
        raise MissingDataError(f"byte[{len(body)}] is too short")

    payload: bytes = _extract_payload(body)
    crc: bytes = _extract_crc(body)
    gen: bytes = _generate(payload)

    return crc == gen


def check_and_remove(body: bytes) -> bytes:
    valid: bool = check(body)
    if (not valid):
        raise MalformedDataError("crc is not valid")
    return _extract_payload(body)
