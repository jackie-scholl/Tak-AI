package foogame;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Optional;
import java.util.stream.Stream;

public class Board {
	private final EnumMap<Color, Integer> numStones;
	private final Stack[][] boardArray;
	public final Color whoseTurn;
	public final int size;

	public Board(Stack[][] boardArray, EnumMap<Color, Integer> numStones, Color whoseTurn) {
		this.numStones = new EnumMap<Color, Integer>(numStones);
		this.boardArray = deepCopy(boardArray);
		this.whoseTurn = whoseTurn;
		this.size = boardArray.length;
	}

	public Board(Stack[][] boardArray) {
		this(boardArray, baseNumStones(boardArray.length), Color.WHITE);
	}

	public Board(int size) {
		this(emptyBoard(size));
	}

	private static EnumMap<Color, Integer> baseNumStones(int size) {
		int numStonesToUse = size == 5 ? 21 : size == 6 ? 30 : -1;
		EnumMap<Color, Integer> numStones = new EnumMap<Color, Integer>(Color.class);
		numStones.put(Color.WHITE, numStonesToUse);
		numStones.put(Color.BLACK, numStonesToUse);
		return numStones;
	}

	private static Stack[][] deepCopy(Stack[][] original) {
		if (original == null) {
			return null;
		}

		@SuppressWarnings("unchecked") final Stack[][] result = new Stack[original.length][];
		for (int i = 0; i < original.length; i++) {
			result[i] = Arrays.copyOf(original[i], original[i].length);
		}
		return result;
	}

