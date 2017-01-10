package foogame;

public interface GameObserver {
	public void acceptUpdate(GameUpdate update);
}

class GameUpdate {
	public final Board board;
	public final Move last;
	
	public GameUpdate(Board board, Move last) {
		this.board = board;
		this.last = last;
	}
}
