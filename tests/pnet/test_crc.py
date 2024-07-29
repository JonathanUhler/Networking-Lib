import pytest
import conftest
from pnet import byteutils
from pnet import crc
from pnet.error import MissingDataError, MalformedDataError


class TestCRC:

    def test_crc_consistency(self):
        loop_amount: int = int(conftest.RANDOM_TEST_AMOUNT ** 0.5)
        for i in range(loop_amount):
            data: str = conftest.random_str(100)
            b: bytes = byteutils.str_to_bytes(data)
            expected: int = crc.crc32(b)
            for j in range(loop_amount):
                generated: int = crc.crc32(b)
                assert generated == expected

    def test_crc_with_null_argument(self):
        with pytest.raises(TypeError):
            crc.crc32(None)

    def test_crc_uniqueness(self):
        for i in range(conftest.RANDOM_TEST_AMOUNT):
            data1: str = conftest.random_str(100)
            data2: str = conftest.random_str(100)
            b1: bytes = byteutils.str_to_bytes(data1)
            b2: bytes = byteutils.str_to_bytes(data2)
            crc1: int = crc.crc32(b1)
            crc2: int = crc.crc32(b2)

            if (data1 == data2):
                continue
            assert crc1 != crc2

    def test_attach_check(self):
        for i in range(conftest.RANDOM_TEST_AMOUNT):
            data: str = conftest.random_str(100)
            payload: bytes = byteutils.str_to_bytes(data)
            body: bytes = crc.attach(payload)
            passed: bool = crc.check(body)

            assert passed

    def test_attach_to_null_argument(self):
        with pytest.raises(TypeError):
            crc.attach(None)

    def test_attach_to_empty_argument(self):
        assert len(crc.attach(bytes())) == crc.NUM_BYTES

    def test_check_with_null_argument(self):
        with pytest.raises(TypeError):
            crc.check(None)

    def test_check_with_empty_argument(self):
        with pytest.raises(MissingDataError):
            crc.check(bytes())

    def test_check_with_short_argument(self):
        with pytest.raises(MissingDataError):
            crc.check(bytes([0, 1, 2]))

    def test_check_with_empty_payload(self):
        expected: bytes = bytes()
        body: bytes = crc.attach(expected)
        generated: bytes = crc.check_and_remove(body)
        assert generated == expected

    def test_attach_check_and_remove(self):
        for i in range(conftest.RANDOM_TEST_AMOUNT):
            data: str = conftest.random_str(100)
            payload: bytes = byteutils.str_to_bytes(data)
            body: bytes = crc.attach(payload)
            generated: bool = crc.check_and_remove(body)

            assert generated is not None
            assert generated == payload
