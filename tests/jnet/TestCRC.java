import java.util.Date;
import java.util.ArrayList;
import org.junit.Test;
import org.junit.Assert;
import org.junit.Assume;
import jnet.Bytes;
import jnet.CRC;
import jnet.MissingDataException;


public class TestCRC {

    @Test
    public void testCrcConsistency() {
        for (int i = 0; i < Math.sqrt(TestMain.RANDOM_TEST_AMOUNT); i++) {
            String data = TestMain.randomString(100);
            byte[] bytes = Bytes.serialize(data);
            int expected = CRC.crc32(bytes);
            for (int j = 0; j < Math.sqrt(TestMain.RANDOM_TEST_AMOUNT); j++) {
                int generated = CRC.crc32(bytes);

                Assert.assertEquals(generated, expected);
            }
        }
    }

    @Test
    public void testCrcWithNullArgument() {
        Assert.assertThrows(NullPointerException.class, () -> CRC.crc32(null));
    }

    @Test
    public void testCrcUniqueness() {
        for (int i = 0; i < Math.sqrt(TestMain.RANDOM_TEST_AMOUNT); i++) {
            String data1 = TestMain.randomString(100);
            String data2 = TestMain.randomString(100);
            byte[] bytes1 = Bytes.serialize(data1);
            byte[] bytes2 = Bytes.serialize(data2);
            int crc1 = CRC.crc32(bytes1);
            int crc2 = CRC.crc32(bytes2);

            Assume.assumeFalse(data1.equals(data2));
            Assert.assertNotEquals(crc1, crc2);
        }
    }

    @Test
    public void testAttachCheck() {
        for (int i = 0; i < TestMain.RANDOM_TEST_AMOUNT; i++) {
            String data = TestMain.randomString(100);
            byte[] payload = Bytes.serialize(data);
            byte[] body = CRC.attach(payload);
            boolean passed = CRC.check(body);

            Assert.assertTrue(passed);
        }
    }

    @Test
    public void testAttachToNullArgument() {
        Assert.assertThrows(NullPointerException.class, () -> CRC.attach(null));
    }

    @Test
    public void testAttachToEmptyArgument() {
        Assert.assertEquals(CRC.attach(new byte[] {}).length, CRC.NUM_BYTES);
    }

    @Test
    public void testCheckWithNullArgument() {
        Assert.assertThrows(NullPointerException.class, () -> CRC.check(null));
    }

    @Test
    public void testCheckWithShortArgument() {
        Assert.assertThrows(MissingDataException.class, () -> CRC.check(new byte[] {}));
        Assert.assertThrows(MissingDataException.class, () -> CRC.check(new byte[] {0,1,2}));
    }

    @Test
    public void testCheckWithEmptyPayload() {
        byte[] expected = new byte[0];
        byte[] body = CRC.attach(expected);
        byte[] generated = CRC.checkAndRemove(body);
        Assert.assertArrayEquals(generated, expected);
    }

    @Test
    public void testAttachCheckAndRemove() {
        for (int i = 0; i < TestMain.RANDOM_TEST_AMOUNT; i++) {
            String data = TestMain.randomString(100);
            byte[] payload = Bytes.serialize(data);
            byte[] body = CRC.attach(payload);
            byte[] generated = CRC.checkAndRemove(body);

            Assert.assertNotNull(generated);
            Assert.assertArrayEquals(generated, payload);
        }
    }

}
