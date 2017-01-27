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
		List<File> filesToProcess = Arrays.stream(args).map(Paths::get).filter(Files::isRegularFile).map(Path::toFile)
				.collect(Collectors.toList());
		// List<File> filesInFolder =
		// Files.walk(Paths.get("D:\\Workspace\\Tak\\IncomingPTN")).filter(Files::isRegularFile)
		// .map(Path::toFile).collect(Collectors.toList());
		BufferedWriter bfOut = new BufferedWriter(new FileWriter("FeatureScoring.ssv"));
		for (File f : filesToProcess) {
			System.out.println(f.toString());
			List<String> lines = processFile(f);
			for (String line : lines) {
				bfOut.append(line);
				bfOut.newLine();
			}
		}
		bfOut.flush();
		bfOut.close();
	}

	private static List<String> processFile(File f) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(f));
		List<String> lines = new ArrayList<String>();
		String line = in.readLine();
		Color winner = Color.WHITE;
		while (line != null) {
			// process out lines and stuff

			if (line.contains("Result")) {
				String res = line.substring(line.indexOf("\"") + 1, line.indexOf("\"") + 4);
				if (res.indexOf("0") == 0) {
					winner = Color.BLACK;
				}
			}
			if (line.length() != 0 && line.charAt(0) != '[' && line.charAt(0) != ' ') {
				lines.add(line);
			}
			line = in.readLine();
		}
		in.close();
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
			// System.out.println(line);
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
			//System.out.println(whiteWin + calculateFeatureScores(b, Color.WHITE, turn));
			out.add(whiteWin + calculateFeatureScores(b, Color.WHITE, turn));

			if (blackMove.length() > 1) {
				Move mBlack = simMove(b, blackMove).get();
				// System.out.printf("Black Move: %s%n", mBlack.ptn());
				b = BoardMoveImpl.makeMove(b, mBlack);
				//System.out.println(blackWin + calculateFeatureScores(b, Color.BLACK, turn));
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

		int[] featuresWeWant = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8 };
		for (int i : featuresWeWant) {
			double f = h.get(i).apply(b, col) - h.get(i).apply(b, col.other());
			out.append(f + " ");
		}

		return out.toString();
	}

	public static Optional<Move> simMove(Board board, String moveTPN) {
		Optional<Move> m = PTNInput.parse(moveTPN, board.whoseTurn);
		if (!m.isPresent()) {
			return m;
		}
		if (!board.isLegalMove(m.get())) {
			System.err.printf("Illegal move: %s%n", moveTPN);
			System.err.printf("Does the system see this as a legal move: %b%n", board.isLegalMove(m.get()));
			return Optional.empty();
		}
		return m;
	}

}
