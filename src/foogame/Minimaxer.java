package foogame;

import java.util.*;
import java.util.stream.Collectors;

/** Alpha-beta minimaxer, using the heuristic to order possible moves to achieve good pruning. */
public class Minimaxer implements Player {
	private final int depth;
	private Color us;

	public Minimaxer(int depth) {
		this.depth = depth;
	}

	public Minimaxer() {
		this(6);
	}

	public void acceptUpdate(GameUpdate update) {}

	public Move getMove(Board board) {
		long start = System.currentTimeMillis();
		
		this.us = board.whoseTurn;
		/*MoveScorePair moveScorePair = board.getLegalMoves().parallel()
				.map(m -> createMoveScorePair(board, m))
				.max((x, y) -> Double.compare(x.score, y.score)).get();*/
		
		List<MoveScorePair> moveScorePairs = board.getLegalMoves().parallel()
				.map(m -> createMoveScorePair(board, m))
				.collect(Collectors.toList());
				//.max((x, y) -> Double.compare(x.score, y.score)).get();
		Collections.shuffle(moveScorePairs);
		
		MoveScorePair moveScorePair = moveScorePairs
				.stream()
				.max((x, y) -> Double.compare(x.score, y.score))
				.get();
		
		Move move = moveScorePair.move;
		double score = moveScorePair.score;
		
		if (!GameInstance.SILENT) {
			System.out.print(score);
			if (score < -0.5) {
				System.out.println("Oh no! They have tinue!");
				//PTNLogger.ptnComment("Oh no! They have tinue!");
			} else if (score > 0.5) {
				System.out.println("Yay! We have tinue!");
			}
			long end = System.currentTimeMillis();
			double difference = (end-start)/1000.0;
			System.out.printf("Time taken: %.1f seconds; score: %.3f%n", difference, score);
		}
			
		return move;
	}
	
	private MoveScorePair createMoveScorePair(Board board, Move m) {
		return new MoveScorePair(
				alphabeta(board.makeMove(m), depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY), m);
	}

	private static class MoveScorePair {
		double score;
		Move move;

		public MoveScorePair(double score, Move move) {
			this.score = score;
			this.move = move;
		}
	}

	private double alphabeta(Board node, int depth, double alpha, double beta) {
		Optional<Color> win = node.hasAnyoneWon();
		if (win.isPresent()) {
			return win.get() == us ? 1 : -1;
		} else if (depth == 0) {
			return heuristic(node);
		}
		//List<Board> boards = node.getLegalMoves().map(node::makeMove).collect(Collectors.toList());
		List<Board> boards = getBoards(node);
		//long seed = System.nanoTime();
		//Collections.shuffle(boards, new Random(seed));
		//Collections.shuffle(boards);
		if (node.whoseTurn == us) {
			double v = Double.NEGATIVE_INFINITY;
			//Collections.sort(boards, (x, y) -> Double.compare(heuristic(y), heuristic(x)));
			for (Board child : boards) {
				double temp = alphabeta(child, depth - 1, alpha, beta);
				v = Math.max(v, temp);
				alpha = Math.max(alpha, v);
				if (beta <= alpha) {
					break;
				}
			}
			return v*0.98;
		} else {
			double v = Double.POSITIVE_INFINITY;
			//Collections.sort(boards, (x, y) -> Double.compare(heuristic(x), heuristic(y)));
			for (Board child : boards) {
				double temp = alphabeta(child, depth - 1, alpha, beta);
				v = Math.min(v, temp);
				beta = Math.min(beta, v);
				if (beta <= alpha) {
					break;
				}
			}
			return v*0.98;
		}
	}
	
	private static List<Board> getBoards(Board node) {
		List<Board> boards = node.getLegalMoves().map(node::makeMove).collect(Collectors.toList());
		Collections.shuffle(boards);
		return boards;
	}

	private double heuristic(Board b) {
		double a1 = 0;
		double a2 = b.numStonesOnBoard(us) - b.numStonesOnBoard(us.other());
		return a1 / 100 + a2 / 200;
	}
}
