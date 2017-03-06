package minesweeper.logic;

import java.util.Random;

import minesweeper.Constants;

public class Map {

	// Slots matrix
	private Slot[][] matrix;


	// Builds a populated Map
	public Map() {

		// Create the (empty) matrix
		matrix = new Slot[Constants.ROWS][Constants.COLUMNS];
		for (int theRow = 0; theRow < Constants.ROWS; theRow++) {

			for (int theColumn = 0; theColumn < Constants.COLUMNS; theColumn++)
			{
				// A new Slot for each position
				matrix[theRow][theColumn] = new Slot();
			}
		}
		// Populate the matrix
		populateMap();
	}
	
	// Getters
	public boolean isThereAMine(int theRow, int theColumn) { return matrix[theRow][theColumn].hasMine(); }
	public boolean isHidden(int theRow, int theColumn) {
		return matrix[theRow][theColumn].isHidden();
	}
	public boolean isSuspicious(int theRow, int theColumn) {
		return matrix[theRow][theColumn].isSuspicious();
	}
	public boolean hasQuestionMark(int theRow, int theColumn) {
		return matrix[theRow][theColumn].hasQuestionMark();
	}

	// Setters
	public void setHasMine(int theRow, int theColumn) {
		matrix[theRow][theColumn].setHasMine(true);
	}

	public void setHasBeenVisitedThisTurn(int theRow, int theColumn, boolean value) {
		matrix[theRow][theColumn].setHasBeenVisitedThisTurn(value);
	}

	private void clear(int theRow, int theColumn) {
		matrix[theRow][theColumn].setIsHidden(false);
		matrix[theRow][theColumn].setIsSuspicious(false);
		matrix[theRow][theColumn].setHasQuestionMark(false);
	}

	public void setSuspicious(int theRow, int theColumn) {
		matrix[theRow][theColumn].setIsSuspicious(true);
	}
	public void setFreeOfSuspicion(int theRow, int theColumn) {
		matrix[theRow][theColumn].setIsSuspicious(false);
	}
	public void addQuestionMark(int theRow, int theColumn) {
		matrix[theRow][theColumn].setHasQuestionMark(true);
	}
	public void clearQuestionMark(int theRow, int theColumn) {
		matrix[theRow][theColumn].setHasQuestionMark(false);
	}

	// Clear the 'hasBeenVisited' flag in all the game grid
	private void renewGameGrid() {

		for (int theRow = 0; theRow < Constants.ROWS; theRow++) {
			for (int theColumn = 0; theColumn < Constants.COLUMNS; theColumn++) {

				// New turn, nothing has been visited
				Slot slot = matrix[theRow][theColumn];
				slot.setHasBeenVisitedThisTurn(false);
				slot.setHasMine(false);
				slot.setIsHidden(true);
				slot.setIsSuspicious(false);
				slot.setHasQuestionMark(false);
			}
		}
	}

	// Has the current game ended?
	public boolean hasWon() {

		// Is every bombed slot marked as suspicious? Is there any hidden slot left?
		boolean ret = true;
		int theRow = 0;

		while ((theRow < Constants.ROWS) && ret) {
			int theColumn = 0;

			while (theColumn < Constants.COLUMNS && ret) {

				Slot slot = matrix[theRow][theColumn];
				// If there is any unmarked hidden slot, or a wrongly marked bombed slot, we are not done yet
				if ((slot.isHidden() && !slot.isSuspicious()) || (slot.isHidden() && slot.isSuspicious() && !slot.hasMine())) {
					ret = false;
				}
				theColumn++;
			}
			theRow++;
		}
		return ret;
	}

	// Check if a player's click has stept into a bomb
	public void click(int theRow, int theColumn) throws BombException {
		if (isSuspicious(theRow, theColumn)) {
			// Nothing done.  The 'suspicious' mark protects the slot
			return;
		}

		// Bomb found? Then throw exception to be managed
		if (matrix[theRow][theColumn].hasMine()) {
			throw new BombException(theRow, theColumn);
		}
		else {
			// Clear recursively the game board around the slot
			clickI(theRow, theColumn);
		}
	}	

