package foogame;

public class Stone {
  public final PieceType type;
  public final Color color;

  public Stone(PieceType type, Color color) {
    this.type = type;
    this.color = color;
  }
  
  public String toString() {
	  return String.format("(%s %s)", type.name(), color.name());
  }
}

enum PieceType {
	FLAT("F"), WALL("S"), CAPSTONE("C");
	
	public final String notationName;

	private PieceType(String notationName) {
		this.notationName = notationName;
	}
}

