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
		return Position.positionStream(b.size)
				.filter(p -> Minimaxer.isColor(b, p, col))
				.mapToLong(p -> Arrays
						.stream(Direction.values())
						.map(d -> p.move(d))
						.filter(p2 -> Minimaxer.isColor(b, p2, col))
						.count())
				.sum();
	}

	// closeness of our stones and not walls
	private static double featureClustering2(Board b, Color col) {
		return Position.positionStream(b.size)
				.filter(p -> Minimaxer.isColor(b, p, col) && b.getStack(p).top().type != PieceType.WALL)
				.mapToLong(p -> Arrays
						.stream(Direction.values())
						.map(d -> p.move(d))
						.filter(p2 -> Minimaxer.isColor(b, p2, col) && b.getStack(p).top().type != PieceType.WALL)
						.count())
				.sum();
	}
	
	private static double featureStackiness(Board b, Color col) {
		return Position.positionStream(b.size)
				.filter(p -> Minimaxer.isColor(b, p, col))
				.map(b::getStack)
				.mapToInt(Stack::length)
				.map(x -> x * x)
				.sum();
	}

	// how many capstones we have in our hand
	private static double featureCapstone(Board b, Color col) {
		return b.getNumCapstones(col);
	}
	
	// how many of our pieces are under our capstone and reachable
	private static double featureCapstoneControlSame(Board b, Color col) {
		Optional<Position> pos = Position.positionStream(b.size).filter(p -> b.getStack(p).top().type == PieceType.CAPSTONE).findAny();
		if (!pos.isPresent()) {
			return 0;
		}
		Stone[] stones = b.getStack(pos.get()).getCopy();
		if (stones.length > b.size) {
			stones = Arrays.copyOfRange(stones, stones.length - b.size, stones.length-1);
		}
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

	// how many of their pieces are under our capstone and reachable
	private static double featureCapstoneControlOther(Board b, Color col) {
		Optional<Position> pos = Position.positionStream(b.size).filter(p -> b.getStack(p).top().type == PieceType.CAPSTONE).findAny();
		if (!pos.isPresent()) {
			return 0;
		}
		Stone[] stones = b.getStack(pos.get()).getCopy();
		if (stones.length == 1) {
			return 0;
		}
		if (stones.length > b.size) {
			stones = Arrays.copyOfRange(stones, stones.length - b.size, stones.length-1);
		}
		int c = 0;
		for (int i = 0; i < stones.length - 2; i++) {
			if (stones[i].color != col) {
				c++;
			}
		}
		return c;
	}
	
	// returns 1 if capstone is hard, -1 if soft, else 0
	private static double featureCapstoneHard(Board b, Color c) {
		Optional<Position> pos = Position.positionStream(b.size)
				.filter(p -> b.getStack(p).top().type == PieceType.CAPSTONE)
				.findAny();
		if (pos.isPresent()) {
			Stone[] stones = b.getStack(pos.get()).getCopy();
			if (stones.length > 1) {
				if (stones[stones.length - 1].color == c) {
					return 1;
				}
				return -1;
			}
		}
		return 0;
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