	// Recursive procedure to clear near slots
	private void clickI(int theRow, int theColumn) {
		Slot slot = matrix[theRow][theColumn];

		// If it has a bomb, do nothing
		// If it has been 'visited' this turn, we do nothing
		// If it was detected, we do nothing
		if (!slot.hasMine() && !slot.hasBeenVisitedThisTurn() && slot.isHidden()) {

			// We visit it
			slot.setHasBeenVisitedThisTurn(true);

			// Call again for the adjacent slots (clockwise)
			if (getMinesAround(theRow, theColumn) == 0) {

				// Upper row
				if (theRow > 0) {
					if (theColumn > 0) {
						// Up left
						clickI(theRow - 1, theColumn - 1);
					}

					// Up
					clickI(theRow - 1, theColumn);
					if (theColumn < Constants.COLUMNS - 1) {
						// Up right
						clickI(theRow - 1, theColumn + 1);
					}
				}

				// To the right
				if (theColumn < Constants.COLUMNS - 1) { clickI(theRow, theColumn + 1); }

				// Lower row
				if (theRow < Constants.ROWS - 1) {
					if (theColumn < Constants.COLUMNS - 1) {
						// Down-right
						clickI(theRow + 1, theColumn + 1);
					}

					// Down
					clickI(theRow + 1, theColumn);
					if (theColumn > 0) {
						// DownLeft
						clickI(theRow + 1, theColumn - 1);
					}
				}

				// To the left
				if (theColumn > 0) { clickI(theRow, theColumn - 1); }
			}

			// Reveal the slot
			clear(theRow, theColumn);
		}
	}

	// It clears the game board and populates it with a number of bombs in random locations
	public void populateMap() {
		renewGameGrid();

		// Randomly place mines
		int minesPlaced = 0;
		Random g = new Random();

		while (minesPlaced < Constants.NUMBER_OF_MINES) {

			// Generate coordinates for the mine
			int row = g.nextInt(Constants.ROWS);
			int column = g.nextInt(Constants.COLUMNS);

			// Place it if it was clear
			// Maybe the coordinates are not valid, we need a new turn of the loop
			if (!isThereAMine(row, column)) {
				minesPlaced++;
				setHasMine(row, column);
			}
		}
	}

	// Counts the number of remaining mines for the player. Beware that it must make its count with
	// the number of flags that the player has placed.
	public int remainingMines() {
		return Constants.NUMBER_OF_MINES - countGuessedMines();
	}

	// It counts the number of slots marked as 'suspicious' by the player
	private int countGuessedMines() {
		int counter = 0;

		for (int theRow = 0; theRow < Constants.ROWS; theRow++) {
			for (int theColumn = 0; theColumn < Constants.COLUMNS; theColumn++) {

				// If it is hidden, add 1 to the counter
				if (matrix[theRow][theColumn].isSuspicious()) { counter++; }
			}
		}
		return counter;
	}

	// Calculates the number of mines around a given slot
	public int getMinesAround(int theRow, int theColumn) {
		int ret = 0;

		// Upper row
		if (theRow > 0) {
			if (theColumn > 0) {
				// Up left
				if (isThereAMine(theRow - 1, theColumn - 1)) { ret++; }
			}

			// Up
			if (isThereAMine(theRow - 1, theColumn)) { ret++;}
			if (theColumn < Constants.COLUMNS - 1) {
				// Up right
				if (isThereAMine(theRow - 1, theColumn + 1)) { ret++; }
			}
		}

		// To the right
		if (theColumn < Constants.COLUMNS - 1) {
			if (isThereAMine(theRow, theColumn + 1)) { ret++; }
		}

		// Lower row
		if (theRow < Constants.ROWS - 1) {
			if (theColumn < Constants.COLUMNS - 1) {
				// Down-right
				if (isThereAMine(theRow + 1, theColumn + 1)) { ret++; }
			}

			// Down
			if (isThereAMine(theRow + 1, theColumn)) { ret++; }
			if (theColumn > 0) {
				// Down-left
				if (isThereAMine(theRow + 1, theColumn - 1)) { ret++; }
			}
		}

		// To the left
		if (theColumn > 0) {
			if (isThereAMine(theRow, theColumn - 1)) { ret++; }
		}
		return ret;
	}

	private class Slot {

		// Visited this turn?
		private boolean visitedThisTurn = false;

		// Does it have a mine under it?
		private boolean hasMine = false;

		// Still hidden?
		private boolean hidden = true;

		// Has the player made it suspicious?
		private boolean suspicious = false;

		// Marked with a question mark?
		private boolean questionMark = false;

		// Constructor
		public Slot() {
		}

		// Getters
		public boolean hasBeenVisitedThisTurn() {
			return visitedThisTurn;
		}
		public boolean hasMine() {
			return hasMine;
		}
		public boolean isHidden() {
			return hidden;
		}
		public boolean isSuspicious() {
			return suspicious;
		}
		public boolean hasQuestionMark() {
			return questionMark;
		}

		// Setters
		public void setHasBeenVisitedThisTurn(boolean hasBeenVisitedThisTurn) {
			visitedThisTurn = hasBeenVisitedThisTurn;
		}
		public void setHasMine(boolean hasMine) {
			this.hasMine = hasMine;
		}
		public void setIsHidden(boolean isHidden) {
			hidden = isHidden;
		}
		public void setIsSuspicious(boolean isSuspicious) {
			suspicious = isSuspicious;
		}
		public void setHasQuestionMark(boolean hasQuestionMark) {
			questionMark = hasQuestionMark;
		}

	}

}