package edumips64.core.cache.cacheExceptions;

public class InvalidAssociativityException extends Exception {
    public InvalidAssociativityException() { super("Cache set associativity must be greater than or equal to 0"); }
}
