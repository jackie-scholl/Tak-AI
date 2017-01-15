package foogame;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.function.BiFunction;

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
	
	public Optional<Color> runSingle() {
		Move m = players.get(board.whoseTurn).getMove(board);
		Board next = board.makeMove(m);
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
			int heuristicNum = Integer.parseInt(s.substring(1, 2));
			int depth = Integer.parseInt(s.substring(2, 3));
			BiFunction<Board, Color, Double> heuristic = HEURISTIC_MAP.get(heuristicNum);
			System.out.printf("Using minimaxer with heuristic #%d (%s) and depth %d%n", heuristicNum, heuristic, depth);
			return new Minimaxer(depth, heuristic);
		} else if (s.startsWith("N")) {
			int depth = Integer.parseInt(s.substring(1));
			System.out.printf("Using alternate minimaxer with depth %d%n", depth);
			return new Minimaxer2(depth);
		} else {
			throw new RuntimeException("bad argument");
			//return new Artificial.Artifical1();
		}
	}
	
	private static final Map<Integer, BiFunction<Board, Color, Double>> HEURISTIC_MAP = new HashMap<>();
	
	static {
		HEURISTIC_MAP.put(0, Minimaxer::heuristic0);
		HEURISTIC_MAP.put(1, Minimaxer::heuristic1);
		HEURISTIC_MAP.put(2, Minimaxer::heuristic2);
		//System.out.println("setting heuristic map");
		//System.out.println(HEURISTIC_MAP);
	}
}