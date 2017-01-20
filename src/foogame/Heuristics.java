package foogame;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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

	// how many capstones we have in our hand
	private static double featureCapstone(Board b, Color col) {
		return b.getNumCapstones(col);
	}
	
	// how many of our pieces are under our capstone
	private static double featureCapstoneControlSame(Board b, Color col) {
		Optional<Position> pos = Position.positionStream(b.size).filter(p -> b.getStack(p).top().type == PieceType.CAPSTONE).findAny();
		if (!pos.isPresent()) {
			return 0;
		}
		Stone[] stones = b.getStack(pos.get()).getCopy();
		if (stones.length == 1) {
			return 0;
		}
		int c = 0;
		for (int i = 0; i < stones.length - 2; i++) {
			if (stones[i].color == col) {
				c++;
			}
		}
		return c;
	}

	// how many of their pieces are under our
	private static double featureCapstoneControlOther(Board b, Color col) {
		Optional<Position> pos = Position.positionStream(b.size).filter(p -> b.getStack(p).top().type == PieceType.CAPSTONE).findAny();
		if (!pos.isPresent()) {
			return 0;
		}
		Stone[] stones = b.getStack(pos.get()).getCopy();
		if (stones.length == 1) {
			return 0;
		}
		int c = 0;
		for (int i = 0; i < stones.length - 2; i++) {
			if (stones[i].color != col) {
				c++;
			}
		}
		return c;
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
