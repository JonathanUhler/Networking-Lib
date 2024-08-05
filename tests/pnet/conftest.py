import random
import string
from typing import Final


RANDOM_TEST_AMOUNT: Final = 100000


def random_str(length: int) -> str:
    chars: list = []
    for i in range(length):
        chars.append(random.choice(string.ascii_letters))
    return "".join(chars)


def random_bytes(length: int) -> bytes:
    return random.randbytes(length)


def random_int32() -> int:
    return random.randint(-2 ** 31, 2 ** 31 - 1)
