import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class MatchGUI {
	private ObjectOutputStream userOutput;
	private int playerNum;
	private Board gameBoard;
	private int boardSize;
	
	/* initializes the spaces labels */
	private JLabel[][] spaces;
	private JFrame boardFrame;
	private JPanel boardPanel;
	
	/*
	 * initializes the current player (which determines the color of the chip
	 * dropped)
	 */
	private int currentPlayer = 1;
	private int placeChipAt;

	/* initializes the board object of this MatchGUI object */

	/**
	 * 
	 * @param out
	 *            Output stream to server
	 * @param playerNum
	 *            if playerNum is 1, you will be player-1 and vice versa.
	 */
	public MatchGUI(ObjectOutputStream out, final int playerNum) {
		userOutput = out;
		this.playerNum = playerNum;
		gameBoard = new Board();
		boardSize = gameBoard.getBoardSize();
		createMatchGUI();
	}

	public void createMatchGUI() {
		/* initializes the frame object and the two panel objects */
		boardFrame = new JFrame();
		boardPanel = (JPanel) boardFrame.getContentPane();

		if (playerNum == 1) {
			boardFrame.setTitle("Connect Four - Player " + playerNum + " Your turn");
		} else {
			boardFrame.setTitle("Connect Four - Player " + playerNum + " Waiting for your opponent..");
		}

		/*
		 * initializes the buttons that the users click on to drop a chip into the board
		 */
		JButton[] buttons = new JButton[boardSize];
		for (int i = 0; i < boardSize; i++) {

			buttons[i] = new JButton();
			buttons[i].setActionCommand(Integer.toString(i));
			buttons[i].addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {

					placeChipAt = Integer.parseInt(e.getActionCommand());
					int columnSpaceLeft = gameBoard.findRemainingColumnSpace(placeChipAt);

					if (playerNum == currentPlayer) {

						if (columnSpaceLeft == -1) {
							/* if the chosen column is full */
							updateBoard();
							JOptionPane.showMessageDialog(null, "Please choose another column", "Column Full",
									JOptionPane.INFORMATION_MESSAGE);

						} else if (Controller.isWon(gameBoard, placeChipAt, columnSpaceLeft, currentPlayer)) {
							/* if the current player has won */
							updateBoard();
							sentBoard();
							endVictoryOption();

						} else if (Controller.isDraw(gameBoard)) {
							/* if the game has ended in a draw */
							updateBoard();
							sentBoard();
							endDrawOption();

						} else {
							/* else continue the game as normal */
							updateBoard();
							sentBoard();
							currentPlayer = Controller.changePlayer(currentPlayer);
							if (currentPlayer == playerNum) {
								boardFrame.setTitle("Connect Four - Player " + playerNum + " Your turn");
							} else {
								boardFrame.setTitle(
										"Connect Four - Player " + playerNum + " Waiting for your opponent..");
							}
						}
					}

				}
			});

			/* adds the button to the boardPanel object */
			boardPanel.add(buttons[i]);
		}

		/* initializes the label object for the spaces */
		spaces = new JLabel[boardSize][boardSize];
		for (int column = 0; column < boardSize; column++) {
			for (int row = 0; row < boardSize; row++) {
				spaces[row][column] = new JLabel();
				spaces[row][column].setHorizontalAlignment(SwingConstants.CENTER);
				spaces[row][column].setBorder(new LineBorder(Color.black));
				boardPanel.add(spaces[row][column]);
			}
		}

		/* sets the boardPanel object's layout */
		boardPanel.setLayout(new GridLayout(boardSize + 1, boardSize));

		/* sets the content pane, size, visibility, and behavior of the frame object */
		boardFrame.setContentPane(boardPanel);
		boardFrame.setSize(boardSize * 85, boardSize * 85);
		boardFrame.setVisible(true);
		boardFrame.setLocationRelativeTo(null);
		boardFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	/**
	 * Set the game chip onto the board
	 * 
	 * @param placedChip
	 *            the column which the game chip to put at
	 */
	public void setGameBoard(Integer placedChip) {

		this.placeChipAt = placedChip;
		int columnSpaceLeft = gameBoard.findRemainingColumnSpace(placeChipAt);

		if (playerNum != currentPlayer) {

			if (Controller.isWon(gameBoard, placeChipAt, columnSpaceLeft, currentPlayer)) {
				/* if the current player has won */
				updateBoard();
				endVictoryOption();

			} else if (Controller.isDraw(gameBoard)) {
				/* if the game has ended in a draw */
				updateBoard();
				endDrawOption();
			} else {
				/* else continue the game as normal */
				updateBoard();
				currentPlayer = Controller.changePlayer(currentPlayer);
				if (currentPlayer == playerNum) {
					boardFrame.setTitle("Connect Four - Player " + playerNum + " Your turn");
				} else {
					boardFrame.setTitle("Connect Four - Player " + playerNum + " Waiting for your opponent..");
				}
			}
		}

	}

	private void endVictoryOption() {
		String prompt = (playerNum == currentPlayer) ? "You win" : "You Lose";
		JOptionPane.showMessageDialog(boardFrame, "Game over", prompt, JOptionPane.INFORMATION_MESSAGE);
		try {
			userOutput.writeObject("***CLOSE***");
			userOutput.writeObject(currentPlayer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		boardFrame.dispose();
	}

	private void endDrawOption() {
		String prompt = "Draw";
		JOptionPane.showMessageDialog(boardFrame, "Game over", prompt, JOptionPane.INFORMATION_MESSAGE);
		try {
			userOutput.writeObject("***CLOSE***");
			userOutput.writeObject(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		boardFrame.dispose();
	}

	/**
	 * To send a game board update to your opponent
	 */
	public void sentBoard() {
		try {
			userOutput.writeObject(placeChipAt + "");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void endGameVictory() {
	}

	public void endGameDraw() {
		try {
			userOutput.writeObject("***CLOSE***");
			userOutput.writeObject(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Updates the color of the spaces chosen by the player & notisfy your opponent
	 * of the change
	 */
	public void updateBoard() {
		for (int row = 0; row < boardSize; row++) {
			for (int column = 0; column < boardSize; column++) {
				if (gameBoard.getSpaceOwnership(row, column, 1)) {
					spaces[row][column].setOpaque(true);
					spaces[row][column].setBackground(Color.red);
				} else if (gameBoard.getSpaceOwnership(row, column, 2)) {
					spaces[row][column].setOpaque(true);
					spaces[row][column].setBackground(Color.yellow);
				}
			}
		}
	}

}
