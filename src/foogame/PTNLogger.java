package foogame;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PTNLogger implements GameObserver {
	private final BufferedWriter writer;
	private boolean first = true;
	private StringBuilder ptn;

	public PTNLogger(String fileName) throws IOException {
		this(new FileWriter(fileName));
	}

	public PTNLogger(Writer writer) {
		this(new BufferedWriter(writer));
	}

	public PTNLogger(BufferedWriter writer) {
		this.writer = writer;
		this.ptn = new StringBuilder();
	}

	public void acceptUpdate(GameUpdate update) {
		if (!update.board.hasAnyoneWon().isPresent()) {
			ptn.append(writePTN(update.last.ptn(), update.board.turnNumber / 2));
		} else {
			try {
				ptn.append(writePTN(update.last.ptn(), update.board.turnNumber / 2));

				writer.write(headerTag());
				writer.write(resultTag(update));
				writer.write(String.format("[Size \"%d\"]%n", update.board.size));
				writer.write(String.format("%n" + ptn.toString() + "\""));
				writer.flush();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public String headerTag() {
		StringBuilder header = new StringBuilder();
		header.append(String.format("[Event \"Tak_ISP\"]%n"));
		header.append(String.format("[Player1 \"White\"]%n"));
		header.append(String.format("[Player2 \"Black\"]%n"));
		Date curDate = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd");
		header.append(String.format("[Date \"%s\"]%n", format.format(curDate)));
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
			out += (turn+1) + ". " + in;
		} else {
			out += " " + in + "%n";
		}
		first = (first) ? false : true;
		return out;
	}
}
