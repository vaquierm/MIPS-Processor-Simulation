package edumips64.core.cache.cacheExceptions;

public class InvalidMappingSchemeException extends Exception {
    public InvalidMappingSchemeException() {
        super("The mapping scheme must be of form : 'DIRECT', 'FULLY_ASSOCIATIVE', or 'N_WAY_SET_ASSOCIATIVE' where N is the number of blocks per set");
    }
}
