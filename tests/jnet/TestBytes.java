import java.util.Date;
import java.util.ArrayList;
import org.junit.Test;
import org.junit.Assert;
import org.junit.Assume;
import jnet.Bytes;
import jnet.MissingDataException;


public class TestBytes {

    @Test
    public void testIntToBytesAndBytesToInt() {
        for (int i = 0; i < TestMain.RANDOM_TEST_AMOUNT; i++) {
            int expected = TestMain.RANDOM_DIS.nextInt();
            byte[] bytes = Bytes.intToBytes(expected);
            int generated = Bytes.bytesToInt(bytes);

            Assert.assertEquals(generated, expected);
        }
    }

    @Test
    public void testBytesToIntWithNullArgument() {
        Assert.assertThrows(NullPointerException.class, () -> Bytes.bytesToInt(null));
    }

    @Test
    public void testBytesToIntWithLongArgument() {
        Assert.assertThrows(MissingDataException.class,
                            () -> Bytes.bytesToInt(new byte[] {0,1,2,3,4}));
    }

    @Test
    public void testBytesToIntWithShortArgument() {
        Assert.assertThrows(MissingDataException.class,
                            () -> Bytes.bytesToInt(new byte[] {0,1,2}));
    }

    @Test
    public void testSerDesString() {
        for (int i = 0; i < TestMain.RANDOM_TEST_AMOUNT; i++) {
            String expected = TestMain.randomString(100);
            byte[] bytes = Bytes.serialize(expected);
            String generated = (String) Bytes.deserialize(bytes);

            Assert.assertEquals(generated, expected);
        }
    }

    @Test
    public void testSerializeWithNullArgument() {
        Assert.assertThrows(NullPointerException.class, () -> Bytes.serialize(null));
    }

    @Test
    public void testDeserializeWithNullArgument() {
        Assert.assertThrows(NullPointerException.class, () -> Bytes.deserialize(null));
    }

    @Test
    public void testSerDesJavaUtils() {
        Date dateExpected = new Date();
        byte[] dateBytes = Bytes.serialize(dateExpected);
        Date dateGenerated = (Date) Bytes.deserialize(dateBytes);
        Assert.assertEquals(dateGenerated, dateExpected);

        ArrayList<String> listExpected = new ArrayList<>();
        for (int i = 0; i < TestMain.RANDOM_TEST_AMOUNT; i++) {
            listExpected.add(TestMain.randomString(10));
        }
        byte[] listBytes = Bytes.serialize(listExpected);
        ArrayList<String> listGenerated = (ArrayList<String>) Bytes.deserialize(listBytes);
        Assert.assertEquals(listGenerated, listExpected);
    }

    @Test
    public void testStringToBytesAndBytesToString() {
        for (int i = 0; i < TestMain.RANDOM_TEST_AMOUNT; i++) {
            String expected = TestMain.randomString(100);
            byte[] bytes = Bytes.stringToBytes(expected);
            String generated = Bytes.bytesToString(bytes);

            Assert.assertEquals(generated, expected);
        }
    }

    @Test
    public void testStringToBytesWithEmptyArgument() {
        Assert.assertArrayEquals(Bytes.stringToBytes(""), new byte[] {});
    }

    @Test
    public void testBytesToStringWithEmptyArgument() {
        Assert.assertEquals(Bytes.bytesToString(new byte[] {}), "");
    }

    @Test
    public void testStringToBytesWithNullArgument() {
        Assert.assertThrows(NullPointerException.class, () -> Bytes.stringToBytes(null));
    }

    @Test
    public void testBytesToStringWithNullArgument() {
        Assert.assertThrows(NullPointerException.class, () -> Bytes.bytesToString(null));
    }

}
