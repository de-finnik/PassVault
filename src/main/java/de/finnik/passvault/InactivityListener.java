package de.finnik.passvault;

import java.awt.*;
import java.awt.event.AWTEventListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Checks whether the user is inactive for a given period of time
 */
public class InactivityListener {
    /**
     * What to do when user is inactive
     */
    private final Inactivity toDo;
    /**
     * Maximal time that the user is allowed to be inactive
     */
    private int inactivity;
    /**
     * Last time where the user was active
     */
    private long time;

    private AWTEventListener listener;

    private ScheduledExecutorService executor;

    /**
     * Creates an instance with the time that the user is allowed to be inactive
     * and a {@link Inactivity} instance whose {@link Inactivity#inactive()} will be executed when the user is inactive
     *
     * @param inactivity The period of time the user is allowed to be inactive
     * @param toDo       Its {@link Inactivity#inactive()} method is exexcuted when the user is inactive
     */
    public InactivityListener(int inactivity, Inactivity toDo) {
        this.inactivity = inactivity;
        this.toDo = toDo;
    }

    /**
     * Starts the listener
     * <p>
     * Will wait {@link InactivityListener#inactivity} seconds after the users last input and then execute the {@link Inactivity#inactive()} method
     * of {@link InactivityListener#toDo}
     */
    public void start() {
        if (executor != null && !executor.isShutdown())
            stop();

        time = System.currentTimeMillis();
        listener = (event -> time = System.currentTimeMillis());
        Toolkit.getDefaultToolkit().addAWTEventListener(listener, AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_WHEEL_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.ACTION_EVENT_MASK);

        executor = Executors.newSingleThreadScheduledExecutor();
        // Checks every second whether the user was inactive for too long
        executor.scheduleAtFixedRate(() -> {
            if (System.currentTimeMillis() - time > (inactivity * 1000)) {
                stop();
                toDo.inactive();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    /**
     * Stops the listener
     */
    public void stop() {
        executor.shutdown();
        Toolkit.getDefaultToolkit().removeAWTEventListener(listener);
    }

    /**
     * Changes the time that the user is allowed to be inactive
     *
     * @param inactivity New value
     */
    public void setInactivity(int inactivity) {
        this.inactivity = inactivity;
        if (executor != null && !executor.isShutdown())
            start();
    }

    /**
     * Is used inside {@link InactivityListener} to execute {@link Inactivity#inactive()} when the user was inactive for too long
     */
    public interface Inactivity {
        /**
         * User was inactive for too long
         */
        void inactive();
    }
}
