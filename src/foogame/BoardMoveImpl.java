package foogame;

import java.util.EnumMap;

public class BoardMoveImpl {
	
	public static boolean isLegalMove(Board b, Move m) {
		if (m.color != b.whoseTurn) {
			return false;
		}
		if (m instanceof PlaceStone) {
			return isLegalPlaceStone(b, (PlaceStone) m);
		} else if (m instanceof MoveStack) {
			return isLegalMoveStack(b, (MoveStack) m);
		} else {
			throw new RuntimeException();
		}
	}

	private static boolean isLegalPlaceStone(Board b, PlaceStone m) {
		return inBounds(b, m) &&
				(m.type.equals(PieceType.CAPSTONE) ? b.getNumCapstones(m.color) > 0 : b.getNumStones(m.color) > 0)
				&& b.getStack(m.x, m.y).isEmpty();
	}

	private static boolean isLegalMoveStack(Board b, MoveStack m) {
		if (m.count > b.size || m.count < 1) {
			//System.out.println("bad count");
			return false;
		}
		if (!inBounds(b, m)) {
			//System.out.println("out of bounds");
			return false;
		}

		Stack s = b.getStack(m.x, m.y);
		if (s.isEmpty()) {
			//System.out.println("nothing to move from");
			return false;
		}
		
		if (s.top().color != b.whoseTurn) {
			//System.out.println("You can't move someone else's stone");
			return false;
		}

		int row = m.x + m.dir.dx * m.dropCounts.length;
		int col = m.y + m.dir.dy * m.dropCounts.length;

		for (int i = m.dropCounts.length - 1; i >= 0; i--) {
			int grabThisTime = m.dropCounts[i];
			Stack[] stacks = s.split(grabThisTime);
			Stack miniStack = stacks[1]; // aka grabStack
			s = stacks[0];
			if (!isLegalMiniStack(b, row, col, miniStack)) {
				//System.out.println("illegal ministack");
				return false;
			}
			row -= m.dir.dx;
			col -= m.dir.dy;
		}

		return true;
	}

	private static boolean isLegalMiniStack(Board b, int row, int col, Stack miniStack) {
		if (!b.inBounds(row) || !b.inBounds(col)) {
			//System.out.println("out of bounds  2");
			return false;
		}

		if (b.getStack(row, col).isEmpty()) {
			return true;
		}

		PieceType t = b.getStack(row, col).top().type;

		if (t == PieceType.CAPSTONE) {
			//System.out.println("can't move onto capstone");
			return false;
		}

		if (t == PieceType.FLAT) {
			return true;
		}

		if (t == PieceType.WALL) {
			if (miniStack.getCopy()[0].type == PieceType.CAPSTONE) {
				//System.out.println("can't move onto wall unless ur a capstone");
				return true;
			}
			return false;
		}

		throw new RuntimeException("Impossible!");
	}

	private static boolean inBounds(Board b, PlaceStone m) {
		return b.inBounds(m.x) && b.inBounds(m.y);
	}

	private static boolean inBounds(Board b, MoveStack m) {
		return b.inBounds(m.x)
				&& b.inBounds(m.y)
				&& b.inBounds(m.x + m.dir.dx * m.dropCounts.length)
				&& b.inBounds(m.y + m.dir.dy * m.dropCounts.length);
	}
	
	public static Board makeMove(Board b, Move m) {
		if (!isLegalMove(b, m)) {
			throw new RuntimeException("bad move");
		}
		if (m instanceof PlaceStone) {
			return doPlaceStone(b, (PlaceStone) m);
		} else if (m instanceof MoveStack) {
			return doMoveStack(b, (MoveStack) m);
		} else {
			throw new RuntimeException();
		}
	}

	private static Board doPlaceStone(Board b, PlaceStone m) {
		Stack[][] array = b.getBoardArray();
		array[m.x][m.y] = new Stack(new Stone(m.type, m.color));
		EnumMap<Color, Integer> newNumStones = b.getNumStones();
		EnumMap<Color, Integer> newNumCapstones = b.getNumCapstones();
		if (m.type.equals(PieceType.CAPSTONE)) {
			newNumCapstones = new EnumMap<Color, Integer>(b.getNumCapstones());
			newNumCapstones.compute(m.color, (k, v) -> v - 1);
		} else {
			newNumStones = new EnumMap<Color, Integer>(b.getNumStones());
			newNumStones.compute(m.color, (k, v) -> v - 1);
		}
		return new Board(array, newNumStones, newNumCapstones, m.color.other(), b.turnNumber + 1);
	}

	private static Board doMoveStack(Board b, MoveStack m) {
		Stack[][] array = b.getBoardArray();
		Stack s = array[m.x][m.y];

		int row = m.x + m.dir.dx * m.length;
		int col = m.y + m.dir.dy * m.length;

		for (int i = m.length - 1; i >= 0; i--) {
			int grabThisTime = m.dropCounts[i];
			Stack[] stacks = s.split(grabThisTime);
			Stack miniStack = stacks[1]; // aka grabStack
			s = stacks[0];
			Stack remain = applyMiniStack(b, row, col, miniStack);
			//System.out.println(remain);
			array[row][col] = remain;

			row -= m.dir.dx;
			col -= m.dir.dy;
		}

		array[m.x][m.y] = s;

		return new Board(array, b.getNumStones(), b.getNumCapstones(), m.color.other(), b.turnNumber + 1);
	}

	private static Stack applyMiniStack(Board b, int row, int col, Stack miniStack) {
		Stack current = b.getStack(row, col);
		if (current.isEmpty()) {
			return miniStack;
		}
		if (current.top().type == PieceType.WALL) {
			// flatten the top
			current = current.split(1)[0].addOnTop(new Stack(new Stone(PieceType.FLAT, current.top().color)));
		}
		return current.addOnTop(miniStack);
	}
}
