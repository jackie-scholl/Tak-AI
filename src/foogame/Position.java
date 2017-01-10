package foogame;

import java.util.stream.IntStream;
import java.util.stream.Stream;

class Position {
	final int x, y;

	public Position(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public Position move(Direction dir) {
		return new Position(x+dir.dx, y+dir.dy);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Position other = (Position) obj;
		if (x != other.x) return false;
		if (y != other.y) return false;
		return true;
	}
	
	public static Stream<Position> positionStream(int size) {
		return IntStream.range(0, size)
				.mapToObj(i -> i)
				.flatMap(x -> IntStream.range(0, size)
						.mapToObj(i -> i)
						.map(y -> new Position(x, y)));
	}
}