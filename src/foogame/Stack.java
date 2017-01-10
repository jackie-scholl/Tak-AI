package foogame;

import java.util.Arrays;

public class Stack {
	private final Stone[] stones;

	public Stack(Stone... stones) {
		if (stones.length == 0) {
			throw new RuntimeException("stacks cannot be empty");
		}
		this.stones = stones;
	}
	
	public Stack[] split(int distanceDown) {
		if (distanceDown == 0 || distanceDown == stones.length) {
			throw new RuntimeException("bad split size");
		}
		return new Stack[]{new Stack(Arrays.copyOfRange(stones, 0, distanceDown)), new Stack(Arrays.copyOfRange(stones, distanceDown, stones.length))};
	}
	
	public Stone[] getCopy() {
		return Arrays.copyOf(stones, stones.length);
	}
	
	public Stack addOnTop(Stack other) {
		Stone[] newStones = Arrays.copyOf(stones, stones.length + other.stones.length);
		for (int i = 0; i < other.stones.length; i++) {
			newStones[i + stones.length] = other.stones[i];
		}
		return new Stack(newStones);
	}
	
	// hehe
	public Stone top() {
		return stones[stones.length-1];
	}

}
