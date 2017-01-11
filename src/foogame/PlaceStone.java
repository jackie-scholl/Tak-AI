package foogame;

public class PlaceStone extends Move {
	public final int x;
	public final int y;
	public final PieceType type;

	public PlaceStone(Color color, int x, int y, PieceType type) {
		super(color);
		this.x = x;
		this.y = y;
		this.type = type;
	}
	
	private String rowName(int y) {
		return String.valueOf('a' - 1 + (char) y);
	}
	
	private String location(int x, int y) {
		return rowName(y) + x;
	}
	
	public String ptn() {
		return type.notationName + location(x, y);
	}

}
