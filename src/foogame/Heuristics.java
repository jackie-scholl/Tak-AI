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
		HEURISTIC_MAP.put(0,  Heuristics::heuristic0);
		HEURISTIC_MAP.put(1,  Heuristics::heuristic1);
		HEURISTIC_MAP.put(2,  Heuristics::heuristic2);
		HEURISTIC_MAP.put(3,  Heuristics::heuristic3);
		HEURISTIC_MAP.put(4,  Heuristics::heuristic4);
		HEURISTIC_MAP.put(5,  Heuristics::heuristic5);
		HEURISTIC_MAP.put(6,  Heuristics::heuristic6);
		HEURISTIC_MAP.put(7,  Heuristics::heuristic7);
		HEURISTIC_MAP.put(8,  Heuristics::heuristic8);
		HEURISTIC_MAP.put(9,  Heuristics::heuristic9);
		HEURISTIC_MAP.put(10, Heuristics::heuristic10);
		HEURISTIC_MAP.put(11, Heuristics::heuristic11);
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
		FEATURE_MAP.put(9, Heuristics::featureStackiness2);
		FEATURE_MAP.put(10, Heuristics::featureCenterAffinity);
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
				.sum(), 25);
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
		int c = 0;
		for (int i = 0; i < stones.length() - 2; i++) {
			if (stones.get(i).color == col) {
				c++;
			}
		}
		return c;
	}

	// how many of their pieces are under our capstone and reachable
	private static double featureCapstoneControlOther(Board b, Color col) {
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
		}
		return 0;
	}
	
	// returns average affinity to center of the board
	// 1 2 3 2 1
	// 2 3 4 3 2
	// 3 4 5 4 3
	// 2 3 4 3 2
	// 1 2 3 2 1
	// does not consider pieces not on top
	private static double featureCenterAffinity(Board b, Color col)  {
		double sum = 0;
		int topCount = 0;
		for (int i = 0; i < 5; i++){
			for (int j = 0; i < 5; j++) {
				Stack s = b.getBoardArray()[i][j];
				if (s.top().color == col) {
					sum += 5 - (Math.abs(3-(i+1)) + Math.abs(3-(j+1)));
					topCount ++;
				}
			}
		}
		return sum/topCount;
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
		
		/*
		 * Coefficients: (1 not defined because of singularities)
              Estimate Std. Error  t value Pr(>|t|)    
		(Intercept) -1.006e-01  5.381e-04 -186.875  < 2e-16 ***
		V3           1.655e-01  4.407e-04  375.592  < 2e-16 ***
		V4          -5.217e-02  2.846e-04 -183.323  < 2e-16 ***
		V5           5.691e-03  4.950e-04   11.497  < 2e-16 ***
		V6           6.051e-03  5.326e-04   11.362  < 2e-16 ***
		V7           1.090e-03  2.456e-05   44.371  < 2e-16 ***
		V8          -1.770e-01  1.223e-03 -144.750  < 2e-16 ***
		V9          -3.484e-03  8.635e-04   -4.034 5.47e-05 ***
		V10                 NA         NA       NA       NA    
		V11          6.415e-03  5.506e-04   11.652  < 2e-16 ***
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
	
	public static double heuristic10(Board b, Color col) {
		Map<Integer, Double> coefficients = new HashMap<>();
		// faked feature #-1 just returns 1 every time
		coefficients.put(-1, -1.032e-01);
		coefficients.put(0,  1.641e-01);
		coefficients.put(1, -5.447e-02);
		coefficients.put(2,  5.513e-03);
		coefficients.put(3,  6.031e-03);
		coefficients.put(5, -1.802e-01);
		coefficients.put(6, -6.904e-03);
		// coefficients.put(7, 0.0);
		coefficients.put(8,  3.911e-03);
		coefficients.put(9,  3.859e-03);

		/*
		Coefficients: (1 not defined because of singularities)
		              Estimate Std. Error  t value Pr(>|t|)    
		(Intercept) -1.032e-01  5.427e-04 -190.197  < 2e-16 ***
		V3           1.641e-01  4.429e-04  370.562  < 2e-16 ***
		V4          -5.447e-02  2.951e-04 -184.584  < 2e-16 ***
		V5           5.513e-03  4.946e-04   11.148  < 2e-16 ***
		V6           6.031e-03  5.319e-04   11.338  < 2e-16 ***
		V8          -1.802e-01  1.224e-03 -147.220  < 2e-16 ***
		V9          -6.904e-03  8.695e-04   -7.941 2.01e-15 ***
		V10                 NA         NA       NA       NA    
		V11          3.911e-03  5.567e-04    7.026 2.12e-12 ***
		V12          3.859e-03  7.249e-05   53.234  < 2e-16 ***
		---
		Signif. codes:  0 ‘***’ 0.001 ‘**’ 0.01 ‘*’ 0.05 ‘.’ 0.1 ‘ ’ 1
		
		Residual standard error: 0.9313 on 3372819 degrees of freedom
		Multiple R-squared:  0.1324,	Adjusted R-squared:  0.1324 
		F-statistic: 6.435e+04 on 8 and 3372819 DF,  p-value: < 2.2e-16
		 */

		double result = coefficients.entrySet()
				.stream()
				.map(e -> (e.getKey() == -1 ? 1 : FEATURE_MAP.get(e.getKey()).apply(b, col)) * e.getValue())
				.reduce((x, y) -> x + y).get();

		return result / 100;

	}
	
	public static double heuristic11(Board b, Color col) {
	
		double v3  = FEATURE_MAP.get(0).apply(b, col);
		double v4  = FEATURE_MAP.get(1).apply(b, col);
		double v5  = FEATURE_MAP.get(2).apply(b, col);
		double v6  = FEATURE_MAP.get(3).apply(b, col);
		double v8  = FEATURE_MAP.get(5).apply(b, col);
		double v9  = FEATURE_MAP.get(6).apply(b, col);
		double v10 = Double.NaN;
		double v11 = FEATURE_MAP.get(8).apply(b, col);
		double v12 = FEATURE_MAP.get(9).apply(b, col);
		
		return  v3  *  1.642e-01 +
				v4  * -5.480e-02 +
				v5  *  5.252e-03 +
				v6  *  6.287e-03 +
				v8  * -1.815e-01 +
				v9  * -7.043e-03 +
				v11 *  3.632e-03 +
				v12 *  3.741e-03 +
				
				v3 * (  v4  * -1.377e-03 +
						v5  * -8.139e-04 +	
						v8  * -4.701e-03 +	
						v12 *  2.741e-04
						) +
				
				v4 * (  v6  *  9.670e-04 +
						v8  * -2.554e-03 +	
						v8  * -4.701e-03
						) +
				
				v5 * (  v6  *  2.385e-04 +
						v11 * -7.796e-04 +
						v12 *  2.711e-04
						) +
				
				v6 * (  v9  *  1.459e-03 +
						v12 * -2.106e-04
						) +
				
				v8 * (  v11 * -5.596e-03 +
						v12 *  2.236e-03
						) +
				
				v9 * (  v10 * -1.326e-03
						) +
				
				v11* (  v12 *  3.617e-04
						);
		
		/*
		Call:
		lm(formula = V1 ~ (V3 + V4 + V5 + V6 + V8 + V9 + V10 + V11 + 
		    V12) * (V3 + V4 + V5 + V6 + V8 + V9 + V10 + V11 + V12), data = read.table("Documents/Workspace/ObjectOrientedDesignAssignments/FeatureScoring.ssv"))
		
		Residuals:
		    Min      1Q  Median      3Q     Max 
		-3.4200 -0.8928  0.0751  0.8829  3.6274 
		
		Coefficients: (8 not defined because of singularities)
		              Estimate Std. Error  t value Pr(>|t|)    
		(Intercept) -1.072e-01  6.700e-04 -160.072  < 2e-16 ***
		V3           1.642e-01  4.562e-04  359.843  < 2e-16 ***
		V4          -5.480e-02  3.046e-04 -179.947  < 2e-16 ***
		V5           5.252e-03  5.035e-04   10.433  < 2e-16 ***
		V6           6.287e-03  5.431e-04   11.575  < 2e-16 ***
		V8          -1.815e-01  1.284e-03 -141.356  < 2e-16 ***
		V9          -7.043e-03  8.969e-04   -7.853 4.06e-15 ***
		V10                 NA         NA       NA       NA    
		V11          3.632e-03  5.761e-04    6.304 2.90e-10 ***
		V12          3.741e-03  7.565e-05   49.453  < 2e-16 ***
		V3:V4       -1.377e-03  1.710e-04   -8.054 8.02e-16 ***
		V3:V5       -8.139e-04  1.635e-04   -4.979 6.39e-07 ***
		V3:V6       -2.783e-05  1.475e-04   -0.189 0.850353    
		V3:V8       -4.701e-03  8.169e-04   -5.754 8.71e-09 ***
		V3:V9       -1.021e-03  5.611e-04   -1.820 0.068716 .  
		V3:V10              NA         NA       NA       NA    
		V3:V11      -7.178e-04  3.769e-04   -1.905 0.056810 .  
		V3:V12       2.741e-04  4.939e-05    5.550 2.86e-08 ***
		V4:V5       -2.752e-04  1.773e-04   -1.552 0.120580    
		V4:V6        9.670e-04  2.027e-04    4.771 1.83e-06 ***
		V4:V8       -2.554e-03  6.759e-04   -3.778 0.000158 ***
		V4:V9        2.377e-04  3.121e-04    0.762 0.446321    
		V4:V10              NA         NA       NA       NA    
		V4:V11      -2.523e-04  2.323e-04   -1.086 0.277294    
		V4:V12       2.812e-05  2.466e-05    1.140 0.254267    
		V5:V6        2.385e-04  2.432e-05    9.810  < 2e-16 ***
		V5:V8        8.925e-04  1.317e-03    0.677 0.498119    
		V5:V9       -8.867e-04  5.623e-04   -1.577 0.114797    
		V5:V10              NA         NA       NA       NA    
		V5:V11      -7.796e-04  3.497e-04   -2.229 0.025813 *  
		V5:V12       2.711e-04  6.939e-05    3.907 9.34e-05 ***
		V6:V8       -1.756e-03  1.339e-03   -1.312 0.189632    
		V6:V9        1.459e-03  6.490e-04    2.248 0.024577 *  
		V6:V10              NA         NA       NA       NA    
		V6:V11       7.646e-04  4.025e-04    1.900 0.057477 .  
		V6:V12      -2.106e-04  7.392e-05   -2.849 0.004390 ** 
		V8:V9       -1.815e-03  2.213e-03   -0.820 0.412014    
		V8:V10              NA         NA       NA       NA    
		V8:V11      -5.596e-03  1.210e-03   -4.626 3.72e-06 ***
		V8:V12       2.236e-03  1.543e-04   14.491  < 2e-16 ***
		V9:V10      -1.326e-03  5.372e-04   -2.468 0.013573 *  
		V9:V11       1.704e-04  6.685e-04    0.255 0.798787    
		V9:V12      -4.573e-05  1.239e-04   -0.369 0.712133    
		V10:V11             NA         NA       NA       NA    
		V10:V12             NA         NA       NA       NA    
		V11:V12      3.617e-04  6.788e-05    5.329 9.88e-08 ***
		---
		Signif. codes:  0 ‘***’ 0.001 ‘**’ 0.01 ‘*’ 0.05 ‘.’ 0.1 ‘ ’ 1
		
		Residual standard error: 0.9312 on 3372790 degrees of freedom
		Multiple R-squared:  0.1326,	Adjusted R-squared:  0.1326 
		F-statistic: 1.394e+04 on 37 and 3372790 DF,  p-value: < 2.2e-16

		 */
		
	}
}
