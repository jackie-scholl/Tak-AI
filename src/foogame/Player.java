package foogame;

public interface Player extends GameObserver {
	public Move getMove(Board board);
}
