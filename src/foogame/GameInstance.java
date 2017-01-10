package foogame;

import java.io.IOException;
import java.util.*;

import foogame.Artificial.Artifical1;

public class GameInstance {
	private final EnumMap<Color, Player> players;
	private final Set<GameObserver> observers;
	private Board board;
	
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
		this.board = new Board(6);
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
		game.runFull();
		long end = System.nanoTime();
		System.out.printf("Time: %f seconds%n", (end-start)/1.0e9);
	}
	
	private static Player parsePlayer(String s) {
		if (s.equals("T")) {
			return new TUIPlayer();
		}
		else if (s.startsWith("M")) {
			int depth = Integer.parseInt(s.substring(1));
			System.out.printf("Using minimaxer with depth %d%n", depth);
			return new Minimaxer(depth);
		}
		else {
			return new Artificial.Artifical1();
		}
	}
}
