package foogame;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class TPNInput {

	public static void main(String args[]) throws IOException {
		List<File> filesInFolder = Files.walk(Paths.get("D:\\Workspace\\Tak\\IncomingPTN")).filter(Files::isRegularFile)
				.map(Path::toFile).collect(Collectors.toList());

		for (File f : filesInFolder) {
			System.out.println(f.toString());
			processFile(f);
		}
	}

	private static void processFile(File f) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(f));
		List<String> lines = new ArrayList<String>();
		String line = in.readLine();
		while (line != null) {
			// process out lines and stuff
			if (line.length() != 0 && line.charAt(0) != '[' && line.charAt(0) != ' ') {
				lines.add(line);
			}
			line = in.readLine();
		}
		doMovesAndThings(lines);
		in.close();
	}

	private static void doMovesAndThings(List<String> lines) {
		Board b0 = new Board(5); // there has to be a better way to

		for (String lineFull : lines) {
			Board b = new Board(b0.getBoardArray());
			// removes the "1. " part of the move line
			String turnNumString = lineFull.substring(0, lineFull.indexOf(" "));
			String line = lineFull.substring(lineFull.indexOf(" ") + 1);
			String whiteMove = line.substring(0, line.indexOf(" "));
			String blackMove = line.substring(line.indexOf(" ") + 1);
			if (blackMove.contains("{")) {
				blackMove = blackMove.substring(0,blackMove.indexOf("{")-1);
			}
			int turn = Integer.parseInt(turnNumString.substring(0, turnNumString.indexOf(".")));

			// System.out.println(turnNumString);
			// make the move and update the board
			Move mWhite = simMove(b, whiteMove).get();
			System.out.printf("White Move: %s%n", mWhite.ptn());
			b = BoardMoveImpl.makeMove(b, mWhite);
			//System.out.println(calculateFeatureScores(b, Color.WHITE, turn));

			Move mBlack = simMove(b, blackMove).get();
			System.out.printf("Black Move: %s%n", mBlack.ptn());
			b = BoardMoveImpl.makeMove(b, mBlack);
			//System.out.println(calculateFeatureScores(b, Color.BLACK, turn));
			// Color.WHITE));

		}
	}

	public static String calculateFeatureScores(Board b, Color col, int turn) {
		StringBuilder out = new StringBuilder();
		out.append(turn + " ");
		// the features we measure
		Map<Integer, BiFunction<Board, Color, Double>> h = Heuristics.getFeatureMap();
		double f0 = h.get(0).apply(b, col);
		double f1 = h.get(1).apply(b, col);
		double f2 = h.get(2).apply(b, col);
		double f3 = h.get(3).apply(b, col);
		double f4 = h.get(4).apply(b, col);
		double f5 = h.get(5).apply(b, col);
		double f6 = h.get(6).apply(b, col);
		double f7 = h.get(7).apply(b, col);
		double f8 = h.get(8).apply(b, col);

		out.append(f0 + " ");
		out.append(f1 + " ");
		out.append(f2 + " ");
		out.append(f3 + " ");
		out.append(f4 + " ");
		out.append(f5 + " ");
		out.append(f6 + " ");
		out.append(f7 + " ");
		out.append(f8 + " ");

		return out.toString();
	}

	public static Optional<Move> simMove(Board board, String moveTPN) {
		Optional<Move> m = PTNInput.parse(moveTPN, board.whoseTurn);
		if (!m.isPresent()) {
			return m;
		}
		if (!board.isLegalMove(m.get())) {
			System.err.printf("Illegal move: %s%n", moveTPN);
			return Optional.empty();
		}
		return m;
	}

}