	private static final Stack[][] emptyBoard(int size) {
		@SuppressWarnings("unchecked") Stack[][] boardArray = new Stack[size][size];
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				boardArray[i][j] = Stack.EMPTY;
			}
		}
		return boardArray;
	}

	public boolean isLegalMove(Move m) {
		if (m.color != this.whoseTurn) {
			return false;
		}
		if (m instanceof PlaceStone) {
			return isLegalPlaceStone((PlaceStone) m);
		} else if (m instanceof MoveStack) {
			return isLegalCapture((MoveStack) m);
		} else {
			throw new RuntimeException();
		}
	}

	private boolean isLegalPlaceStone(PlaceStone m) {
		return inBounds(m) && numStones.get(m.color) != 0 && boardArray[m.x][m.y].isEmpty();
	}

	private boolean isLegalCapture(MoveStack m) {
		return inBounds(m) ;//&& boardArray[m.x][m.y].filter(x -> x.color == m.color).isPresent()
				//&& boardArray[m.x + m.dir.dx][m.y + m.dir.dy].filter(x -> x.color != m.color && x.type == PieceType.STONE).isPresent();
	}
	
	private boolean isLegalMoveStack(MoveStack m) {
		if (m.count > size || m.count < 1) {
			return false;
		}
		if (!inBounds(m)) {
			return false;
		}
		
		Stack s = boardArray[m.x][m.y];
		if (s.isEmpty()) {
			return false;
		}
		
		//Stack[] stacks = s.split(m.count);
		
		//Stack grabStack = stacks[1];
		
		int row = m.x + m.dir.dx * m.dropCounts.length;
		int col = m.y + m.dir.dy * m.dropCounts.length;
		
		for (int i=m.dropCounts.length - 1; i >= 0; i--) {
			row -= m.dir.dx;
			col -= m.dir.dy;
			int grabThisTime = m.dropCounts[i];
			Stack[] stacks = s.split(grabThisTime);
			Stack miniStack = stacks[1]; // aka grabStack
			s = stacks[0];
			if (!isLegalMiniStack(row, col, miniStack)) {
				return false;
			}
		}
		
		return true;
	}
	
	private boolean isLegalMiniStack(int row, int col, Stack miniStack) {
		if (!inBounds(row) || !inBounds(col)) {
			return false;
		}
		
		if (boardArray[row][col].isEmpty()) {
			return true;
		}
		
		PieceType t = boardArray[row][col].top().type;
		
		if (t == PieceType.CAPSTONE) {
			return false;
		}
		
		if (t == PieceType.FLAT) {
			return true;
		}
		
		if (t == PieceType.WALL) {
			if (miniStack.getCopy()[0].type == PieceType.CAPSTONE) {
				return true;
			}
			return false;
		}
		
		throw new RuntimeException("Impossible!");
	}
	
	
	private boolean inBounds(PlaceStone m) {
		return inBounds(m.x) && inBounds(m.y);
	}

	private boolean inBounds(MoveStack m) {
		return inBounds(m.x)
			&& inBounds(m.y)
			&& inBounds(m.x + m.dir.dx * m.dropCounts.length)
			&& inBounds(m.y + m.dir.dy * m.dropCounts.length);
	}

	public boolean inBounds(int x) {
		return x >= 0 && x < size;
	}

	public Board makeMove(Move m) {
		if (!isLegalMove(m)) {
			throw new RuntimeException("bad move");
		}
		if (m instanceof PlaceStone) {
			return doPlaceStone((PlaceStone) m);
		} else if (m instanceof MoveStack) {
			return doMoveStack((MoveStack) m);
		} else {
			throw new RuntimeException();
		}
	}

	private Board doPlaceStone(PlaceStone m) {
		Stack[][] array = deepCopy(boardArray);
		array[m.x][m.y] = new Stack(new Stone(m.type, m.color));
		EnumMap<Color, Integer> numStones = new EnumMap<Color, Integer>(this.numStones);
		numStones.compute(m.color, (k, v) -> v - 1);
		return new Board(array, numStones, m.color.other());
	}

	/*private Board doCapture(MoveStack m) {
		Optional<Stack>[][] array = deepCopy(boardArray);
		//PieceType oldType = array[m.x][m.y].get().top().type;
		Stack movingStack = array[m.x][m.y].get();
		array[m.x][m.y] = Optional.empty();
		Optional<Stack> bottomStack = array[m.x+m.dir.dx][m.y+m.dir.dy];
		if (bottomStack.isPresent()) {
			array[m.x + m.dir.dx][m.y + m.dir.dy] = Optional.of(bottomStack.get().addOnTop(movingStack));
		} else {
			array[m.x + m.dir.dx][m.y + m.dir.dy] = Optional.of(movingStack);
		}
		return new Board(array, this.numStones, m.color.other());
	}*/
	
	private Board doMoveStack(MoveStack m) {
		Stack[][] array = deepCopy(boardArray);
		Stack s = boardArray[m.x][m.y];
		
		int row = m.x + m.dir.dx * m.dropCounts.length;
		int col = m.y + m.dir.dy * m.dropCounts.length;
		
		for (int i=m.dropCounts.length - 1; i >= 0; i--) {
			row -= m.dir.dx;
			col -= m.dir.dy;
			int grabThisTime = m.dropCounts[i];
			Stack[] stacks = s.split(grabThisTime);
			Stack miniStack = stacks[1]; // aka grabStack
			s = stacks[0];
			Stack remain = applyMiniStack(row, col, miniStack);
			array[row][col] = remain;
		}
		
		array[m.x][m.y] = s;
		
		return new Board(array, this.numStones, m.color.other());
	}
	
	private Stack applyMiniStack(int row, int col, Stack miniStack) {
		Stack current = boardArray[row][col];
		if (current.isEmpty()) {
			return miniStack;
		}
		return current.addOnTop(miniStack);
	}


	public Optional<Color> hasAnyoneWon() {
		//Optional<Color> result = WinChecker.winCheck(this);
		Optional<Color> result = WinChecker.winCheck2(this);
		return result;
	}

	public Stack[][] getBoardArray() {
		return deepCopy(boardArray);
	}

	public EnumMap<Color, Integer> getNumStones() {
		return new EnumMap<Color, Integer>(numStones);
	}

	public int getNumStones(Color c) {
		return numStones.get(c);
	}

	public Board rotateBoard() {
		@SuppressWarnings("unchecked") Stack[][] array = new Stack[size][size];
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				array[i][j] = boardArray[size - j - 1][i];
			}
		}
		return new Board(array, this.numStones, this.whoseTurn);
	}

	public EnumMap<Color, Integer> numStonesOnBoard() {
		EnumMap<Color, Integer> numStonesOnBoard = new EnumMap<>(Color.class);
		for (Color c : Color.values()) {
			numStonesOnBoard.put(c, numStonesOnBoard(c));
		}
		return numStonesOnBoard;
	}

	public int numStonesOnBoard(Color c) {
		int n = 0;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				Stack val = boardArray[i][j];
				if (Optional.of(val).map(Stack::top).filter(x -> x.color == c && x.type == PieceType.FLAT).isPresent())
					n++;
			}
		}
		return n;
	}

	public Stream<Move> getLegalMoves() {
		return Stream.concat(
				Stream.concat(Position.positionStream(size).map(x -> new PlaceStone(whoseTurn, x.x, x.y, PieceType.FLAT)),
					Position.positionStream(size).map(x -> new PlaceStone(whoseTurn, x.x, x.y, PieceType.WALL))),
				Position.positionStream(size).flatMap(
						x -> Arrays.stream(Direction.values()).map(d -> new MoveStack(whoseTurn, x.x, x.y, d, 1))))
				// TODO: *actually* iterate through all possible stack moves
				.filter(this::isLegalMove);
	}
}
