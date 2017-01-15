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
		return new Board(5);
	}

	private boolean verifyTPS(String str) {
		return str.matches("TPS");
	}

}
