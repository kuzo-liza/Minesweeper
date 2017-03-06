package minesweeper.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.text.MessageFormat;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;

import minesweeper.Constants;
import minesweeper.logic.BombException;
import minesweeper.logic.Map;

import org.apache.sanselan.common.byteSources.ByteSourceInputStream;
import org.apache.sanselan.formats.gif.GifImageParser;

import com.jhlabs.image.MapColorsFilter;

public class GamePanel extends JPanel {

	// Borders definition
	private static final Border BEVEL_BORDER = BorderFactory.createRaisedBevelBorder();
	private static final Border GRAY_BORDER = BorderFactory.createLineBorder(Color.LIGHT_GRAY);

	// Mine colors
	private static final String[] COLORS = { "blue", "green", "red", "pink", "#000099", "#996600", "#FF6600", "#99CC3" };

	// HTML template for each mine
	private static final String MINE_TEMPLATE = "<html><div style=\"font-size:12px;color:{0}\">"+ "{1}</div></html>";

	protected GridBagLayout gridBagLayout1 = new GridBagLayout();

	// Slots matrix
	protected GraphicSlot[][] matrix;

	// Graphic elements
	protected JTextField box;
	protected JButton resetButton;

	// Can we play?
	private boolean play = true;

	// Marks the start of the game
	protected boolean firstMove = true;

	// Timestamp of the start of the game
	protected long timestampGameStart = 0;

	// Instances of the gif images
	protected ImageIcon flag;
	protected ImageIcon mine;
	protected ImageIcon redMine;
	protected ImageIcon questionMark;
	protected ImageIcon smiley;
	protected ImageIcon wrongFlag;

	// Coordinates of the last mine found
	private int rowLastMine = -1;
	private int columnLastMine = -1;

	// Any mine step on?
	protected boolean mineStepOn = false;

	// Map object instance
	protected static Map map = null;

	/**
	 * The Map is accesed through this method
	 * 
	 * @return The <code>Map</code> of the application
	 */
	private static Map theMap() {
		if (map == null) {
			map = new Map();
		}
		return map;
	}

	// Getters
	public boolean canWePlay() {
		return play;
	}
	public boolean isFirstMove() {
		return firstMove;
	}

	// Timestamp
	public long getGameStart() {
		return timestampGameStart;
	}

	public boolean mineStepOn() {
		return mineStepOn;
	}

	// Setters
	public void setPlay(boolean areWePlaying) {
		play = areWePlaying;
	}
	public void setFirstMove(boolean theFirstMove) {
		firstMove = theFirstMove;
	}
	public void setGameStart(long millis) {
		timestampGameStart = millis;
	}
	public void setStepOnMine(boolean isMineStepOn) {
		mineStepOn = isMineStepOn;
	}

