import java.util.Date;
import java.util.ArrayList;
import org.junit.Test;
import org.junit.Assert;
import org.junit.Assume;
import jnet.Bytes;
import jnet.CRC;
import jnet.Header;
import jnet.MalformedDataException;


public class TestHeader {

    @Test
    public void testAttachParse() {
        for (int i = 0; i < TestMain.RANDOM_TEST_AMOUNT; i++) {
            int dataLength = TestMain.RANDOM_DIS.nextInt(1000);
            String data = TestMain.randomString(dataLength);
            byte[] payload = Bytes.serialize(data);
            byte[] body = CRC.attach(payload);

            Assume.assumeTrue(CRC.check(body));

            byte[] message = Header.attach(body);
            byte[] header = new byte[Header.SIZE];
            System.arraycopy(message, 0, header, 0, header.length);
            Header.Info headerInfo = Header.validateAndParse(header);

            Assert.assertNotNull(headerInfo);
            Assert.assertEquals(headerInfo.id, Header.HEADER_BYTE);
            Assert.assertEquals(headerInfo.size, body.length);
        }
    }

    @Test
    public void testAttachToNullArgument() {
        Assert.assertThrows(NullPointerException.class, () -> Header.attach(null));
    }

    @Test
    public void testValidateWithNullArgument() {
        Assert.assertThrows(NullPointerException.class, () -> Header.attach(null));
    }

    @Test
    public void testMissingCrc() {
        String data = TestMain.randomString(100);
        byte[] payload = Bytes.serialize(data);

        Assert.assertThrows(MalformedDataException.class, () -> Header.attach(payload));
    }

    @Test
    public void testInvalidCrc() {
        String data = TestMain.randomString(100);
        String dataForCrc = TestMain.randomString(1);
        byte[] payload = Bytes.serialize(data);
        byte[] payloadForCrc = Bytes.serialize(dataForCrc);

        byte[] crc = Bytes.intToBytes(CRC.crc32(payloadForCrc));
        byte[] body = new byte[payload.length + crc.length];
        System.arraycopy(payload, 0, body, 0, payload.length);
        System.arraycopy(crc, 0, body, body.length - crc.length, crc.length);

        Assert.assertThrows(MalformedDataException.class, () -> Header.attach(payload));
    }

}
