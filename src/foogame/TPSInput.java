package foogame;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class TPSInput {
	private String tps;
	private String[] grid;
	private int turnIndicator;
	private int turnNumber;
	private int whitePieces;
	private int blackPieces;
	private int whiteCapstones;
	private int blackCapstones;
	private boolean print_verification = true;

	public TPSInput(String tps) {
		if (GameInstance.SILENT) {
			print_verification = false;
		}
		this.tps = tps;
		this.whitePieces = 21; // TODO change for 6x6
		this.blackPieces = 21; // TODO change for 6x6
		this.whiteCapstones = 1;
		this.blackCapstones = 1;
	}

	public Board populateBoard() {
		if (verifyTPS(this.tps)) {
			if (print_verification) {
				System.out.println("tps was verified:" + tps);
				for (String i : grid) {
					System.out.println(i);
				}
				System.out.println("Turn Indicator: " + turnIndicator);
				System.out.println("Turn Number: " + turnNumber);
			}
			Stack[][] out = new Stack[5][5];
			int row = 0;
			for (String i : grid) {
				String[] eachSpace = i.split(",");
				int col = 0;
				for (String space : eachSpace) {
					out[row][col] = buildSpace(space);
					col++;
				}
				row++;
			}
			Color turnColor = turnIndicator == 1 ? Color.WHITE : Color.BLACK;
			EnumMap<Color, Integer> numStones = new EnumMap<Color, Integer>(Color.class);
			numStones.put(Color.WHITE, this.whitePieces);
			numStones.put(Color.BLACK, this.blackPieces);
			EnumMap<Color, Integer> numCapstones = new EnumMap<Color, Integer>(Color.class);
			numCapstones.put(Color.WHITE, this.whiteCapstones);
			numCapstones.put(Color.BLACK, this.blackCapstones);
			return new Board(out, numStones, numCapstones, turnColor, turnNumber);
		}
		System.err.println(tps + "failed");
		return new Board(5);
	}

	private Stack buildSpace(String in) {
		List<Stone> stones = new ArrayList<Stone>();
		char[] chars = in.toCharArray();
		for (char c : chars) {
			if (c == 'X' || c == 'x') {
				break;
			} else if (c == '1') {
				stones.add(new Stone(PieceType.FLAT, Color.WHITE));
				this.whitePieces--;
			} else if (c == '2') {
				stones.add(new Stone(PieceType.FLAT, Color.BLACK));
				this.blackPieces--;
			} else if (c == 'C' || c == 'c') {
				Color color = stones.get(stones.size() - 1).color;
				stones.remove(stones.get(stones.size() - 1));
				stones.add(new Stone(PieceType.CAPSTONE, color));
				if (stones.get(stones.size() - 1).color == Color.WHITE) {
					this.whiteCapstones--;
				} else {
					this.blackCapstones--;
				}
			} else if (c == 'S' || c == 's') {
				Color color = stones.get(stones.size() - 1).color;
				stones.remove(stones.get(stones.size() - 1));
				stones.add(new Stone(PieceType.WALL, color));
			}
		}
		Stone[] stoneArr = stones.toArray(new Stone[stones.size()]);
		return new Stack(stoneArr);
	}

	private boolean verifyTPS(String in) {
		String str = in.replace("\"", "");
		String head = str.substring(0, 5);
		if (!head.equals("[TPS ")) {
			System.out.println("Bad tps starting with: " + head);
			return false;
		}
		str = str.substring(5);
		String grid = str.substring(0, str.indexOf(" "));
		String[] rows = grid.split("/");
		for (String r : rows) {
			// verify each line
			if (!r.matches("(((1|2|x|S|c|C|c)+),){4}(1|2|x|S|s|C|c)+")) {
				System.out.println("Bad tps line at index: " + r);
				return false;
			}
		}
		this.grid = rows;
		str = str.replace(grid, "");
		// System.out.println("String is now:" + str);
		String turnIndicator = str.substring(1, 2);
		if (!turnIndicator.matches("1|2")) {
			System.out.println("Bad turn indicator: " + turnIndicator);
		} else {
			this.turnIndicator = Integer.parseInt(turnIndicator);
		}
		String turnNum = str.substring(3).replace("]", "");
		this.turnNumber = Integer.parseInt(turnNum) * 2;
		if (this.turnIndicator == 2) {
			this.turnNumber += 1;
		}

		return true;
	}
}
