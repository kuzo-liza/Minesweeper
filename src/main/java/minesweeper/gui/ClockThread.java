package minesweeper.gui;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class ClockThread extends Thread {

	// A second expressed in milliseconds
	private static final long SECOND_TIME_MILLIS = 1000;

	// Single instance of the thread
	private static ClockThread instance;

	// Seconds counter
	private long seconds = 0;

	// Reference to the graphic element with the seconds that have passed since the start of the game
	private JTextField clock = null;

	private ClockThread(JTextField look) {
		clock = look;
		seconds = 0;
	}

	// Resets the counter
	public static void reset(JTextField look) {
		if (instance != null) {
			// Tries to interrupt the current thread. If any problem raises,
			// we get an InterruptedException; if not, we just change our state
			instance.interrupt();
		}

		// Leave the former instance for the garbage collector
		instance = null;

		// Create a new instance
		instance = new ClockThread(look);
	}

	// This one will make the thread start the counter
	public static void startGame() {
		instance.start();
	}

	// Will just update the clock every XXX milliseconds
	public void run() {
		try {

			// The thread will continue, unless interrupted from the outside
			while (!interrupted()) {

				// Paint the time in the GUI (using invokeLater)
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						clock.setText(Long.toString(seconds++));
					}
				});

				// Wait for a second
				sleep(SECOND_TIME_MILLIS);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}