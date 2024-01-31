package dev.rdh.minesweeper;

import lombok.Getter;

@Getter
public enum Difficulty {
	BEGINNER(9, 9, 10),
	INTERMEDIATE(16, 16, 40),
	EXPERT(30, 16, 99),
	CUSTOM(-1, -1, -1);

	private final int width, height, numMines;

	Difficulty(int width, int height, int numMines) {
		this.width = width;
		this.height = height;
		this.numMines = numMines;
	}

	@Override
	public String toString() {
		return this.name().charAt(0) + this.name().substring(1).toLowerCase();
	}
}
