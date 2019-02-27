package edumips64.core.cache.cacheExceptions;

public class InvalidPowerOfTwoException extends Exception {
    public InvalidPowerOfTwoException() {
        super("The Cache size and the block size both must be a power of two");
    }
}
