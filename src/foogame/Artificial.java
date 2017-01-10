package foogame;

import java.util.Optional;

public class Artificial {

	static class Artifical1 implements Player {
		public void acceptUpdate(GameUpdate update) {}

		public Move getMove(Board board) {
			return AiMove1(board);
		}
	}

	static class Artifical2 implements Player {
		public void acceptUpdate(GameUpdate update) {}

		public Move getMove(Board board) {
			return AiMove2(board);
		}
	}

	/*
	 * makes a move in the first open spot returns it as an int array the first
	 * element is the x coordinate (where to place in boardArray[]) the second
	 * element is the y coordinate (there to place in boardArray[x][])
	 */
	public static Move AiMove1(Board board) {
		Optional<Stack>[][] boardArray = board.getBoardArray();
		for (int i = 0; i < boardArray.length; i++) {
			for (int j = 0; j < boardArray[i].length; j++) {
				Move m = new PlaceStone(board.whoseTurn, i, j, PieceType.FLAT);
				if (board.isLegalMove(m)) {
					return m;
				}
			}
		}
		return null;
	}

	/*
	 * same as AiMove1 but starts at the bottom \(��)/
	 */
	public static Move AiMove2(Board board) {
		Optional<Stack>[][] boardArray = board.getBoardArray();
		for (int i = boardArray.length-1; i > 0; i--) {
			for (int j = boardArray[i].length-1; j > 0; j--) {
				Move m = new PlaceStone(board.whoseTurn, i, j, PieceType.FLAT);
				if (board.isLegalMove(m)) {
					return m;
				}
			}
		}
		return null;
	}
}
