package minesweeper;

import java.awt.Color;

public interface Constants {

	// Paths for the images (gif)
	String QUESTION_MARK_PATH = "questionMark.gif";
	String SMILEY_PATH = "smiley.gif";
	String WRONG_FLAG_PATH = "wrongFlag.gif";
	String FLAG_PATH = "flag.gif";
	String MINE_PATH = "mine.gif";
	String RED_MINE_PATH = "redMine.gif";

	// Font for the numbers shown
	String FONT = "Dialog";

	// Start again
	String RESET = "Start again";

	// Slot marked as suspicious by the user
	char X = 'x';

	// Font size
	int FONT_SIZE = 7;

	// Width and height for the box with the remaining mines information
	int BOX_WIDTH = 55;
	int BOX_HEIGHT = 20;

	// Reset the clock
	String ZERO = "0";

	// Slots: width and height
	int SLOT_WIDTH = 15;
	int SLOT_HEIGHT = 15;

	// Empty slot content
	String EMPTY_SLOT_TEXT = "V";

	// Messages
	String CONGRATULATIONS = "Congratulations!";
	String YOU_HAVE_FINISHED_IN = "You have finished it in ";
	String SECONDS = " seconds!";

	// Background colors
	Color HIDDEN_BACKGROUND = Color.LIGHT_GRAY;
	Color VISIBLE_BACKGROUND = Color.WHITE;

	// Clock width and height
	int CLOCK_WIDTH = 40;
	int CLOCK_HEIGHT = 20;

	// Map rows and columns
	int ROWS = 16;
	int COLUMNS = 30;

	int NUMBER_OF_MINES = 100;
}