package foogame;

import java.util.*;
import java.util.stream.Collectors;

public class WinChecker {
	public static Optional<Color> winCheck(Board b) {
		return Optional.empty();
		//return numStonesCheck(b).map(Optional::of).orElse(
		//		Arrays.stream(Color.values()).filter(c -> winCheck(b, c) || winCheck(b.rotateBoard(), c)).findFirst());
	}

	public static Optional<Color> winCheck2(Board b) {
		Color c = b.whoseTurn.other();
		//System.out.printf("winchecking for %s%n", c);
		if (winCheck(b, c) || winCheck(b.rotateBoard(), c)) {
			return Optional.of(c);
		}
		Optional<Color> result = numStonesCheck(b);
		if (!result.isPresent()) {
			result = noMovesCheck(b);
		}
		return result;
	}

	/*public static Optional<Color> winCheck(Board b) {
		return numStonesCheck(b).map(Optional::of).orElse(
			Optional.of(b.whoseTurn)(c -> winCheck(b, c) || winCheck(b.rotateBoard(), c)).findFirst());
	}*/

	private static Optional<Color> numStonesCheck(Board b) {
		boolean someoneOut = false;
		for (Color c : Color.values()) {
			someoneOut |= b.getNumStones(c) == 0;
		}
		if (!someoneOut) {
			return Optional.empty();
		}

		/*EnumMap<Color, Integer> numStonesOnBoard = new EnumMap<>(Color.class);
		for (Color c : Color.values()) {
			int n = 0;
			for (int i=0; i<5; i++) {
				for (int j=0; j<5; j++) {
					Optional<Color> val = b.getBoardArray()[i][j];
					if (val.filter(x -> x == c).isPresent())
						n++;
				}
			}
			numStonesOnBoard.put(c, n);
		}*/

		return b.numStonesOnBoard()
				.entrySet()
				.stream()
				.max((x, y) -> Integer.compare(x.getValue(), y.getValue()))
				.map(x -> x.getKey());
	}
	
	private static Optional<Color> noMovesCheck(Board b) {
		if (!b.getLegalMoves().findAny().isPresent()) {
			System.out.println("no moves available; returning black");
			return Optional.of(Color.BLACK); // If no moves available, give the win to Blue because reasons
		}
		return Optional.empty();
	}

	private static boolean winCheck(Board board, Color c) {
		for (int i=0; i<board.size; i++) {
			if (winCheck(board, c, new HashSet<>(), new Position(i, 0))) {
				return true;
			}
		}
		return false;
	}

	private static boolean winCheck(Board board, Color c, Set<Position> visitedSet, Position curPos) {
		if (visitedSet.contains(curPos) || !inBounds(board, curPos) ||
				!Optional.of(board.getBoardArray()[curPos.x][curPos.y]).filter(x -> !x.isEmpty()).map(Stack::top)
				.filter(x -> x.color == c && x.type != PieceType.WALL).isPresent()) {
			//System.out.printf("false for %s at (%d, %d)%n", c, curPos.x, curPos.y);
			return false;
		}
		if (curPos.y == board.size-1) {
			return true;
		}
		Set<Position> newVisitedSet = new HashSet<>(visitedSet);
		newVisitedSet.add(curPos);
		return Arrays.stream(Direction.values())
				.map(d -> curPos.move(d))
				.anyMatch(p -> winCheck(board, c, newVisitedSet, p));
	}

	private static boolean inBounds(Board b, Position p) {
		return b.inBounds(p.x) && b.inBounds(p.y);
	}

}
