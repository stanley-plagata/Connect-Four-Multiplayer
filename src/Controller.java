public class Controller {
	private static int winSize = 4;
	private static int boardSize = 8;

	/**
	 * @param currentPlayer
	 *            The current player
	 * @return The player who will take their turn next
	 */
	public static int changePlayer(int currentPlayer) {
		if (currentPlayer == 1) {
			currentPlayer++;
		} else if (currentPlayer == 2) {
			currentPlayer--;
		}
		return currentPlayer;
	}

	/**
	 * @param row
	 *            The row index of the column being checked
	 * @param column
	 *            The column index of the column being checked
	 * @param currentPlayer
	 *            The current player
	 * @return If either player has satisfied one of the victory conditions
	 */
	public static boolean isWon(Board gameBoard, int row, int column, int currentPlayer) {
		gameBoard.setSpaceOwnership(row, column, currentPlayer);

		if (isWon(gameBoard, row, column, 0, 1, currentPlayer) || isWon(gameBoard, row, column, -1, 0, currentPlayer)
				|| isWon(gameBoard, row, column, 1, 1, currentPlayer)
				|| isWon(gameBoard, row, column, -1, 1, currentPlayer)) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 * @param row
	 *            The row index of the column being checked
	 * @param column
	 *            The column index of the column being checked
	 * @param rowDirection
	 *            The value to be added to row
	 * @param columnDirection
	 *            The value to be added to column
	 * @param currentPlayer
	 *            The current player
	 * @return (Helper method for isGameover)
	 */
	private static boolean isWon(Board gameBoard, int row, int column, int rowDirection, int columnDirection,
			int currentPlayer) {
		int tempRow = row;
		int tempColumn = column;

		int count = 0;

		while (count < winSize && isValid(tempRow, tempColumn)) {
			if (gameBoard.getSpaceOwnership(tempRow, tempColumn, currentPlayer) == false) {
				break;
			}
			tempRow += rowDirection;
			tempColumn += columnDirection;
			count++;
		}

		tempRow = row - rowDirection;
		tempColumn = column - columnDirection;

		while (count < winSize && isValid(tempRow, tempColumn)) {
			if (gameBoard.getSpaceOwnership(tempRow, tempColumn, currentPlayer) == false) {
				break;
			}
			tempRow -= rowDirection;
			tempColumn -= columnDirection;
			count++;
		}

		if (count == winSize) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @param row
	 *            The row index of the column being checked
	 * @param column
	 *            The column index of the column being checked
	 * @return (Helper method for isGameover)
	 */
	private static boolean isValid(int row, int column) {
		if (row >= 0 && row < boardSize && column >= 0 && column < boardSize) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @return If the board has no more remaining spaces and neither player has
	 *         satisfied one of the victory conditions
	 */
	public static boolean isDraw(Board gameBoard) {
		int remainingSpots = gameBoard.getRemainingSpaces();
		if (remainingSpots == 0) {
			return true;
		} else {
			return false;
		}
	}

}
