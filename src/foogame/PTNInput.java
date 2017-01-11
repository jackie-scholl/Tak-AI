package foogame;

import java.util.Arrays;
import java.util.Optional;

public class PTNInput {
	
	public static Optional<Move> parse(String input, Color color) {
		if (input.isEmpty()) {
			return Optional.empty();
		}
		if (Character.isAlphabetic(input.charAt(0))) {
			// MUST be a PlaceStone
			return parsePlaceStone(input, color);
		} else if (Character.isDigit(input.charAt(0))) {
			return parseMoveStone(input, color);
		}
		System.err.printf("Bad input: %s%n", input);
		return Optional.empty();
	}
	
	private static Optional<Move> parsePlaceStone(String input, Color color) {
		boolean matches = input.matches("[FSC]?([a-z]\\d)");
		if (!matches) {
			//throw new RuntimeException(String.format("Input does not match: %s", input));
			System.err.printf("Input does not match: %s%n", input);
			return Optional.empty();
		}
		int index = 0;
		PieceType type = PieceType.FLAT;
		if (Character.isUpperCase(input.charAt(0))) {
			type = Arrays.stream(PieceType.values()).filter(x -> input.substring(0, 1).equals(x.notationName)).findFirst().get();
			index++;
		}
		int col = parseColumn(input.charAt(index++));
		int row = parseRow(input.charAt(index++));
		return Optional.of(new PlaceStone(color, row, col, type));
	}
	
	private static Optional<Move> parseMoveStone(String input, Color color) {
		boolean matches = input.matches("\\d[a-z]\\d[-+<>]\\d{0,5}");
		if (!matches) {
			//throw new RuntimeException(String.format("Input does not match: %s", input));
			System.err.printf("Input does not match: %s%n", input);
			return Optional.empty();
		}
		int count = parseInt(input.charAt(0));
		int col = parseColumn(input.charAt(1));
		int row = parseRow(input.charAt(2));
		System.out.println(input.substring(3, 4));
		Direction dir = Arrays.stream(Direction.values()).filter(x -> input.substring(3, 4).equals(x.notationName)).findFirst().get();
		int[] counts = new int[]{count};
		if (input.length() > 4) {
			counts = Arrays.stream(input.substring(4).split("")).mapToInt(Integer::parseInt).toArray();
			if (Arrays.stream(counts).reduce(0, Integer::sum) != count) {
				System.err.printf("Count of %i does not match counts %s%n", count, counts);
				return Optional.empty();
			}
		}
		
		return Optional.of(new MoveStack(color, row, col, dir, counts));
	}
	
	private static int parseColumn(char c) {
		return c - 'a';
	}
	
	private static int parseRow(char c) {
		return Integer.parseInt(String.valueOf(c)) - 1;
	}
	
	private static int parseInt(char c) {
		return Integer.parseInt(String.valueOf(c));
	}
	
	

}
