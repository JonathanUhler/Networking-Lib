import random
import pytest
import conftest
import struct
from pnet import byteutils
from pnet import crc
from pnet import header
from pnet.error import MalformedDataError


class TestHeader:

    def test_attach_parse(self):
        for i in range(conftest.RANDOM_TEST_AMOUNT):
            data_len: int = random.randint(0, 1000)
            data: str = conftest.random_str(data_len)
            payload: bytes = byteutils.str_to_bytes(data)
            body: bytes = crc.attach(payload)

            assert crc.check(body)

            message: bytes = header.attach(body)
            header_bytes: bytes = message[0:header.SIZE]
            header_info: header.Info = header.validate_and_parse(header_bytes)

            assert header_info is not None
            assert header_info.id == header.HEADER_BYTE
            assert header_info.size == len(body)

    def test_attach_to_null_argument(self):
        with pytest.raises(TypeError):
            header.attach(None)

    def test_validate_with_null_argument(self):
        with pytest.raises(TypeError):
            header.validate_and_parse(None)

    def test_missing_crc(self):
        data: str = conftest.random_str(100)
        payload: bytes = byteutils.str_to_bytes(data)

        with pytest.raises(MalformedDataError):
            header.attach(payload)

    def test_invalid_crc(self):
        data: str = conftest.random_str(100)
        data_for_crc: str = conftest.random_str(1)
        payload: bytes = byteutils.str_to_bytes(data)
        payload_for_crc: bytes = byteutils.str_to_bytes(data_for_crc)

        crc_bytes: bytes = struct.pack("<I", crc.crc32(payload_for_crc))
        body: bytes = payload + crc_bytes

        with pytest.raises(MalformedDataError):
            header.attach(body)
