package foogame;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Board {
	private final EnumMap<Color, Integer> numStones;
	private final EnumMap<Color, Integer> numCapstones;
	private final Stack[][] boardArray;
	public final Color whoseTurn;
	public final int size;
	public final int turnNumber;

	public Board(Stack[][] boardArray, EnumMap<Color, Integer> numStones, EnumMap<Color, Integer> numCapstones, Color whoseTurn, int turnNumber) {
		this.numStones = new EnumMap<Color, Integer>(numStones);
		this.numCapstones = new EnumMap<Color, Integer>(numCapstones);
		this.boardArray = deepCopy(boardArray);
		this.whoseTurn = whoseTurn;
		this.size = boardArray.length;
		this.turnNumber = turnNumber;
	}

	public Board(Stack[][] boardArray) {
		this(boardArray, baseNumStones(boardArray.length), baseNumCapstones(boardArray.length), Color.WHITE, 0);
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
	
	private static EnumMap<Color, Integer> baseNumCapstones(int size) {
		int numStonesToUse = size < 7 ? 1 : 2;
		EnumMap<Color, Integer> numStones = new EnumMap<Color, Integer>(Color.class);
		numStones.put(Color.WHITE, numStonesToUse);
		numStones.put(Color.BLACK, numStonesToUse);
		return numStones;
	}

	private static Stack[][] deepCopy(Stack[][] original) {
		if (original == null) {
			return null;
		}

		final Stack[][] result = new Stack[original.length][];
		for (int i = 0; i < original.length; i++) {
			result[i] = Arrays.copyOf(original[i], original[i].length);
		}
		return result;
	}

	private static final Stack[][] emptyBoard(int size) {
		Stack[][] boardArray = new Stack[size][size];
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
			return isLegalMoveStack((MoveStack) m);
		} else {
			throw new RuntimeException();
		}
	}

	private boolean isLegalPlaceStone(PlaceStone m) {
		return inBounds(m) &&
				(m.type.equals(PieceType.CAPSTONE) ? numCapstones.get(m.color) > 0 : numStones.get(m.color) > 0)
				&& boardArray[m.x][m.y].isEmpty();
	}

	private boolean isLegalMoveStack(MoveStack m) {
		if (m.count > size || m.count < 1) {
			//System.out.println("bad count");
			return false;
		}
		if (!inBounds(m)) {
			//System.out.println("out of bounds");
			return false;
		}

		Stack s = boardArray[m.x][m.y];
		if (s.isEmpty()) {
			//System.out.println("nothing to move from");
			return false;
		}
		
		if (s.top().color != whoseTurn) {
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
			if (!isLegalMiniStack(row, col, miniStack)) {
				//System.out.println("illegal ministack");
				return false;
			}
			row -= m.dir.dx;
			col -= m.dir.dy;
		}

		return true;
	}

	private boolean isLegalMiniStack(int row, int col, Stack miniStack) {
		if (!inBounds(row) || !inBounds(col)) {
			//System.out.println("out of bounds  2");
			return false;
		}

		if (boardArray[row][col].isEmpty()) {
			return true;
		}

		PieceType t = boardArray[row][col].top().type;

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
		EnumMap<Color, Integer> newNumStones = this.numStones;
		EnumMap<Color, Integer> newNumCapstones = this.numCapstones;
		if (m.type.equals(PieceType.CAPSTONE)) {
			newNumCapstones = new EnumMap<Color, Integer>(this.numCapstones);
			newNumCapstones.compute(m.color, (k, v) -> v - 1);
		} else {
			newNumStones = new EnumMap<Color, Integer>(this.numStones);
			newNumStones.compute(m.color, (k, v) -> v - 1);
		}
		return new Board(array, newNumStones, newNumCapstones, m.color.other(), this.turnNumber + 1);
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
		Stack s = array[m.x][m.y];

		int row = m.x + m.dir.dx * m.length;
		int col = m.y + m.dir.dy * m.length;

		for (int i = m.length - 1; i >= 0; i--) {
			int grabThisTime = m.dropCounts[i];
			//System.out.printf("(%d, %d, %d)%n", row, col, grabThisTime);
			Stack[] stacks = s.split(grabThisTime);
			//System.out.printf("Stacks: %s%n", Arrays.deepToString(stacks));
			Stack miniStack = stacks[1]; // aka grabStack
			s = stacks[0];
			Stack remain = applyMiniStack(row, col, miniStack);
			//System.out.println(remain);
			array[row][col] = remain;

			row -= m.dir.dx;
			col -= m.dir.dy;
		}

		array[m.x][m.y] = s;

		return new Board(array, this.numStones, this.numCapstones, m.color.other(), this.turnNumber + 1);
	}

	private Stack applyMiniStack(int row, int col, Stack miniStack) {
		Stack current = boardArray[row][col];
		if (current.isEmpty()) {
			return miniStack;
		}
		if (current.top().type == PieceType.WALL) {
			// flatten the top
			current = current.split(1)[0].addOnTop(new Stack(new Stone(PieceType.FLAT, current.top().color)));
		}
		return current.addOnTop(miniStack);
	}

	public Optional<Color> hasAnyoneWon() {
		// Optional<Color> result = WinChecker.winCheck(this);
		Optional<Color> result = WinChecker.winCheck2(this);
		return result;
	}

	public Stack[][] getBoardArray() {
		return deepCopy(boardArray);
	}
	
	public Stack getStack(int x, int y) {
		return boardArray[x][y];
	}
	
	public Stack getStack(Position p) {
		return getStack(p.x, p.y);
	}

	public EnumMap<Color, Integer> getNumStones() {
		return new EnumMap<Color, Integer>(numStones);
	}

	public int getNumStones(Color c) {
		return numStones.get(c);
	}

	public Board rotateBoard() {
		Stack[][] array = new Stack[size][size];
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				array[i][j] = boardArray[size - j - 1][i];
			}
		}
		return new Board(array, this.numStones, this.numCapstones, this.whoseTurn, this.turnNumber);
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
				if (Optional.of(val)
						.filter(((Predicate<Stack>) Stack::isEmpty).negate())
						.map(Stack::top)
						.filter(x -> x.color == c && x.type == PieceType.FLAT)
						.isPresent())
					n++;
			}
		}
		return n;
	}

	public Stream<Move> getLegalMoves() {
		return Stream.concat(
				Stream.concat(
				Stream.concat(
						Position.positionStream(size).map(x -> new PlaceStone(whoseTurn, x.x, x.y, PieceType.FLAT)),
						Position.positionStream(size).map(x -> new PlaceStone(whoseTurn, x.x, x.y, PieceType.WALL))),
						Position.positionStream(size).map(x -> new PlaceStone(whoseTurn, x.x, x.y, PieceType.CAPSTONE))),
						getLegalMoveStacks())
				/*Position.positionStream(size).flatMap(
						x -> Arrays.stream(Direction.values()).map(d -> new MoveStack(whoseTurn, x.x, x.y, d, 1))))*/
				// TODO: *actually* iterate through all possible stack moves
				.filter(this::isLegalMove);
	}

	private Stream<Move> getLegalMoveStacks() {
		return Position.positionStream(size)
				.filter(x -> !boardArray[x.x][x.y].isEmpty())
				.filter(x -> boardArray[x.x][x.y].top().color == whoseTurn)
				.flatMap(x -> getLegalMoveStacks(x.x, x.y))
				.filter(this::isLegalMove);
	}

	private Stream<Move> getLegalMoveStacks(int x, int y) {
		return Arrays.stream(Direction.values()).flatMap(d -> getLegalMoveStacks(x, y, d));
	}

	private Stream<Move> getLegalMoveStacks(int x, int y, Direction d) {
		int maxToMove = boardArray[x][y].length();
		int maxDistance = getMaxDistance(x, y, d);
		
		List<List<Integer>> possibleDropCounts = new ArrayList<>();
		for (int i = 0; i <= maxToMove; i++) {
			int numberToMove = i;
			//int numberRemaining = maxToMove - numberToMove;
			List<List<Integer>> sub = getLegalMoveStacks(x, y, d, numberToMove, maxDistance);
			for (List<Integer> arr : sub) {
				possibleDropCounts.add(Collections.unmodifiableList(arr));
			}
		}
		
		//List<List<Integer>> possibleDropCounts = getLegalMoveStacks(x, y, d, maxToMove, maxDistance);
		return possibleDropCounts.stream()
				.map(Board::integerListToIntArray)
				.map(counts -> new MoveStack(whoseTurn, x, y, d, counts));
	}

	private static int[] integerListToIntArray(List<Integer> l) {
		return l.stream().mapToInt(x -> x).toArray();
	}

	private List<List<Integer>> getLegalMoveStacks(int x, int y, Direction d, int maxToMove, int maxDistance) {
		if (maxDistance == 0 || maxToMove == 0) {
			return Collections.unmodifiableList(Arrays.asList(Collections.emptyList()));
		}
		List<List<Integer>> moveStacks = new ArrayList<>();
		for (int i = 0; i < maxToMove; i++) {
			int numberToMove = i;
			int numberRemaining = maxToMove - numberToMove;
			List<List<Integer>> sub = getLegalMoveStacks(x + d.dx, y + d.dy, d, numberToMove, maxDistance - 1);
			for (List<Integer> arr : sub) {
				List<Integer> temp = new ArrayList<>(arr);
				temp.add(0, numberRemaining);
				moveStacks.add(Collections.unmodifiableList(temp));
			}
		}
		return moveStacks;
	}

	private int getMaxDistance(int x, int y, Direction d) {
		for (int i = 0; i < size + 1; i++) {
			int curX = x + d.dx * i;
			int curY = y + d.dy * i;
			if (!inBounds(curX) || !inBounds(curY)) {
				return i;
			}
		}
		throw new RuntimeException();
	}
}
