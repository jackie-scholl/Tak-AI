package foogame;

import java.util.Arrays;
import java.util.stream.Collectors;

public class MoveStack extends Move {
	public final int x;
	public final int y;
	public final Direction dir;
	public final int count;
	public final int length;
	public final int[] dropCounts;
	
	public MoveStack(Color color, int x, int y, Direction dir, int... dropCounts) {
		super(color);
		this.x = x;
		this.y = y;
		this.dir = dir;
		this.dropCounts = Arrays.copyOf(dropCounts, dropCounts.length);
		this.length = dropCounts.length;
		this.count = Arrays.stream(dropCounts).reduce(0, Integer::sum);
	}
	
	public boolean checkDropsValid() {
		for (int i=1; i<dropCounts.length; i++) {
			if (dropCounts[i] == 0) {
				return false;
			}
		}
		// Should be able to extend to 6 later (depending on board size)
		if (count > 5) {
			return false;
		}
		if (dropCounts.length > 4) {
			return false;
		}
		return true;
	}
	
	private String rowName(int y) {
		return String.valueOf((char) ('a' + y));
	}
	
	private String location(int x, int y) {
		return rowName(y) + (x + 1);
	}

	public String ptn() {
		StringBuilder buff = new StringBuilder();
		
		buff.append(count);
		buff.append(location(x, y));
		buff.append(dir.notationName);
		buff.append(String.join("", Arrays.stream(dropCounts).mapToObj(String::valueOf).collect(Collectors.toList())));
		
		return buff.toString();
	}
	
}

enum Direction {
	LEFT (0, -1, "<"),
	RIGHT (0, 1, ">"),
	UP (-1, 0, "+"),
	DOWN (1, 0, "-");
	
	public final int dx;
	public final int dy;
	public final String notationName;

	Direction(int deltaX, int deltaY, String notationName) {
		this.dx = deltaX;
		this.dy = deltaY;
		this.notationName = notationName;
	}
}
