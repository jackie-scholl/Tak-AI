package foogame;

import java.io.BufferedWriter;

public class PTNLogger implements GameObserver{
	private final BufferedWriter writer;
	
	public void acceptUpdate(GameUpdate update) {
		
	}
	
	public PTNLogger(BufferedWriter writer)
	{
		this.writer = writer;
		StringBuilder header = new StringBuilder();
		
	}

}