	// Gets an image off a file
	private BufferedImage getImage(String path) {
		BufferedImage ret = null;
		try {
			List images = new GifImageParser().getAllBufferedImages(new ByteSourceInputStream(GamePanel
					.class.getClassLoader().getResourceAsStream(path), path));

			if (images != null && images.size() > 0) {
				ret = (BufferedImage) images.get(0);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	// Use color filter in order to get transparency
	private Image filterImage(String path, Color filter) {
		BufferedImage ret = null;

		try {
			BufferedImage tmp = getImage(path);

			// This particular bit mask has transparent behavior
			if (tmp != null) {MapColorsFilter f = new MapColorsFilter(filter.getRGB(),0x00FFFFFF & filter.getRGB());

				// Make sure we obtain a standard RGB image
				ret = f.createCompatibleDestImage(tmp, ColorModel.getRGBdefault());
				f.filter(tmp, ret);

				System.out.println(path);
				System.out.println(tmp);
				System.out.println(ret);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	// Panel constructor
	public GamePanel() {
		try {
			// Load the gif images
			mine = new ImageIcon(filterImage(Constants.MINE_PATH, Color.WHITE));
			flag = new ImageIcon(filterImage(Constants.FLAG_PATH, Color.WHITE));
			redMine = new ImageIcon(getImage(Constants.RED_MINE_PATH));

			questionMark = new ImageIcon(filterImage(Constants.QUESTION_MARK_PATH, Color.WHITE));
			wrongFlag = new ImageIcon(filterImage(Constants.WRONG_FLAG_PATH, Color.WHITE));
			smiley = new ImageIcon(filterImage(Constants.SMILEY_PATH, Color.WHITE));

			// Build the dialog
			jbInit();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void jbInit() throws Exception {
		this.setLayout(gridBagLayout1);

		// Matrix of JLabels
		matrix = new GraphicSlot[Constants.ROWS][Constants.COLUMNS];
		JTextField clock = new JTextField(Constants.ZERO);

		clock.setEnabled(false);
		clock.setHorizontalAlignment(JTextField.RIGHT);

		Observer observer = new Observer(this, clock);
		placeButtonsPanel(observer, clock);

		// Place the JLabels
		for (int row = 0; row < Constants.ROWS; row++) {
			for (int column = 0; column < Constants.COLUMNS; column++) {

				// Create the JLabel and get it into the panel
				matrix[row][column] = new GraphicSlot(row, column);
				matrix[row][column].addMouseListener(observer);
				matrix[row][column].setMaximumSize(new java.awt.Dimension(Constants.SLOT_WIDTH, Constants.SLOT_HEIGHT));
				matrix[row][column].setPreferredSize(new java.awt.Dimension(Constants.SLOT_WIDTH, Constants.SLOT_HEIGHT));

				// The constraints are ok
				this.add(matrix[row][column], new GridBagConstraints(column,
						row + 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
						GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			}
		}
	}

	protected void placeButtonsPanel(Observer observer, JTextField clock) {

		// Reset button and counters
		resetButton = new JButton(smiley);
		resetButton.setName(Constants.RESET);
		resetButton.addMouseListener(observer);

		// Another panel to get a place in the grid for it
		JPanel panelButtonCounter = new JPanel();
		GridBagLayout layoutPanel = new GridBagLayout();
		panelButtonCounter.setLayout(layoutPanel);

		// Contains a button and a non editable box with the number of mines remaining
		box = new JTextField();

		// Add the lower panel
		this.add(panelButtonCounter, new GridBagConstraints(0, 0,
				Constants.COLUMNS, 1, 1.0, 2.0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(10, 10, 10, 10), 0, 0));

		// Add clock, buttons and mine counter to panel
		panelButtonCounter.add(box, new GridBagConstraints(0, 0, 1, 1, 1.0,
				1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 0, 0));

		panelButtonCounter.add(resetButton, new GridBagConstraints(1, 0, 1, 1,
				8.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 0, 0));

		panelButtonCounter.add(clock, new GridBagConstraints(2, 0, 1, 1, 1.0,
				1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 0, 0));

		// The clock size
		clock.setMaximumSize(new java.awt.Dimension(Constants.CLOCK_WIDTH, Constants.CLOCK_HEIGHT));
		clock.setPreferredSize(new java.awt.Dimension(Constants.CLOCK_WIDTH, Constants.CLOCK_HEIGHT));

		// Boxes: format and color
		box.setHorizontalAlignment(JTextField.RIGHT);
		box.setText(Integer.toString(Constants.NUMBER_OF_MINES));
		box.setSize(Constants.BOX_WIDTH, Constants.BOX_HEIGHT);
		box.setEditable(false);
		box.setPreferredSize(new java.awt.Dimension(Constants.BOX_WIDTH, Constants.BOX_HEIGHT));

		// Colors
		box.setBackground(Color.BLUE);
		box.setForeground(Color.GRAY);
		box.setBorder(BorderFactory.createLineBorder(Color.BLACK));

		clock.setBackground(Color.BLUE);
		clock.setForeground(Color.GRAY);
		clock.setBorder(BorderFactory.createLineBorder(Color.BLACK));
	}

	// Coordinates of the last mine found
	public void setLastMineCoordinates(int row, int column) {
		rowLastMine = row;
		columnLastMine = column;
	}

	/**
	 * Redraws the game map. Run through the map querying every position. If it
	 * is hidden, the color must be black. If it is revealed, the color is gray.
	 * If it is revealed, it must also write the number of mines around the slot.
	 * 
	 * @param isAMine
	 *            Tells if we are painting the last turn, this meaning, if the
	 *            player has just stept on a mine and we must reveal the game
	 *            panel situation
	 */
	public void redrawGamePanel(boolean isAMine, int theRow, int theColumn) {
		Map map = theMap();

		// How many mines left?
		box.setText(Integer.toString(map.remainingMines()));

		// Run through the map
		for (int row = 0; row < Constants.ROWS; row++) {
			for (int column = 0; column < Constants.COLUMNS; column++) {

				// If it is not hidden
				GraphicSlot slot = matrix[row][column];
				if (!map.isHidden(row, column)) {
					map.setHasBeenVisitedThisTurn(row, column, false);

					// Set background to gray
					slot.setBackground(Constants.VISIBLE_BACKGROUND);
					slot.setBorder(GRAY_BORDER);

					slot.setIcon(null);

					int numMines = map.getMinesAround(row, column);

					if (numMines > 0) {
						slot.setForeground(Color.BLUE);
					}
					else {
						slot.setForeground(Constants.VISIBLE_BACKGROUND);
					}
					slot.writeMinesNumber(numMines);
				}

				else if (isAMine) {
					// In this case, we paint a hidden slot in the last turn,
					// when the player just stept on a mine
					paintHiddenSlot(slot, map.isSuspicious(row, column), true,
							false);
				}

				else if (map.isSuspicious(row, column)) {
					// Hidden and suspicious
					paintHiddenSlot(slot, true, false, false);
				}

				else if (map.hasQuestionMark(row, column)) {
					// With a question mark
					paintHiddenSlot(slot, false, false, true);
				}

				else {
					// In this case, the slot returns to clean state
					if (isFirstMove() || (slot.getRow() == theRow && slot.getColumn() == theColumn)) {

						// Hidden and free of suspicion
						paintHiddenSlot(slot, false, false, false);
					}
				}
			}
		}
	}

	// Paints a hidden slot in the panel
	private void paintHiddenSlot(GraphicSlot slot, boolean isSuspicious,
			boolean isMineStepOn, boolean hasQuestionMark) {

		slot.setText(null);
		slot.setBorder(BEVEL_BORDER);
		slot.setBackground(Constants.HIDDEN_BACKGROUND);

		// Was there a mine?
		boolean isThereAMine = theMap().isThereAMine(slot.getRow(), slot.getColumn());

		if (isMineStepOn && !isSuspicious) {
			if (isThereAMine) {
				if (slot.getRow() == rowLastMine && slot.getColumn() == columnLastMine) {
					slot.setIcon(redMine);
				}

				else {
					slot.setIcon(mine);
				}
			}

			else {
				slot.setForeground(Constants.HIDDEN_BACKGROUND);
				slot.setText(Constants.EMPTY_SLOT_TEXT);
				slot.setIcon(null);
			}
		}

		else if (isMineStepOn && isSuspicious && !isThereAMine) {
			slot.setIcon(wrongFlag);
		}

		else if (isSuspicious) {
			slot.setIcon(flag);
		}

		else if (hasQuestionMark) {
			slot.setIcon(questionMark);
		}

		else {
			slot.setForeground(Constants.HIDDEN_BACKGROUND);
			slot.setText(Constants.EMPTY_SLOT_TEXT);
			slot.setIcon(null);
		}

	}

	// Define JLabel with coordinates
	protected class GraphicSlot extends JLabel {

		// Properties
		private int row;
		private int column;

		// New slot with black and white border
		public GraphicSlot(int theRow, int theColumn) {
			super();
			row = theRow;
			column = theColumn;
			this.setOpaque(true);

			// Every slot has a white border when just created
			setBorder(BEVEL_BORDER);
			setBackground(Constants.HIDDEN_BACKGROUND);

			// Text font and alignment
			setFont(new Font(Constants.FONT, Font.BOLD, Constants.FONT_SIZE));
			this.setHorizontalAlignment(JLabel.CENTER);
			setForeground(Constants.HIDDEN_BACKGROUND);
			setText(Constants.EMPTY_SLOT_TEXT);

			// Text: orange by default
			setPreferredSize(new java.awt.Dimension(Constants.SLOT_WIDTH, Constants.SLOT_HEIGHT));
		}

		// Getters
		public int getRow() {
			return row;
		}
		public int getColumn() {
			return column;
		}

		// Setters
		public void setColor(Color color) {
			setBackground(color);
		}

		// Nothing around, then nothing written
		public void writeMinesNumber(int minesNumber) {
			if (minesNumber != 0) {
				setText(formatMinesNumber(minesNumber));
				setFont(new Font(Constants.FONT, Font.BOLD, Constants.FONT_SIZE));
			}
			else {
				setText(Constants.EMPTY_SLOT_TEXT);
				setFont(new Font(Constants.FONT, Font.BOLD, Constants.FONT_SIZE));
			}
		}

		/**
		 * @param minesNumber
		 *            Number of mines to paint in the slot
		 * @return A proper HTML string to render the number of mines around the slot
		 */
		private String formatMinesNumber(int minesNumber) {
			return MessageFormat.format(MINE_TEMPLATE, COLORS[minesNumber - 1], minesNumber);
		}
	}

	// Events observer
	// Every slot is subscribed to it
	private class Observer extends java.awt.event.MouseAdapter {

		// Panel
		private GamePanel gamePanel = null;

		// Field
		private JTextField clock = null;

		public Observer(GamePanel panel, JTextField look) {
			super();
			gamePanel = panel;
			clock = look;

			// The observer sets the clock to zero
			ClockThread.reset(clock);
		}

		// Capture a click
		public void mousePressed(MouseEvent e) {

			// Mine step on?
			boolean mine = gamePanel.mineStepOn();
			Component c = e.getComponent();

			try {
				Map map = theMap();
				if (c == null) {
					return;
				}

				// If the event comes from a slot
				if (c instanceof GraphicSlot && gamePanel.canWePlay()) {
					manageSlotEvent(e, map, c);
				}

				// If it comes from a button
				else {
					String name = c.getName();
					if (name != null && name.equals(Constants.RESET)) {

						// Resetting the game
						map.populateMap();
						gamePanel.setPlay(true);
						gamePanel.setStepOnMine(false);
						gamePanel.setFirstMove(true);
						mine = false;
						clock.setText(Constants.ZERO);
					}
				}

			} catch (BombException eb) {
				gamePanel.setPlay(false);
				gamePanel.setStepOnMine(true);
				gamePanel.setLastMineCoordinates(eb.getRow(), eb.getColumn());

				mine = true;
				ClockThread.reset(clock);

			} finally {
				gamePanel.redrawGamePanel(mine, c instanceof GraphicSlot ? ((GraphicSlot) c).getRow() : -1,
						c instanceof GraphicSlot ? ((GraphicSlot) c).getColumn() : -1);
			}
		}

		// This method manages the click over the slots: is it a mine, is it suspicious...?
		private void manageSlotEvent(MouseEvent e, Map map, Component c)
				throws HeadlessException, BombException {

			GraphicSlot slot = (GraphicSlot) c;
			int row = slot.getRow();
			int column = slot.getColumn();

			if (e.getButton() == MouseEvent.BUTTON1) {

				// Where did the event come from?
				if (!map.isSuspicious(row, column)) {

					map.click(row, slot.getColumn());
				}

				// Has the player won?
				if (map.hasWon()) {
					win();
				}
			}

			else if (e.getButton() == MouseEvent.BUTTON3) {
				// Marks the mines with a capital 'X'
				if (map.isHidden(row, column)) {

					if (map.isSuspicious(row, column)) {
						// Free of suspicion
						map.setFreeOfSuspicion(row, column);

						// Mark with a question mark
						map.addQuestionMark(row, column);
					}

					else if (map.hasQuestionMark(row, column)) {
						// Clear it
						map.clearQuestionMark(row, column);
					}

					else {
						// Suspicious
						map.setSuspicious(row, column);
					}

					// Has the player won?
					if (map.hasWon()) {
						win();
					}
				}
			}

			// Counter
			if (gamePanel.isFirstMove()) {
				gamePanel.setGameStart(System.currentTimeMillis());
				gamePanel.setFirstMove(false);
				ClockThread.startGame();
			}
		}

		// Calculates the game time
		private void win() throws HeadlessException {

			// Stop the clock; then paint the message
			ClockThread.reset(clock);
			gamePanel.setPlay(false);

			long now = System.currentTimeMillis();
			long time = now - gamePanel.getGameStart();

			StringBuffer message = new StringBuffer(Constants.YOU_HAVE_FINISHED_IN);

			message.append(Math.round(time / 1000.0));
			message.append(Constants.SECONDS);

			JOptionPane.showMessageDialog(gamePanel, message.toString(), Constants.CONGRATULATIONS, JOptionPane.PLAIN_MESSAGE);
		}
	}
}