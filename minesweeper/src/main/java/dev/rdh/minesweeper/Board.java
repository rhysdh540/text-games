package dev.rdh.minesweeper;

import lombok.Getter;

import java.util.concurrent.ThreadLocalRandom;

public class Board {
	private int x, y;

	@Getter
	private final int width, height;

	private final Cell[][] cells;

	private final int numMines;
	private int numFlags = 0;

	@Getter
	private boolean lost, won;

	@Getter
	private final String diffName;

	private int safeSquaresOpened = 0;

	private boolean firstRevealed = false;

	@Getter
	private long startTime;

	public Board(int width, int height, int numMines, String diffName) {
		this.width = width;
		this.height = height;
		this.cells = new Cell[width][height];
		this.numMines = numMines;
		this.diffName = diffName;
		reset();
		x = width / 2;
		y = height / 2;
	}

	private void reset() {
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				boolean wasFlagged = cells[x][y] != null && cells[x][y].isFlagged();
				cells[x][y] = new Cell(false);
				if(wasFlagged) {
					cells[x][y].flag();
				}
			}
		}
	}

	public void regenerate() {
		reset();
		ThreadLocalRandom random = ThreadLocalRandom.current();
		for(int i = 0; i < numMines; i++) {
			int x = random.nextInt(width);
			int y = random.nextInt(height);
			if(cells[x][y].isMine()) {
				i--;
				continue;
			}

			cells[x][y] = new Cell(true);
		}

		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				int adjacentMines = 0;
				for(int x1 = x - 1; x1 <= x + 1; x1++) {
					for(int y1 = y - 1; y1 <= y + 1; y1++) {
						if(x1 < 0 || x1 >= width || y1 < 0 || y1 >= height) {
							continue;
						}

						if(cells[x1][y1].isMine()) {
							adjacentMines++;
						}
					}
				}

				cells[x][y].setAdjacentMines(adjacentMines);
			}
		}
	}

	@Override
	public String toString() {
		if(safeSquaresOpened == width * height - numMines) {
			won = true;
		}

		StringBuilder sb = new StringBuilder();
		sb.append("Minesweeper (").append(diffName).append(") - ").append(numMines - numFlags).append(" mines left:     \n");
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				boolean isSelection = this.x == x && this.y == y && !isGameOver();

				sb.append(cells[x][y].toString(isSelection)).append(' ');
			}

			sb.append("\n");
		}

		return sb.toString();
	}

	public void handleMovement(char move) {
		if(isGameOver()) return;
		switch(move) {
			case 'A', 'w' -> {
				if(y > 0) y--;
			}
			case 'B', 's' -> {
				if(y < height - 1) y++;
			}
			case 'C', 'd' -> {
				if(x < width - 1) x++;
			}
			case 'D', 'a' -> {
				if(x > 0) x--;
			}
		}
	}

	public boolean isGameOver() {
		return lost || won;
	}

	private boolean shouldFloodReveal() {
		Cell cell = cells[x][y];
		return !cell.isRevealed() && !cell.isMine()
				&& (cell.getAdjacentMines() <= 0 || diffName.equals("Custom"));
	}

	private void start() {
		startTime = System.currentTimeMillis();
		firstRevealed = true;
		do {
			// guarantees that the first time user reveals it will flood unless custom difficulty
			regenerate();
		} while(!shouldFloodReveal());
		floodReveal(x, y);
	}

	public void reveal() {
		reveal(x, y);
	}

	private void reveal(int x, int y) {
		Cell cell = cells[x][y];
		if(cell.isFlagged()) return;

		if(!firstRevealed) {
			start();
			return;
		}

		if(cell.isRevealed()) return;

		if(cell.isMine()) {
			cell.reveal();
			lost = true;
			return;
		}

		if(cell.getAdjacentMines() <= 0) {
			floodReveal(x, y);
		} else {
			cell.reveal();
			safeSquaresOpened++;
		}
	}

	private void floodReveal(int x, int y) {
		if(x < 0 || x >= width || y < 0 || y >= height) {
			return;
		}

		Cell cell = cells[x][y];

		if(cell.isRevealed() || cell.isFlagged()) {
			return;
		}

		cell.reveal();
		safeSquaresOpened++;

		if(cell.getAdjacentMines() > 0) {
			return;
		}

		for(int x1 = x - 1; x1 <= x + 1; x1++) {
			for(int y1 = y - 1; y1 <= y + 1; y1++) {
				floodReveal(x1, y1);
			}
		}
	}

	public void chord() {
		Cell cell = cells[x][y];
		if(!cell.isRevealed()) return;

		int adjacentFlags = 0;
		for(int x1 = x - 1; x1 <= x + 1; x1++) {
			for(int y1 = y - 1; y1 <= y + 1; y1++) {
				if(x1 < 0 || x1 >= width || y1 < 0 || y1 >= height) {
					continue;
				}

				if(cells[x1][y1].isFlagged()) {
					adjacentFlags++;
				}
			}
		}

		if(adjacentFlags != cell.getAdjacentMines()) return;

		for(int x1 = x - 1; x1 <= x + 1; x1++) {
			for(int y1 = y - 1; y1 <= y + 1; y1++) {
				if(x1 < 0 || x1 >= width || y1 < 0 || y1 >= height) {
					continue;
				}

				if(!cells[x1][y1].isFlagged()) {
					reveal(x1, y1);
				}
			}
		}
	}

	public void revealAll() {
		if(!isGameOver()) {
			throw new IllegalStateException("Game is not over yet");
		}

		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				if(cells[x][y].isFlagged() && !cells[x][y].isMine()) {
					cells[x][y].flag();
				}
				cells[x][y].reveal();
			}
		}
	}

	public void flag() {
		Cell cell = cells[x][y];
		if(cell.isRevealed()) return;

		cell.flag();
		if(cell.isFlagged()) {
			numFlags++;
		} else {
			numFlags--;
		}
	}
}
