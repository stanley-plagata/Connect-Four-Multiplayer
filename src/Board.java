public class Board {
	private int[][] gameBoard;
	private int remainingSpaces;
	private int boardSize = 8;

	public Board() {
		gameBoard = new int[boardSize][boardSize];
		remainingSpaces = boardSize * boardSize;

		for (int i = 0; i < boardSize; i++) {
			for (int j = 0; j < boardSize; j++) {
				gameBoard[i][j] = 0;
			}
		}
	}
	public void resetboard(){
		for (int i = 0; i < boardSize; i++) {
			for (int j = 0; j < boardSize; j++) {
				gameBoard[i][j] = 0;
			}
		}
	}
	/**
	 * @param row
	 *            The row index of the column being checked
	 * @return Number of remaining spaces in the column
	 */
	public int findRemainingColumnSpace(int row) {
		int remainingColumnSpace = -1;
		for (int i = 0; i < boardSize; i++) {
			if (gameBoard[row][i] == 0) {
				remainingColumnSpace = i;
			}
		}
		return remainingColumnSpace;
	}

	/**
	 * @param row
	 *            The row index of the row space being set
	 * @param column
	 *            The column index of the column space being set
	 * @param currentPlayer
	 *            The current player
	 */
	public void setSpaceOwnership(int row, int column, int currentPlayer) {
		gameBoard[row][column] = currentPlayer;
		remainingSpaces--;
	}

	/**
	 * @param row
	 *            The row index of the column being checked
	 * @param column
	 *            The column index of the column being checked
	 * @param currentPlayer
	 *            The current player
	 * @return If current player is owner of the space
	 */
	public boolean getSpaceOwnership(int row, int column, int currentPlayer) {
		return gameBoard[row][column] == currentPlayer;
	}

	/**
	 * @return Number of remaining spaces
	 */
	public int getRemainingSpaces() {
		return remainingSpaces;
	}

	/**
	 * @return Size of board
	 */
	public int getBoardSize() {
		return boardSize;
	}
}
