package foogame;

public class Stone {
  public final PieceType type;
  public final Color color;

  public Stone(PieceType type, Color color) {
    this.type = type;
    this.color = color;
  }
}

enum PieceType {
	FLAT, WALL;
}

