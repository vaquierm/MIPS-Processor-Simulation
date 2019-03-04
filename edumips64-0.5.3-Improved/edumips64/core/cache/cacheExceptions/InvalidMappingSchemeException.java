package edumips64.core.cache.cacheExceptions;

public class InvalidMappingSchemeException extends Exception {
    public InvalidMappingSchemeException() {
        super("The mapping scheme must be of form : 'DIRECT', 'FULLY_ASSOCIATIVE_<Replacementploicy>', or 'N_WAY_SET_ASSOCIATIVE_<Replacementploicy>' where N is the number of blocks per set and <Replacementploicy> is one of the values {'RANDOM', 'FIFO', 'LRU'}");
    }
}
