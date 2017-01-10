package foogame;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Optional;
import java.util.stream.Stream;

public class Board {
	private final EnumMap<Color, Integer> numStones;
	private final Optional<Stack>[][] boardArray;
	public final Color whoseTurn;
	public final int size;

	public Board(Optional<Stack>[][] boardArray, EnumMap<Color, Integer> numStones, Color whoseTurn) {
		this.numStones = new EnumMap<Color, Integer>(numStones);
		this.boardArray = deepCopy(boardArray);
		this.whoseTurn = whoseTurn;
		this.size = boardArray.length;
	}

	public Board(Optional<Stack>[][] boardArray) {
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

	private static Optional<Stack>[][] deepCopy(Optional<Stack>[][] original) {
		if (original == null) {
			return null;
		}

		@SuppressWarnings("unchecked") final Optional<Stack>[][] result = new Optional[original.length][];
		for (int i = 0; i < original.length; i++) {
			result[i] = Arrays.copyOf(original[i], original[i].length);
		}
		return result;
	}

	private static final Optional<Stack>[][] emptyBoard(int size) {
		@SuppressWarnings("unchecked") Optional<Stack>[][] boardArray = new Optional[size][size];
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				boardArray[i][j] = Optional.empty();
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
		return inBounds(m) && numStones.get(m.color) != 0 && !boardArray[m.x][m.y].isPresent();
	}

	private boolean isLegalCapture(MoveStack m) {
		return inBounds(m) ;//&& boardArray[m.x][m.y].filter(x -> x.color == m.color).isPresent()
				//&& boardArray[m.x + m.dir.dx][m.y + m.dir.dy].filter(x -> x.color != m.color && x.type == PieceType.STONE).isPresent();
	}

	private boolean inBounds(PlaceStone m) {
		return inBounds(m.x) && inBounds(m.y);
	}

	private boolean inBounds(MoveStack m) {
		return inBounds(m.x) && inBounds(m.y) && inBounds(m.x + m.dir.dx) && inBounds(m.y + m.dir.dy);
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
			return doCapture((MoveStack) m);
		} else {
			throw new RuntimeException();
		}
	}

	private Board doPlaceStone(PlaceStone m) {
		Optional<Stack>[][] array = deepCopy(boardArray);
		array[m.x][m.y] = Optional.of(new Stack(new Stone(m.type, m.color)));
		EnumMap<Color, Integer> numStones = new EnumMap<Color, Integer>(this.numStones);
		numStones.compute(m.color, (k, v) -> v - 1);
		return new Board(array, numStones, m.color.other());
	}

	private Board doCapture(MoveStack m) {
		Optional<Stack>[][] array = deepCopy(boardArray);
		//PieceType oldType = array[m.x][m.y].get().top().type;
		Stack movingStack = array[m.x][m.y].get();
		array[m.x][m.y] = Optional.empty();
		Stack bottomStack = array[m.x+m.dir.dx][m.y+m.dir.dy].get();
		array[m.x + m.dir.dx][m.y + m.dir.dy] = Optional.of(bottomStack.addOnTop(movingStack));
		return new Board(array, this.numStones, m.color.other());
	}

	public Optional<Color> hasAnyoneWon() {
		//Optional<Color> result = WinChecker.winCheck(this);
		Optional<Color> result = WinChecker.winCheck2(this);
		return result;
	}

	public Optional<Stack>[][] getBoardArray() {
		return deepCopy(boardArray);
	}

	public EnumMap<Color, Integer> getNumStones() {
		return new EnumMap<Color, Integer>(numStones);
	}

	public int getNumStones(Color c) {
		return numStones.get(c);
	}

	public Board rotateBoard() {
		@SuppressWarnings("unchecked") Optional<Stack>[][] array = new Optional[size][size];
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
				Optional<Stack> val = boardArray[i][j];
				if (val.map(Stack::top).filter(x -> x.color == c && x.type == PieceType.FLAT).isPresent())
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
