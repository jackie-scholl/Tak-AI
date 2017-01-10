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
		this.us = board.whoseTurn;
		return board.getLegalMoves().parallel()
				.map(m -> new MoveScorePair(
						alphabeta(board.makeMove(m), depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY), m))
				.max((x, y) -> Double.compare(x.score, y.score)).get().move;
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
		List<Board> boards = node.getLegalMoves().map(node::makeMove).collect(Collectors.toList());
		if (node.whoseTurn == us) {
			double v = Double.NEGATIVE_INFINITY;
			Collections.sort(boards, (x, y) -> Double.compare(heuristic(y), heuristic(x)));
			for (Board child : boards) {
				double temp = alphabeta(child, depth - 1, alpha, beta);
				v = Math.max(v, temp);
				alpha = Math.max(alpha, v);
				if (beta <= alpha) {
					break;
				}
			}
			return v;
		} else {
			double v = Double.POSITIVE_INFINITY;
			Collections.sort(boards, (x, y) -> Double.compare(heuristic(x), heuristic(y)));
			for (Board child : boards) {
				double temp = alphabeta(child, depth - 1, alpha, beta);
				v = Math.min(v, temp);
				beta = Math.min(beta, v);
				if (beta <= alpha) {
					break;
				}
			}
			return v;
		}
	}

	private double heuristic(Board b) {
		double a1 = b.getNumStones(us) - b.getNumStones(us.other());
		double a2 = b.numStonesOnBoard(us) - b.numStonesOnBoard(us.other());
		return a1 / 100 + a2 / 200;
	}
}
