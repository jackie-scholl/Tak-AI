package foogame;

import java.util.List;

import com.google.common.collect.ImmutableList;


public class Stack {
	//private final Stone[] stones;
	private final ImmutableList<Stone> stones;
	public final static Stack EMPTY = new Stack();

	public Stack(Stone... stones) {
		this.stones = ImmutableList.copyOf(stones);
	}
	
	public Stack(List<Stone> stones) {
		this.stones = ImmutableList.copyOf(stones);
	}
	
	public Stack[] split(int distanceDown) {
		return new Stack[]{new Stack(stones.subList(0, length() - distanceDown)),
				new Stack(stones.subList(length() - distanceDown, length()))};
	}
	
	@Deprecated
	public Stone[] getCopy() {
		return stones.toArray(new Stone[]{});
	}
	
	public Stack addOnTop(Stack other) {
		return new Stack(ImmutableList.<Stone>builder().addAll(this.stones).addAll(other.stones).build());
	}
	
	public Stone top() {
		if (isEmpty()) {
			throw new RuntimeException("Oops! Can't get the top because the stack is empty!");
		}
		return stones.get(length() - 1);
	}
	
	public Stack reachableStones(int boardSize) {
		if (boardSize >= length()) {
			return this;
		}
		return new Stack(stones.subList(length()-boardSize, length()));
	}
	
	public Stone get(int index) {
		return stones.get(index);
	}
	
	public boolean isEmpty() {
		return length() == 0;
	}
	
	public int length() {
		return stones.size();
	}
	
	public String toString() {
		return stones.toString();
	}

}
