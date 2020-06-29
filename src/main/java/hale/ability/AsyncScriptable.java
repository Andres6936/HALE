package hale.ability;

/**
 * Wrapper class for {@link Scriptable}.
 * <p>
 * This object enables Script execution via the Scriptable object
 * to be done asynchronously (in a new Thread).
 * <p>
 * Note that calling the start() method of this Object directly
 * will generate an IllegalStateException.  You need to instead
 * use the executeAsync functions.
 *
 * @author Jared Stephen
 */

public class AsyncScriptable extends Thread
{
    private Scriptable scriptable;

    private volatile boolean readyToExecute = false;
    private ScriptFunctionType type;
    private String function;
    private Object[] arguments;

    private long delayMillis;

    /**
     * Creates a new AsyncScriptable wrapping the specified Scriptable
     *
     * @param scriptable the Scriptable to wrap
     */

    public AsyncScriptable(Scriptable scriptable)
    {
        this.scriptable = scriptable;
    }

    /**
     * Sets the number of milliseconds this AsyncScriptable will delay
     * before executing its script
     *
     * @param delay the delay in milliseconds
     */

    public void setDelayMillis(long delay)
    {
        this.delayMillis = delay;
    }

    /**
     * Returns the Scriptable object that this AsyncScriptable is wrapping.
     *
     * @return the Scriptable object that this AsyncScriptable is wrapping.
     */

    public Scriptable getScriptable()
    {
        return scriptable;
    }

    public void executeAsync(String function, Object... arguments)
    {
        this.function = function;
        this.arguments = arguments;
        this.readyToExecute = true;

        this.start();
    }

    public void executeAsync(ScriptFunctionType type, Object... arguments)
    {
        this.type = type;
        this.arguments = arguments;
        this.readyToExecute = true;

        this.start();
    }

    @Override
    public void run()
    {
        if (!readyToExecute) {
            throw new IllegalStateException("Cannot use start() method of AsyncScriptable directly.  " +
                    "Use executeAsync instead.");
        }

        if (delayMillis != 0l) {
            try {
                Thread.sleep(delayMillis);
            } catch (InterruptedException e) {
                // thread was interrupted, can exit
                return;
            }
        }

        if (function != null) {
            scriptable.executeFunction(function, arguments);
        } else {
            scriptable.executeFunction(type, arguments);
        }
    }
}
