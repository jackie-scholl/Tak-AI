package foogame;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

public class Heuristics {
	public static final Map<Integer, BiFunction<Board, Color, Double>> HEURISTIC_MAP = new HashMap<>();
	public static final Map<Integer, BiFunction<Board, Color, Double>> FEATURE_MAP = new HashMap<>();

	static {
		HEURISTIC_MAP.put(0, Heuristics::heuristic0);
		HEURISTIC_MAP.put(1, Heuristics::heuristic1);
		HEURISTIC_MAP.put(2, Heuristics::heuristic2);
		HEURISTIC_MAP.put(3, Heuristics::heuristic3);
		HEURISTIC_MAP.put(4, Heuristics::heuristic4);
		HEURISTIC_MAP.put(5, Heuristics::heuristic5);
		HEURISTIC_MAP.put(6, Heuristics::heuristic6);
		HEURISTIC_MAP.put(7, Heuristics::heuristic7);
		// System.out.println("setting heuristic map");
		// System.out.println(HEURISTIC_MAP);
	}
	
	static {
		FEATURE_MAP.put(0,  Heuristics::featureNumStonesOnBoard);
		FEATURE_MAP.put(1,  Heuristics::featureNumStones);
		FEATURE_MAP.put(2,  Heuristics::featureClustering);
		FEATURE_MAP.put(3,  Heuristics::featureClustering2);
		FEATURE_MAP.put(4,  Heuristics::featureStackiness);
		FEATURE_MAP.put(5,  Heuristics::featureCapstoneUsThem);
		FEATURE_MAP.put(6,  Heuristics::featureCapstoneControlSame);
		FEATURE_MAP.put(7,  Heuristics::featureCapstoneControlOther);
		FEATURE_MAP.put(8,  Heuristics::featureCapstoneHard);
	}
	
	private Heuristics() {
		// prevent accidental instantiation
	}
	
	public static Map<Integer, BiFunction<Board, Color, Double>> getFeatureMap() {
		return FEATURE_MAP;
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

	// how many capstones we have in our hand IF theyve played theirs
	private static double featureCapstoneUsThem(Board b, Color col) {
		return b.getNumCapstones(col.other()) < 1 ? b.getNumCapstones(col) : 0;
	}
	
	// how many of our pieces are under our capstone and reachable
	private static double featureCapstoneControlSame(Board b, Color col) {
		Optional<Position> pos = Position.positionStream(b.size).filter(p -> !b.getStack(p).isEmpty() && b.getStack(p).top().type == PieceType.CAPSTONE).findAny();
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
		Optional<Position> pos = Position.positionStream(b.size).filter(p -> !b.getStack(p).isEmpty() && b.getStack(p).top().type == PieceType.CAPSTONE).findAny();
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
				.filter(p -> !b.getStack(p).isEmpty() && b.getStack(p).top().type == PieceType.CAPSTONE)
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
	
	public static double heuristic3(Board b, Color col) {
		double a1 = featureNumStonesOnBoard(b, col);
		double a2 = featureNumStones(b, col);
		double a3 = featureClustering(b, col);
		double a4 = featureCapstoneUsThem(b, col);
		double a5 = featureCapstoneHard(b, col);
		double a6 = featureCapstoneControlSame(b, col);
		double a7 = featureCapstoneControlOther(b, col);
		return (a1 / 200) + (a2 / -400) + (a3 / 300) + (a4 / -400) + ( a5 / 400) + (a6 / 400) + (a7 / -400);
	}
	
	/*
	 * Regression based on like 15 games. Very bad.
	 */
	public static double heuristic4(Board b, Color col) {
		double a0 =  0.117600;
		double a1 =  0.003166 * featureNumStonesOnBoard(b, col);
		double a2 = -0.321358 * featureNumStones(b, col);
		double a3 = -0.018144 * featureClustering(b, col);
		return (a0 + a1 + a2 + a3)/10;
	}
	
	public static double heuristic5(Board b, Color col) {
		return 0;
	}
	
	/*
	 * Problem: score always shows as 0.
	 */
	
	/*
	 * Regression based on all games with TakticianBot as either white or black. ~300,000 rows, 10 features
	 * Consistently loses to H3, even when white. Ignores a few games that it sees as containing illegal moves.
	 * All feature values shown as statistically significant.
	 */
	public static double heuristic6(Board b, Color col) {
		Map<Integer, Double> coefficients = new HashMap<>();
		// faked feature #-1 just returns 1 every time
		coefficients.put(-1, -0.1101628);
		coefficients.put(0, -0.0011995);
		coefficients.put(1,  0.1911019);
		coefficients.put(2, -0.0303396);
		coefficients.put(3,  0.0460357);
		coefficients.put(4, -0.0205508);
		coefficients.put(5,  0.0023800);
		coefficients.put(6, -0.1133801);
		coefficients.put(7,  0.0157600);
		coefficients.put(8,  0.0);
		//coefficients.put(9,  0.037850);
		
		double result = coefficients.entrySet()
			.stream()
			.map(e -> 
				(e.getKey() == -1 ? 1 : FEATURE_MAP.get(e.getKey()).apply(b, col) ) * e.getValue())
			.reduce((x, y) -> x + y).get();
		
		//System.out.println(result);
		
		return result / 100;
	}
	
	/*
	 * Regression based on all games in the database. ~3,300,000 rows, 10 features.
	 * Ignoring 204 games that are seen as containing illegal moves.
	 * Consistently loses to H3, even when white. All coefficients statistically significant, except #7: featureCapstoneControlOther
	 */
	public static double heuristic7(Board b, Color col) {
		Map<Integer, Double> coefficients = new HashMap<>();
		// faked feature #-1 just returns 1 every time
		coefficients.put(-1, -1.030e-01);
		coefficients.put(0,  2.375e-04);
		coefficients.put(1,  1.659e-01);
		coefficients.put(2, -5.186e-02);
		coefficients.put(3,  5.818e-03);
		coefficients.put(4,  5.873e-03);
		coefficients.put(5,  1.077e-03);
		coefficients.put(6, -1.769e-01);
		coefficients.put(7,  9.425e-05);
		coefficients.put(8,  0.0);
		//coefficients.put(9,  6.981e-03);
		
/*
 * Coefficients: (1 not defined because of singularities)
              Estimate Std. Error  t value Pr(>|t|)    
(Intercept) -1.030e-01  8.660e-04 -118.933  < 2e-16 ***
V2           2.375e-04  6.621e-05    3.587 0.000334 ***
V3           1.659e-01  4.408e-04  376.257  < 2e-16 ***
V4          -5.186e-02  2.845e-04 -182.316  < 2e-16 ***
V5           5.818e-03  4.950e-04   11.753  < 2e-16 ***
V6           5.873e-03  5.325e-04   11.031  < 2e-16 ***
V7           1.077e-03  2.461e-05   43.742  < 2e-16 ***
V8          -1.769e-01  1.223e-03 -144.645  < 2e-16 ***
V9           9.425e-05  9.426e-04    0.100 0.920353    
V10                 NA         NA       NA       NA    
V11          6.981e-03  5.494e-04   12.706  < 2e-16 ***

 */
		
		double result = coefficients.entrySet()
			.stream()
			.map(e -> 
				(e.getKey() == -1 ? 1 : FEATURE_MAP.get(e.getKey()).apply(b, col) ) * e.getValue())
			.reduce((x, y) -> x + y).get();
		
		return result / 100;
		
	}
}
