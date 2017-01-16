package foogame;

public class TPSInput {
	private String tps;
	private String[] grid;
	private int turnIndicator;
	private int turnNumber;
	private boolean print_verification = true;

	public TPSInput(String tps) {
		if (GameInstance.SILENT) {
			print_verification = false;
		}
		this.tps = tps;
	}

	public Board populateBoard() {
		if (verifyTPS(this.tps)) {
			if (print_verification) {
				System.out.println("tps was verified:");
				for (String i : grid) {
					System.out.println(i);
				}
				System.out.println("Turn Indicator: " + turnIndicator);
				System.out.println("Turn Number: " + turnNumber);
			}
			// make the board
			
			
			return new Board(5);
		}
		System.out.println(tps + "failed");
		return new Board(5);
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
		if (!turnNum.matches("\\d")) {
			System.out.println("Bad turn number: " + turnNum);
		} else {
			this.turnNumber = Integer.parseInt(turnNum);
		}

		return true;
	}
}
