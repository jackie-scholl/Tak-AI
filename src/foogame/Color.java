package foogame;

public enum Color {
	WHITE, BLACK;
	
	public Color other() {
		return (this == WHITE ? BLACK : WHITE);
	}
}
