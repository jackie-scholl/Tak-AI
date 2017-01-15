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
		//System.out.println("setting heuristic map");
		//System.out.println(HEURISTIC_MAP);
	}

	public static double heuristic0(Board b, Color us) {
		// flat count
		double a1 = b.numStonesOnBoard(us) - b.numStonesOnBoard(us.other());
		return a1 / 200;
	}

	public static double heuristic1(Board b, Color us) {
		// flat count
		double a1 = b.numStonesOnBoard(us) - b.numStonesOnBoard(us.other());
		// num stones we've played vs them
		double a2 = b.getNumStones(us.other()) - b.getNumStones(us);
		return (a1 / 200) + (a2 / 200);
	}

	public static double heuristic2(Board b, Color us) {
		double c1 = Position.positionStream(b.size)
				.filter(p -> Minimaxer.isColor(b, p, us))
				.mapToLong(p -> Arrays.stream(Direction.values())
						.map(d -> p.move(d))
						.filter(p2 -> Minimaxer.isColor(b, p2, us))
						.count())
				.sum();
		
		double c2 = Position.positionStream(b.size)
				.filter(p -> Minimaxer.isColor(b, p, us.other()))
				.mapToLong(p -> Arrays.stream(Direction.values())
						.map(d -> p.move(d))
						.filter(p2 -> Minimaxer.isColor(b, p2, us.other()))
						.count())
				.sum();
		
		double c = c1 - c2;
	
		// flat count
		double a1 = b.numStonesOnBoard(us) - b.numStonesOnBoard(us.other());
		// num stones we've played vs them
		double a2 = b.getNumStones(us.other()) - b.getNumStones(us);
		return (a1 / 200) + (a2 / 200) + (c/400);
	}
}
