package dev.rdh.twenty48;

import dev.rdh.games.Console;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class Board {
	private final int[][] board = new int[4][4];

	public static final Map<Integer, Color> COLORS = new HashMap<>();
	static {
		COLORS.put(0, new Color(0xCDC1B4));
		COLORS.put(2, new Color(0xEEE4DA));
		COLORS.put(4, new Color(0xEDE0C8));
		COLORS.put(8, new Color(0xF2B179));
		COLORS.put(16, new Color(0xF59563));
		COLORS.put(32, new Color(0xF67C5F));
		COLORS.put(64, new Color(0xF65E3B));
		COLORS.put(128, new Color(0xEDCF72));
		COLORS.put(256, new Color(0xEDCC61));
		COLORS.put(512, new Color(0xEDC850));
		COLORS.put(1024, new Color(0xEDC53F));
		COLORS.put(2048, new Color(0xEDC22E));
	}

	public Board() {
		//initialize board with two random cells
		addRandomCell();
		addRandomCell();
	}

	public void addRandomCell() {
		//find empty cells
		int emptyCells = 0;
		for(int y = 0; y < 4; y++) {
			for(int x = 0; x < 4; x++) {
				if(board[y][x] == 0) {
					emptyCells++;
				}
			}
		}
		if(emptyCells == 0) {
			return;
		}
		//pick a random empty cell
		int cell = (int)(Math.random() * emptyCells);
		for(int y = 0; y < 4; y++) {
			for(int x = 0; x < 4; x++) {
				if(board[y][x] != 0) continue;

				if(cell == 0) {
					//add a 2 or 4 to the cell
					board[y][x] = Math.random() < 0.9 ? 2 : 4;
					return;
				}
				cell--;
			}
		}
	}

	public void moveUp() {
		boolean moved = false;
		for(int x = 0; x < 4; x++) {
			for(int y = 1; y < 4; y++) {
				if(board[y][x] == 0) continue;

				int y2 = y;
				while(y2 > 0 && (board[y2 - 1][x] == 0 || board[y2 - 1][x] == board[y][x])) {
					y2--;
				}

				if(board[y2][x] == board[y][x]) {
					board[y2][x] *= 2;
					board[y][x] = 0;
					moved = true;
				}

				if(y2 == y) continue;
				if(board[y2][x] == 0) {
					board[y2][x] = board[y][x];
					board[y][x] = 0;
					moved = true;
				}
			}
		}
		if(moved) {
			addRandomCell();
		}
	}

	public void moveDown() {
		boolean moved = false;
		for(int x = 0; x < 4; x++) {
			for(int y = 2; y >= 0; y--) {
				if(board[y][x] == 0) continue;

				int y2 = y;
				while(y2 < 3 && (board[y2 + 1][x] == 0 || board[y2 + 1][x] == board[y][x])) {
					y2++;
				}

				if(board[y2][x] == board[y][x]) {
					board[y2][x] *= 2;
					board[y][x] = 0;
					moved = true;
				}
				if(y2 == y) continue;
				if(board[y2][x] == 0) {
					board[y2][x] = board[y][x];
					board[y][x] = 0;
					moved = true;
				}
			}
		}
		if(moved) {
			addRandomCell();
		}
	}

	public void moveLeft() {
		boolean moved = false;
		for(int y = 0; y < 4; y++) {
			for(int x = 1; x < 4; x++) {
				if(board[y][x] == 0) continue;

				int x2 = x;
				while(x2 > 0 && board[y][x2 - 1] == 0) {
					x2--;
				}

				if(board[y][x2] == board[y][x]) {
					board[y][x2] *= 2;
					board[y][x] = 0;
					moved = true;
				}
				if(x2 == x) continue;
				if(board[y][x2] == 0) {
					board[y][x2] = board[y][x];
					board[y][x] = 0;
					moved = true;
				}
			}
		}
		if(moved) {
			addRandomCell();
		}
	}

	public void moveRight() {
		boolean moved = false;
		for(int y = 0; y < 4; y++) {
			for(int x = 2; x >= 0; x--) {
				if(board[y][x] == 0) continue;

				int x2 = x;
				while(x2 < 3 && (board[y][x2 + 1] == 0 || board[y][x2 + 1] == board[y][x])) {
					x2++;
				}

				if(board[y][x2] == board[y][x]) {
					board[y][x2] *= 2;
					board[y][x] = 0;
					moved = true;
				}
				if(x2 == x) continue;
				if(board[y][x2] == 0) {
					board[y][x2] = board[y][x];
					board[y][x] = 0;
					moved = true;
				}
			}
		}
		if(moved) {
			addRandomCell();
		}
	}

	public boolean isFull() {
		for(int y = 0; y < 4; y++) {
			for(int x = 0; x < 4; x++) {
				if(board[y][x] == 0) {
					return false;
				}
			}
		}
		return true;
	}

	public void display(Console console) {
		//print out grid: 4x4 cells of 7x3 characters each

		Color borderColor = new Color(0xBBADA0);
		console.setBackgroundColor(borderColor);
		console.println(" ".repeat(38));

		console.setForegroundColor(new Color(0x776E65));
		for(int y = 0; y < 4 * 3; y++) {
			for(int x = 0; x < 4; x++) {
				int cellValue = board[y / 3][x];
				Color color = COLORS.get(cellValue);
				console.setBackgroundColor(borderColor);
				console.print("  ");

				console.setBackgroundColor(color);
				if(y % 3 == 1) { // middle row
					console.print(cellValue == 0 ? "       " : center7(cellValue));
				} else {
					console.print("       ");
				}

				if(x == 3) {
					console.setBackgroundColor(borderColor);
					console.print("  ");
				}
			}
			console.println();
			if(y % 3 == 2) {
				console.setBackgroundColor(borderColor);
				console.println(" ".repeat(38));
			}
		}

		console.resetBackgroundColor();
		console.resetForegroundColor();
	}

	private String center7(int value) {
		String str = Integer.toString(value);
		int padding = (7 - str.length()) / 2;
		return " ".repeat(padding) + str + " ".repeat(7 - padding - str.length());
	}
}
