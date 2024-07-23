import java.util.Random;


public class TestMain {

    static final int RANDOM_TEST_AMOUNT = 1000000;
    static final Random RANDOM_DIS = new Random();


    public static String randomString(int length) {
        StringBuilder string = new StringBuilder();
        for (int i = 0; i < length; i++) {
            string.append((char) TestMain.RANDOM_DIS.nextInt(255));
        }
        return string.toString();
    }

}
