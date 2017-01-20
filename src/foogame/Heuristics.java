package foogame;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class Heuristics {
	public static final Map<Integer, BiFunction<Board, Color, Double>> HEURISTIC_MAP = new HashMap<>();

	static {
		HEURISTIC_MAP.put(0, Heuristics::heuristic0);
		HEURISTIC_MAP.put(1, Heuristics::heuristic1);
		HEURISTIC_MAP.put(2, Heuristics::heuristic2);
		// System.out.println("setting heuristic map");
		// System.out.println(HEURISTIC_MAP);
	}

	// flat count
	private static double featureNumStonesOnBoard(Board b, Color col) {
		return b.numStonesOnBoard(col);
	}

	// num stones we've played
	private static double featureNumStones(Board b, Color col) {
		return b.getNumStones(col);
	}

	// closeness of our stones
	private static double featureClustering(Board b, Color col) {
		return Position.positionStream(b.size).filter(p -> Minimaxer.isColor(b, p, col)).mapToLong(p -> Arrays
				.stream(Direction.values()).map(d -> p.move(d)).filter(p2 -> Minimaxer.isColor(b, p2, col)).count())
				.sum();
	}
	
	// closeness of our stones
	private static double featureClustering2(Board b, Color col) {
		return Position.positionStream(b.size).filter(p -> Minimaxer.isColor(b, p, col) && b.getStack(p).top().type != PieceType.WALL)
			.mapToLong(p -> Arrays
					.stream(Direction.values()).map(d -> p.move(d)).filter(p2 -> Minimaxer.isColor(b, p2, col)).count())
			.sum();
	}

	public static double heuristic0(Board b, Color col) {
		double a1 = featureNumStonesOnBoard(b, col);
		return a1 / 200;
	}

	public static double heuristic1(Board b, Color col) {
		double a1 = featureNumStonesOnBoard(b, col);
		double a2 = featureNumStones(b, col);
		return (a1 / 200) + (a2 / -200);
	}

	public static double heuristic2(Board b, Color col) {
		double a1 = featureNumStonesOnBoard(b, col);
		double a2 = featureNumStones(b, col);
		double a3 = featureClustering(b, col);
		return a1 / 200 + a2 / -200 + a3 / 400;
	}
}
