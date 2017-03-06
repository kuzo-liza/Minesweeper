package minesweeper.logic;

public class BombException extends Exception {

	private int row;
	private int column;

	/**
	 * Constructor
	 * 
	 * @param theRow
	 *            Row of the bomb location
	 * @param theColumn
	 *            Column of the bomb location
	 */
	public BombException(int theRow, int theColumn) {
		row = theRow;
		column = theColumn;
	}

	// Getters
	public int getRow() {
		return row;
	}
	public int getColumn() {
		return column;
	}
}