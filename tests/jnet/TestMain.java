import java.util.Random;


public class TestMain {

    static final int RANDOM_TEST_AMOUNT = 10000;
    static final Random RANDOM_DIS = new Random();


    public static String randomString(int length) {
        StringBuilder string = new StringBuilder();
        for (int i = 0; i < length; i++) {
            string.append((char) TestMain.RANDOM_DIS.nextInt(255));
        }
        return string.toString();
    }


    public static byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = (byte) TestMain.RANDOM_DIS.nextInt(255);
        }
        return bytes;
    }

}
