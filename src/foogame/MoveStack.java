package foogame;

import java.util.Arrays;

public class MoveStack extends Move {
	public final int x;
	public final int y;
	public final Direction dir;
	public final int count;
	public final int[] dropCounts;
	
	public MoveStack(Color color, int x, int y, Direction dir, int... dropCounts) {
		super(color);
		this.x = x;
		this.y = y;
		this.dir = dir;
		this.dropCounts = Arrays.copyOf(dropCounts, dropCounts.length);
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
	
}

enum Direction {
	LEFT (0, -1),
	RIGHT (0, 1),
	UP (-1, 0),
	DOWN (1, 0);
	
	public final int dx;
	public final int dy;

	Direction(int deltaX, int deltaY) {
		this.dx = deltaX;
		this.dy = deltaY;
	}
}
