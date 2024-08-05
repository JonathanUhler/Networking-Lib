"""
Exceptions that are thrown by pnet.

Author: Jonathan Uhler
"""


class MissingDataError(Exception):
    """
    A simple child of `Exception` that identifies a piece of data that was shorter or longer
    than expected and cannot be processed because of the discrepancy in length.
    """


class MalformedDataError(Exception):
    """
    A simple child of `Exception` that indicates the presence of bad data.

    In most contexts, data is determined to be "bad" when the correct number of bytes are present,
    but their interpretation violates some common principle. This could include a failed CRC check
    or bytes that represent a negative payload size.
    """
