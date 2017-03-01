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
		HEURISTIC_MAP.put(8, Heuristics::heuristic8);
		HEURISTIC_MAP.put(9, Heuristics::heuristic8);
		// System.out.println("setting heuristic map");
		// System.out.println(HEURISTIC_MAP);
	}

	static {
		FEATURE_MAP.put(0, Heuristics::featureNumStonesOnBoard);
		FEATURE_MAP.put(1, Heuristics::featureNumStones);
		FEATURE_MAP.put(2, Heuristics::featureClustering);
		FEATURE_MAP.put(3, Heuristics::featureClustering2);
		FEATURE_MAP.put(4, Heuristics::featureStackiness);
		FEATURE_MAP.put(5, Heuristics::featureCapstoneUsThem);
		FEATURE_MAP.put(6, Heuristics::featureCapstoneControlSame);
		FEATURE_MAP.put(7, Heuristics::featureCapstoneControlOther);
		FEATURE_MAP.put(8, Heuristics::featureCapstoneHard);
		FEATURE_MAP.put(9, Heuristics::featureStackiness);
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
	
	private static double featureStackiness2(Board b, Color col) {
		return Math.min(Position.positionStream(b.size)
				.filter(p -> Minimaxer.isColor(b, p, col))
				.map(b::getStack)
				.mapToInt(Stack::length)
				.map(x -> x * x)
				.sum(), 5);
	}

	// how many capstones we have in our hand IF theyve played theirs
	private static double featureCapstoneUsThem(Board b, Color col) {
		return b.getNumCapstones(col.other()) < 1 ? b.getNumCapstones(col) : 0;
	}

	// how many of our pieces are under our capstone and reachable
	private static double featureCapstoneControlSame(Board b, Color col) {
		Optional<Position> pos = Position.positionStream(b.size)
				.filter(p -> !b.getStack(p).isEmpty() && b.getStack(p).top().type == PieceType.CAPSTONE).findAny();
		if (!pos.isPresent()) {
			return 0;
		}
		
		Stack stones = b.getStack(pos.get());
		if (stones.length() == 1) {
			return 0;
		}
		stones = stones.reachableStones(b.size);
		/*if (stones.length > b.size) {
			stones = Arrays.copyOfRange(stones, stones.length - b.size, stones.length - 1);
		}*/
		int c = 0;
		for (int i = 0; i < stones.length() - 2; i++) {
			if (stones.get(i).color == col) {
				c++;
			}
		}
		return c;
		
		/*Stone[] stones = b.getStack(pos.get()).getCopy();
		if (stones.length > b.size) {
			stones = Arrays.copyOfRange(stones, stones.length - b.size, stones.length - 1);
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
		return c;*/
	}

	// how many of their pieces are under our capstone and reachable
	private static double featureCapstoneControlOther(Board b, Color col) {
		Optional<Position> pos = Position.positionStream(b.size)
				.filter(p -> !b.getStack(p).isEmpty() && b.getStack(p).top().type == PieceType.CAPSTONE).findAny();
		if (!pos.isPresent()) {
			return 0;
		}
		//Stone[] stones = b.getStack(pos.get()).getCopy();
		Stack stones = b.getStack(pos.get());
		if (stones.length() == 1) {
			return 0;
		}
		stones = stones.reachableStones(b.size);
		/*if (stones.length > b.size) {
			stones = Arrays.copyOfRange(stones, stones.length - b.size, stones.length - 1);
		}*/
		int c = 0;
		for (int i = 0; i < stones.length() - 2; i++) {
			if (stones.get(i).color != col) {
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
			Stack stones = b.getStack(pos.get());
			if (stones.length() > 1) {
				if (stones.get(stones.length() - 1).color == c) {
					return 1;
				}
				return -1;
			}
			/*Stone[] stones = b.getStack(pos.get()).getCopy();
			if (stones.length > 1) {
				if (stones[stones.length - 1].color == c) {
					return 1;
				}
				return -1;
			}*/
		}
		return 0;
	}
	
	private static double testing() {
		int numStonesOnBoard = 0, numStones = 0, clustering = 0;
		
		return numStonesOnBoard /  200 +
			   numStones        / -200 +
			   clustering       /  400;
		
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
		return (a1 / 200) + (a2 / -400) + (a3 / 300) + (a4 / -400) + (a5 / 400) + (a6 / 400) + (a7 / -400);
	}

	/*
	 * Regression based on like 15 games. Very bad.
	 */
	public static double heuristic4(Board b, Color col) {
		double a0 = 0.117600;
		double a1 = 0.003166 * featureNumStonesOnBoard(b, col);
		double a2 = -0.321358 * featureNumStones(b, col);
		double a3 = -0.018144 * featureClustering(b, col);
		return (a0 + a1 + a2 + a3) / 10;
	}

	public static double heuristic5(Board b, Color col) {
		return 0;
	}

	/*
	 * Problem: score always shows as 0.
	 */

	/*
	 * Regression based on all games with TakticianBot as either white or black. ~300,000 rows, 10 features
	 * Wins as black against H3 60% of the time (ply 2). Ignores 6 games that it sees as containing illegal moves.
	 * All feature values shown as statistically significant.
	 */
	public static double heuristic6(Board b, Color col) {
		Map<Integer, Double> coefficients = new HashMap<>();
		// faked feature #-1 just returns 1 every time
		coefficients.put(-1, -0.1209699);
		coefficients.put(0, 0.1914487);
		coefficients.put(1, -0.0304879);
		coefficients.put(2, 0.0459075);
		coefficients.put(3, -0.0206859);
		coefficients.put(4, 0.0023551);
		coefficients.put(5, -0.1140156);
		coefficients.put(6, 0.0155949);
		coefficients.put(7, 0.0);
		coefficients.put(8, 0.0378559);

		/*
		 * Coefficients: (1 not defined because of singularities)
		      Estimate Std. Error t value Pr(>|t|)    
		(Intercept) -0.1209699  0.0017685 -68.404  < 2e-16 ***
		V3           0.1914487  0.0015499 123.527  < 2e-16 ***
		V4          -0.0304879  0.0010779 -28.285  < 2e-16 ***
		V5           0.0459075  0.0016478  27.859  < 2e-16 ***
		V6          -0.0206859  0.0017997 -11.494  < 2e-16 ***
		V7           0.0023551  0.0001084  21.721  < 2e-16 ***
		V8          -0.1140156  0.0041116 -27.731  < 2e-16 ***
		V9           0.0155949  0.0031462   4.957 7.17e-07 ***
		V10                 NA         NA      NA       NA    
		V11          0.0378559  0.0018343  20.638  < 2e-16 ***
		
		 */

		double result = coefficients.entrySet()
				.stream()
				.map(e -> (e.getKey() == -1 ? 1 : FEATURE_MAP.get(e.getKey()).apply(b, col)) * e.getValue())
				.reduce((x, y) -> x + y).get();

		// System.out.println(result);

		return result / 100;
	}

	/*
	 * Regression based on all games in the database. ~3,300,000 rows, 10 features.
	 * Ignoring 204 games that are seen as containing illegal moves.
	 * Wins as black against H3 80% of the time (ply 2). All coefficients statistically significant,
	 * except #6: featureCapstoneControlSame
	 */
	public static double heuristic7(Board b, Color col) {
		Map<Integer, Double> coefficients = new HashMap<>();
		// faked feature #-1 just returns 1 every time
		coefficients.put(-1, -1.006e-01);
		coefficients.put(0, 1.658e-01);
		coefficients.put(1, -5.184e-02);
		coefficients.put(2, 5.849e-03);
		coefficients.put(3, 5.886e-03);
		coefficients.put(4, 1.081e-03);
		coefficients.put(5, -1.768e-01);
		coefficients.put(6, 1.143e-04);
		// coefficients.put(7, 0.0);
		coefficients.put(8, 6.974e-03);

		/*
		 * Coefficients: (1 not defined because of singularities)
		      Estimate Std. Error  t value Pr(>|t|)    
		(Intercept) -1.006e-01  5.381e-04 -186.865   <2e-16 ***
		V3           1.658e-01  4.406e-04  376.317   <2e-16 ***
		V4          -5.184e-02  2.844e-04 -182.281   <2e-16 ***
		V5           5.849e-03  4.949e-04   11.818   <2e-16 ***
		V6           5.886e-03  5.325e-04   11.054   <2e-16 ***
		V7           1.081e-03  2.458e-05   43.960   <2e-16 ***
		V8          -1.768e-01  1.223e-03 -144.604   <2e-16 ***
		V9           1.143e-04  9.426e-04    0.121    0.903    
		V10                 NA         NA       NA       NA    
		V11          6.974e-03  5.494e-04   12.694   <2e-16 ***
		---
		 ***
		 */

		double result = coefficients.entrySet()
				.stream()
				.map(e -> (e.getKey() == -1 ? 1 : FEATURE_MAP.get(e.getKey()).apply(b, col)) * e.getValue())
				.reduce((x, y) -> x + y).get();

		return result / 100;

	}

	/*
	 * Regression based on all games in the database. ~3,300,000 rows, 4 features.
	 * Ignoring 204 games that are seen as containing illegal moves.
	 * Wins as white 80% of the time against H3 (ply 2).
	 * All coefficients statistically significant, except #6: featureCapstoneControlSame
	 */
	public static double heuristic8(Board b, Color col) {
		Map<Integer, Double> coefficients = new HashMap<>();
		// faked feature #-1 just returns 1 every time
		coefficients.put(-1, -0.09830311);
		coefficients.put(0, 0.1894554); // NumStonesOnBoard
		coefficients.put(1, -0.0480462); // NumStones
		// coefficients.put(2, 5.849e-03);
		// coefficients.put(3, 5.886e-03);
		// coefficients.put(4, 1.081e-03);
		// coefficients.put(5, -1.768e-01);
		coefficients.put(6, -0.2055230); // CapstoneControlSame
		// coefficients.put(7, 0.0);
		coefficients.put(8, 0.0109975); // CapstoneHard

		/*
		 * Coefficients:
		      Estimate Std. Error t value Pr(>|t|)    
		(Intercept) -0.0983031  0.0005345 -183.92   <2e-16 ***
		V3           0.1894554  0.0003131  605.13   <2e-16 ***
		V4          -0.0480462  0.0002522 -190.53   <2e-16 ***
		V8          -0.2055230  0.0011546 -178.01   <2e-16 ***
		V11          0.0109975  0.0005277   20.84   <2e-16 ***
		---
		Signif. codes:  0 ‘***’ 0.001 ‘**’ 0.01 ‘*’ 0.05 ‘.’ 0.1 ‘ ’ 1
		 */

		double result = coefficients.entrySet()
				.stream()
				.map(e -> (e.getKey() == -1 ? 1 : FEATURE_MAP.get(e.getKey()).apply(b, col)) * e.getValue())
				.reduce((x, y) -> x + y).get();

		return result / 100;
	}
	
	/*
	 * H7 as White, H9 as black, with M2, 12 games: 7 / 5
	 * H9 as White, H7 as black, with M2, 14 games: 7 / 7
	 * 
	 * It appears that H9 is pretty evenly matched with H7, and M2H9 v M2H9 games run nearly twice as fast
	 * as M2H7 vs M2H7 games
	 */
	public static double heuristic9(Board b, Color col) {
		Map<Integer, Double> coefficients = new HashMap<>();
		// faked feature #-1 just returns 1 every time
		coefficients.put(-1, -1.006e-01);
		coefficients.put(0, 1.658e-01);
		coefficients.put(1, -5.184e-02);
		coefficients.put(2, 5.849e-03);
		coefficients.put(3, 5.886e-03);
		coefficients.put(9, 1.081e-03); // stackiness2
		coefficients.put(5, -1.768e-01);
		coefficients.put(6, 1.143e-04);
		// coefficients.put(7, 0.0);
		coefficients.put(8, 6.974e-03);

		/*
		 * Coefficients: (1 not defined because of singularities)
		      Estimate Std. Error  t value Pr(>|t|)    
		(Intercept) -1.006e-01  5.381e-04 -186.865   <2e-16 ***
		V3           1.658e-01  4.406e-04  376.317   <2e-16 ***
		V4          -5.184e-02  2.844e-04 -182.281   <2e-16 ***
		V5           5.849e-03  4.949e-04   11.818   <2e-16 ***
		V6           5.886e-03  5.325e-04   11.054   <2e-16 ***
		V7           1.081e-03  2.458e-05   43.960   <2e-16 ***
		V8          -1.768e-01  1.223e-03 -144.604   <2e-16 ***
		V9           1.143e-04  9.426e-04    0.121    0.903    
		V10                 NA         NA       NA       NA    
		V11          6.974e-03  5.494e-04   12.694   <2e-16 ***
		---
		 ***
		 */

		double result = coefficients.entrySet()
				.stream()
				.map(e -> (e.getKey() == -1 ? 1 : FEATURE_MAP.get(e.getKey()).apply(b, col)) * e.getValue())
				.reduce((x, y) -> x + y).get();

		return result / 100;

	}
}
