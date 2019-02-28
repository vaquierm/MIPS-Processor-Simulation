package edumips64.core.cache.cacheExceptions;

public class BlockNotFoundException extends Exception {
    public BlockNotFoundException() {
        super("Block not found in cache");
    }
}
