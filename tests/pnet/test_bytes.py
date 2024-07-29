import pytest
import conftest
from pnet import byteutils
from pnet.error import MissingDataError


class TestBytes:

    def test_int_to_bytes_and_bytes_to_int(self):
        for i in range(conftest.RANDOM_TEST_AMOUNT):
            expected: int = conftest.random_int32()
            b: bytes = byteutils.int_to_bytes(expected)
            generated: int = byteutils.bytes_to_int(b)

            assert generated == expected

    def test_bytes_to_int_with_null_argument(self):
        with pytest.raises(TypeError):
            byteutils.bytes_to_int(None)

    def test_bytes_to_int_with_long_argument(self):
        with pytest.raises(MissingDataError):
            byteutils.bytes_to_int(bytes([0, 1, 2, 3, 4]))

    def test_bytes_to_int_with_short_argument(self):
        with pytest.raises(MissingDataError):
            byteutils.bytes_to_int(bytes([0, 1, 2]))

    def test_ser_des_string(self):
        for i in range(conftest.RANDOM_TEST_AMOUNT):
            expected: str = conftest.random_str(100)
            b: bytes = byteutils.str_to_bytes(expected)
            generated: str = byteutils.bytes_to_str(b)

            assert generated == expected

    def test_str_to_bytes_with_empty_argument(self):
        assert byteutils.str_to_bytes("") == bytes()


    def test_bytes_to_str_with_empty_argument(self):
        assert byteutils.bytes_to_str(bytes()) == ""

    def test_str_to_bytes_with_null_argument(self):
        with pytest.raises(TypeError):
            byteutils.str_to_bytes(None)

    def test_bytes_to_str_with_null_argument(self):
        with pytest.raises(TypeError):
            byteutils.bytes_to_str(None)
