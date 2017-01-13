package foogame;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class PTNLogger implements GameObserver {
	private final BufferedWriter writer;
	private boolean first = false;
	private StringBuilder tps;

	public PTNLogger(String fileName) throws IOException {
		this(new FileWriter(fileName));
	}

	public PTNLogger(Writer writer) {
		this(new BufferedWriter(writer));
	}

	public PTNLogger(BufferedWriter writer) {
		this.writer = writer;
		this.tps = new StringBuilder();
	}

	public void acceptUpdate(GameUpdate update) {
		if (update.board.turnNumber != 1) {
			if (!update.board.hasAnyoneWon().isPresent()) {
				tps.append(writePTN(update.last.ptn(), update.board.turnNumber / 2));
			} else {
				try {
					tps.append(writePTN(update.last.ptn(), update.board.turnNumber / 2));

					writer.write(headerTag());
					writer.write(resultTag(update));
					writer.flush();
					writer.write(String.format("[Size N\"%d\"]%n", update.board.size));
					writer.write(tps.toString());
					writer.flush();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	public String headerTag() {
		StringBuilder header = new StringBuilder();
		header.append(String.format("[Event \"Tak_ISP\"]%n"));
		header.append(String.format("[Player1 \"White\"]%n"));
		header.append(String.format("[Player2 \"Black\"]%n"));
		header.append(String.format("[Time \"%d\"]%n", System.currentTimeMillis()));
		return header.toString();
	}

	public String resultTag(GameUpdate update) {
		if (update.board.hasAnyoneWon().get() == Color.WHITE) {
			return String.format("[Result \"F-0\"]%n");
		}
		return String.format("[Result \"0-F\"]%n");
	}

	public String writePTN(String in, int turn) {
		String out = "";
		if (first) {
			out += turn + ". " + in;
		} else {
			out += " " + in + "%n";
		}
		first = (first) ? false : true;
		return out;
	}
}
