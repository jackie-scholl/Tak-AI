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
		return BoardMoveImpl.isLegalMove(this, m);
	}

	public boolean inBounds(int x) {
		return x >= 0 && x < size;
	}
	
	public Board makeMove(Move m) {
		return BoardMoveImpl.makeMove(this, m);
	}

	public Optional<Color> hasAnyoneWon() {
		Optional<Color> result = WinChecker.winCheck2(this);
		return result;
	}

	public Stack[][] getBoardArray() {
		return deepCopy(boardArray);
	}
	
	public boolean inBounds(Position p) {
		return inBounds(p.x) && inBounds(p.y);
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
	
	public EnumMap<Color, Integer> getNumCapstones() {
		return new EnumMap<Color, Integer>(numCapstones);
	}

	public int getNumCapstones(Color c) {
		return numCapstones.get(c);
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
			List<List<Integer>> sub = getLegalMoveStacks(x, y, d, numberToMove, maxDistance);
			for (List<Integer> arr : sub) {
				possibleDropCounts.add(Collections.unmodifiableList(arr));
			}
		}
		
		return possibleDropCounts.stream()
				//.map(Board::integerListToIntArray)
				.map(counts -> new MoveStack(whoseTurn, x, y, d, counts));
	}

	public static int[] integerListToIntArray(List<Integer> l) {
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
