import java.util.Date;
import java.util.ArrayList;
import org.junit.Test;
import org.junit.Assert;
import org.junit.Assume;
import jnet.Bytes;


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
    public void testSerDesString() {
        for (int i = 0; i < TestMain.RANDOM_TEST_AMOUNT; i++) {
            String expected = TestMain.randomString(100);
            byte[] bytes = Bytes.serialize(expected);
            String generated = (String) Bytes.deserialize(bytes);

            Assert.assertEquals(generated, expected);
        }
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

}
