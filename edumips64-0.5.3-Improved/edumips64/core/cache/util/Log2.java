package edumips64.core.cache.util;

/**
 * Utility class used to calculate integer log base two
 */
public class Log2 {

    /**
     * Returns the log base two of a number. If the number is not a power of two it returns -1
     * @param x  Number to take the log of
     * @return  Log_2(x)
     */
    public static int compute(int x) {

        int result = 0;

        if (x == 0)
            return result;

        while (true) {
            int bit = x & 1;

            result++;

            if (bit == 1) {
                if (x != 0)
                    return -1;

                return  result;
            }

            if (result > 32)
                return -1;
        }

    }

    public static void main(String[] args)
    {
        System.out.println(2);
        System.out.println(4);
        System.out.println(31);
        System.out.println(32);
        System.out.println(2048);

    }
}
