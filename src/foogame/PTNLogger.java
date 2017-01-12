package foogame;

import java.io.BufferedWriter;
import java.io.IOException;

public class PTNLogger implements GameObserver {
	private final BufferedWriter writer;
	private boolean first = false;
	private StringBuilder tps;

	public void acceptUpdate(GameUpdate update) {
		if (!update.board.hasAnyoneWon().isPresent()) {
			tps.append(writePTN(update.last.ptn(), update.board.turnNumber / 2));
		} else {
			try {
				tps.append(writePTN(update.last.ptn(), update.board.turnNumber / 2));
				
				writer.write(headerTag());
				writer.write(winnerTag(update));
				writer.write(String.format("[Size \"%d\"]", update.board.size));
				writer.write(tps.toString());
				writer.flush();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public String headerTag() {
		StringBuilder header = new StringBuilder();
		header.append("[Event \"Tak_ISP\"]\n");
		header.append("[Player1 \"White\"]\n");
		header.append("[Player2 \"Black\"]\n");
		header.append(String.format("[Time \"%l\"]\n", System.currentTimeMillis()));
		return header.toString();
	}

	
	public String winnerTag(GameUpdate update)
	{
		if (update.board.hasAnyoneWon().get() == Color.WHITE){
			return "[Result \"F-0\"]\n";
		}
		return "[Result \"0-F\"]\n";
	}

	public String writePTN(String in, int turn) {
		String out = "";
		if (first) {
			out += turn + ". " + in;
		}
		out += " " + in + "\n";
		first = (first)? false : true;
		return out;
	}

	public PTNLogger(BufferedWriter writer) {
		this.writer = writer;
	}

}