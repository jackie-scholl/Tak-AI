package foogame;

public class TPSInput {
	private String tps;
	
	public TPSInput(String tps)
	{
		this.tps = tps;
	}
	
	public Board populateBoard() {
		if (verifyTPS(this.tps)) {
			Stack[][] b;
			System.out.println("tps was verified");
			return new Board(5);
		}
		System.out.println("fuck" + tps);
		return new Board(5);
	}

	private boolean verifyTPS(String str) {
		return str.matches("\\[TPS \"(((((1|2|x)+)S*C*,){4}((1|2|x)+)S*C*/?){1,4}){5} (1|2) \\d\"\\]");
	}

}
