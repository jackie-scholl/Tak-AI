package foogame;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class TPNInput {

	public static void main(String args[]) throws IOException {
		//translateDBtoPTN("P A1,P D4,P D3,P B1,M D3 D4 1,P A2,P D3,M B1 A1 1,P E4,M A1 B1 1,M D4 B4 1 1", "R-0");
		
		List<File> filesToProcess = Arrays.stream(args).map(Paths::get).filter(Files::isRegularFile).map(Path::toFile)
				.collect(Collectors.toList());
		List<String> lines = filesToProcess.stream().map(TPNInput::processFile).flatMap(List::stream)
				.collect(Collectors.toList());
		
		writeOut(lines);
		
		/*BufferedWriter bfOut = new BufferedWriter(new FileWriter("FeatureScoring.ssv"));
		for (File f : filesToProcess) {
			System.out.println(f.toString());
			List<String> lines = processFile(f);
			for (String line : lines) {
				bfOut.append(line);
				bfOut.newLine();
			}
		}
		bfOut.flush();
		bfOut.close();*/
	}

	public static int writeOut(List<String> lines) throws IOException {
		BufferedWriter bfOut = new BufferedWriter(new FileWriter("FeatureScoring.ssv"));
		for (String line : lines) {
			bfOut.append(line);
			bfOut.newLine();
		}
		bfOut.flush();
		bfOut.close();
		
		return lines.size();
	}

	public static List<String> processFile(File f) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(f));
			List<String> lines = in.lines().collect(Collectors.toList());
			in.close();
			return processPTN(lines);
		} catch (IOException e) {
			e.printStackTrace();
			return Collections.emptyList();
		}
	}

	public static List<String> processPTN(Iterable<String> in) {
		List<String> lines = new ArrayList<String>();
		Color winner = Color.WHITE;
		for (String line : in) {
			// process out lines and stuff

			if (line.contains("Result")) {
				if (!line.contains("0") || line.contains("1")) {
					return lines;
				}
				String res = line.substring(line.indexOf("\"") + 1, line.indexOf("\"") + 4);
				if (res.indexOf("0") == 0) {
					winner = Color.BLACK;
				}
			}
			if (line.length() != 0 && line.charAt(0) != '[' && line.charAt(0) != ' ') {
				lines.add(line);
			}
		}

		return doMovesAndThings(lines, winner);

	}

	private static List<String> doMovesAndThings(List<String> lines, Color winner) {
		Board b0 = new Board(5); // there has to be a better way to
		Board b = new Board(b0.getBoardArray());
		List<String> out = new ArrayList<String>();

		for (String lineFull : lines) {
			// removes the "1. " part of the move line
			String turnNumString = lineFull.substring(0, lineFull.indexOf(" "));
			String line = lineFull.substring(lineFull.indexOf(" ") + 1);
			//System.out.println(line);
			String whiteMove;
			if (line.indexOf(" ") == -1 && line.length() > 0) {
				whiteMove = line;
			} else {
				whiteMove = line.substring(0, line.indexOf(" "));
			}
			line = line.replace(whiteMove, "");
			String blackMove = line.substring(line.indexOf(" ") + 1);
			if (blackMove.contains("{")) {
				blackMove = blackMove.substring(0, blackMove.indexOf("{") - 1);
			}
			int turn = Integer.parseInt(turnNumString.substring(0, turnNumString.indexOf(".")));
			String whiteWin = winner == Color.WHITE ? "1 " : "-1 ";
			String blackWin = winner == Color.BLACK ? "1 " : "-1 ";
			if (turn == 1) {
				String temp = blackMove;
				blackMove = whiteMove;
				whiteMove = temp;
			}
			// make the move and update the board
			Move mWhite = simMove(b, whiteMove).get();
			// System.out.printf("White Move: %s%n", mWhite.ptn());
			b = BoardMoveImpl.makeMove(b, mWhite);
			// System.out.println(whiteWin + calculateFeatureScores(b,
			// Color.WHITE, turn));
			out.add(whiteWin + calculateFeatureScores(b, Color.WHITE, turn));

			if (blackMove.length() > 1) {
				Move mBlack = simMove(b, blackMove).get();
				// System.out.printf("Black Move: %s%n", mBlack.ptn());
				b = BoardMoveImpl.makeMove(b, mBlack);
				// System.out.println(blackWin + calculateFeatureScores(b,
				// Color.BLACK, turn));
				out.add(blackWin + calculateFeatureScores(b, Color.BLACK, turn));
			}
		}
		return out;
	}

	public static String calculateFeatureScores(Board b, Color col, int turn) {
		StringBuilder out = new StringBuilder();
		out.append(turn + " ");
		// the features we measure
		Map<Integer, BiFunction<Board, Color, Double>> h = Heuristics.getFeatureMap();

		int[] featuresWeWant = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		for (int i : featuresWeWant) {
			double f = h.get(i).apply(b, col) - h.get(i).apply(b, col.other());
			out.append(f + " ");
		}

		return out.toString();
	}

	public static List<String> translateDBtoPTN(String dbString, String result) {
		List<String> lines = new ArrayList<String>();
		lines.add(String.format("[Result \"%s\"]", result));

		dbString = dbString.replace(" ", "");
		String[] moves = dbString.split(",");
		
		if (moves.length == 1) {
			return Collections.emptyList();
		}

		for (String move : moves) {
			String ptnMove = "";
			if (move.charAt(0) == 'P') {
				if (move.length() == 3) {
					// place a flat
					ptnMove += "F" + move.substring(1).toLowerCase();
				} else {
					// place a wall or capstone
					if (move.charAt(3) == 'C') {
						ptnMove += "C" + move.substring(1, 3).toLowerCase();
					} else {
						ptnMove += "S" + move.substring(1, 3).toLowerCase();
					}
				}
				lines.add(ptnMove);
			} else {
				// move
				String initial = move.substring(1, 3);
				String target = move.substring(3, 5);
				char ptnDir;
				if (initial.charAt(0) == target.charAt(0)) {
					// same column
					ptnDir = (initial.charAt(1) < target.charAt(1)) ? '+' : '-';
				} else {
					// same rank
					ptnDir = (initial.charAt(0) < target.charAt(0)) ? '>' : '<';
				}
				String drops = move.substring(5);
				int dropSum = 0;
				for (char c : drops.toCharArray()) {
					dropSum += Character.getNumericValue(c);
				}
				lines.add(dropSum + initial.toLowerCase() + ptnDir + drops);

			}
			// System.out.println("dbString: " + move + " ,ptn: " + ptnMove);
		}
		/*for (String line : lines) {
			System.out.println(line);
		}*/
		List<String> lines2 = new ArrayList<>();
		for (int i=0; i<lines.size(); i++) {
			String line = lines.get(i);
			if (i == 0) {
				lines2.add(line);
			} else if (i % 2 == 1) {
				lines2.add(String.format("%d. %s", (i+1)/2, line));
			} else {
				String old = lines2.get(lines2.size()-1);
				String addition = String.format(" %s", line);
				String newString = old + addition;
				lines2.set(lines2.size()-1, newString);
			}
		}
		return lines2;
	}

	public static Optional<Move> simMove(Board board, String moveTPN) {
		Optional<Move> m = PTNInput.parse(moveTPN, board.whoseTurn);
		if (!m.isPresent()) {
			return m;
		}
		if (!board.isLegalMove(m.get())) {
			System.err.printf("Illegal move: %s%n", moveTPN);
			System.err.printf("Does the system see this as a legal move: %b%n", board.isLegalMove(m.get()));
			//System.err.printf("Board state: %s%n", GameLogger.stringifyBoard(board));
			return Optional.empty();
		}
		return m;
	}

}
