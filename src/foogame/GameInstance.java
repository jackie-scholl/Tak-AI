package foogame;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GameInstance {
	private final EnumMap<Color, Player> players;
	private final Set<GameObserver> observers;
	private Board board;
	public final static boolean SILENT = false;
	
	public GameInstance(Player redPlayer, Player bluePlayer) {
		this(getEnumMap(redPlayer, bluePlayer));
	}
	
	private static EnumMap<Color, Player> getEnumMap(Player redPlayer, Player bluePlayer) {
		EnumMap<Color, Player> map = new EnumMap<Color, Player>(Color.class);
		map.put(Color.WHITE, redPlayer);
		map.put(Color.BLACK, bluePlayer);
		return map;
	}
	
	public GameInstance(EnumMap<Color, Player> players) {
		this(players, new HashSet<>());
	}
	
	public GameInstance(EnumMap<Color, Player> players, Set<GameObserver> observers) {
		this.players = new EnumMap<Color, Player>(players);
		this.observers = new HashSet<>(observers);
		this.observers.addAll(players.values());
		this.board = new Board(5);
	}
	
	public GameInstance(Player player1, Player player2, Board b) {
		this(getEnumMap(player1, player2), b);
	}
	
	public GameInstance(EnumMap<Color, Player> players, Board b) {
		this(players, new HashSet<>(), b);
	}
	
	public GameInstance(EnumMap<Color, Player> players, Set<GameObserver> observers, Board b) {
		this.players = new EnumMap<Color, Player>(players);
		this.observers = new HashSet<>(observers);
		this.observers.addAll(players.values());
		this.board = b;
	}
	
	public Optional<Color> runSingle() {
		Move m = players.get(board.whoseTurn).getMove(board);
		//Board next = board.makeMove(m);
		Board next = BoardMoveImpl.makeMove(board, m);
		GameUpdate update = new GameUpdate(next, m);
		
		observers.forEach(x -> x.acceptUpdate(update));
		board = next;
		Optional<Color> result = board.hasAnyoneWon();
		if (result.isPresent()) {
			System.out.printf("Game finished! %s won%n", result.get().name());
		}
		return result;
	}
	
	public Color runFull() {
		while (true) {
			Optional<Color> optColor = runSingle();
			if (optColor.isPresent()) {
				return optColor.get();
			}
		}
	}
	
	public void registerObserver(GameObserver s) {
		observers.add(s);
	}
	
	public void deregisterObserver(GameObserver s) {
		observers.remove(s);
	}
	
	public static void main(String... args) throws IOException {
		long start = System.nanoTime();
		//GameInstance game = new GameInstance(new TUIPlayer(), new Minimaxer());
		Player player1 = parsePlayer(args[0]);
		Player player2 = parsePlayer(args[1]);
		GameInstance game = new GameInstance(player1, player2);
		game.registerObserver(new GameLogger("game.out.txt"));
		if (args.length > 2) {
			TPSInput tpsIn = new TPSInput(args[2]);
			Board b = tpsIn.populateBoard();
			game = new GameInstance(player1, player2, b);
		}
		if (!SILENT) {
			game.registerObserver(new GameLogger(new PrintWriter(System.out)));
		}
		game.registerObserver(new PTNLogger("game.out.ptn"));
		game.runFull();
		long end = System.nanoTime();
		System.out.printf("Time: %f seconds; # moves: %d%n", (end-start)/1.0e9, game.board.turnNumber);
	}
	
	private static Player parsePlayer(String s) {
		if (s.equals("T")) {
			return new TUIPlayer();
		} else if (s.startsWith("M")) {
			return parseMinimaxPlayer(s);
		} else {
			throw new RuntimeException("bad argument");
			//return new Artificial.Artifical1();
		}
	}
	
	private static Player parseMinimaxPlayer(String s) {
		Matcher m = Pattern.compile("M(\\d)(H(\\d)?)").matcher(s);
		if (!m.matches()) {
			throw new RuntimeException("Cannot parse minimax player: "+s);
		}
		int depth = Integer.parseInt(m.group(1));
		int heuristicNum;
		if (m.groupCount() > 1) {
			heuristicNum = Integer.parseInt(m.group(3));
		} else {
			heuristicNum = 2;
		}
		BiFunction<Board, Color, Double> heuristic = Heuristics.HEURISTIC_MAP.get(heuristicNum);
		System.out.printf("Using minimaxer with heuristic #%d and depth %d%n", heuristicNum, depth);
		return new Minimaxer(depth, heuristic);
	}
}