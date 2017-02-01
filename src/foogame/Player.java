package foogame;

import java.io.IOException;

public interface Player extends GameObserver {
	public Move getMove(Board board) throws IOException;
}
