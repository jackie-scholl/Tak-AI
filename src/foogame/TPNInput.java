package foogame;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
		Board b = new Board(b0.getBoardArray()); // do this but im not seeing it
		Map <Board, Double> whiteMoveScoreMap = new LinkedHashMap<Board, Double>();
		Map <Board, Double> blackMoveScoreMap = new LinkedHashMap<Board, Double>();
		BiFunction<Board, Color, Double> heuristic = Heuristics.HEURISTIC_MAP.get(3); //change heuristic num here
		System.out.println(lines.size());
		for (String lineFull : lines) {
			// removes the "1. " part of the move line
			String turnNumString = lineFull.substring(0, lineFull.indexOf(" "));
			String line = lineFull.substring(lineFull.indexOf(" ") + 1);
			String whiteMove = line.substring(0, line.indexOf(" "));
			String blackMove = line.substring(line.indexOf(" ") + 1);
			
			System.out.println(turnNumString);
			// make the move and update the board
			Move mWhite = simMove(b, whiteMove).get();
			double whiteScore = heuristic.apply(b, Color.WHITE) - heuristic.apply(b, Color.BLACK);
			System.out.printf("White Move: %s resulted in score %f%n", mWhite.ptn(), whiteScore);
			b = BoardMoveImpl.makeMove(b, mWhite);
			
			Move mBlack = simMove(b, blackMove).get();
			double blackScore = heuristic.apply(b, Color.BLACK) - heuristic.apply(b, Color.WHITE);
			System.out.printf("Black Move: %s resulted in score %f%n", mBlack.ptn(), blackScore);
			b = BoardMoveImpl.makeMove(b, mBlack);
			
			//System.out.printf("WhiteMove Heuristic: %f", heuristic.apply(b, Color.WHITE));

		}
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
