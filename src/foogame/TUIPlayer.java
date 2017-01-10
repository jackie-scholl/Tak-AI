package foogame;

import java.util.Optional;

public class TUIPlayer implements Player {
	public TUIPlayer() {}

	public void acceptUpdate(GameUpdate update) {
		//print out new board
		/*System.out.printf("Move by %s resulted in board:%n", update.last.color);
		System.out.println("+----------+");
		for (int i=0; i<5; i++) {
			System.out.print("|");
			for (int j=0; j<5; j++) {
				System.out.print(" ");
				System.out.print(update.board.getBoardArray()[i][j].map(c -> c.name().substring(0,1)).orElse(" "));
			}
			System.out.println("|");
		}
		System.out.println("+----------+");
		System.out.printf("Num stones left: %s%n", update.board.getNumStones());*/
		System.out.println(GameLogger.stringifyUpdate(update));
	}

	public Move getMove(Board board) {
		//3 5
		//3 5 UP
		while (true) {
			String result = System.console().readLine("Make move (%s): ", board.whoseTurn.name());
			Optional<Move> move = parseMove(board, result);
			if (move.isPresent()) {
				return move.get();
			}
		}
	}

	public Optional<Move> parseMove(Board board, String result) {
		String[] parts = result.split(" ");
		if (parts.length < 3 || parts.length > 4) {
			System.err.println("wrong number of spaces");
			return Optional.empty();
		}
		String command = parts[0];
		int x, y;
		try {
			x = Integer.parseInt(parts[1]) - 1;
			y = Integer.parseInt(parts[2]) - 1;
		} catch (NumberFormatException e) {
			System.err.printf("Did not like number given.%n");
			return Optional.empty();
		}
		Move m;
		if (parts.length == 3) {
			PieceType type = PieceType.valueOf(command.toUpperCase());
			m = new PlaceStone(board.whoseTurn, x, y, type);
		} else {
			m = new MoveStack(board.whoseTurn, x, y, Direction.valueOf(parts[3].toUpperCase()));
		}
		boolean isLegal = board.isLegalMove(m);
		if (isLegal) {
			return Optional.of(m);
		} else {
			System.out.println("not legal");
			return Optional.empty();
		}
		//return board.isLegalMove(m)? Optional.of(m) : Optional.empty();

	}

}
