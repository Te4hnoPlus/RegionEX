package plus.region.utl;


/**
 * Use this to fast throw an exception
 */
public final class FastExitException extends Exception{
    private final StackTraceElement[] EMPTY = new StackTraceElement[0];
    public static FastExitException INSTANCE = new FastExitException();

    private FastExitException(){
        super("Fast exit");
    }

    /**
     * Ignore this to speedy
     */
    @Override
    public Throwable fillInStackTrace() {
        return this;
    }


    @Override
    public StackTraceElement[] getStackTrace() {
        return EMPTY;
    }
}