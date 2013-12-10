package org.javacint.loading;

/**
 * Named runnabled. Should be used to identify different startup tasks.
 */
public abstract class NamedRunnable {

    private final String name;

    public NamedRunnable(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * Run some code. This is not a Runnable method as it can throw an
     * exception.
     *
     * @throws Exception
     */
    public abstract void run() throws Exception;
}
