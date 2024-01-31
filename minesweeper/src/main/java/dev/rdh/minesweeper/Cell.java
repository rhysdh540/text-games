package dev.rdh.minesweeper;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Cell {
	private boolean revealed;
	private boolean flagged;
	private final boolean mine;

	@Setter(AccessLevel.PACKAGE)
	private int adjacentMines;

	public Cell(boolean mine) {
		this.mine = mine;
	}

	public void reveal() {
		if(flagged) return;
		this.revealed = true;
	}

	public void flag() {
		if(!this.revealed) {
			this.flagged = !this.flagged;
		}
	}

	public String toString(boolean isSelected) {
		if(this.flagged) {
			return isSelected ? "\033[30;101mF\033[0m" : "\033[91mF\033[0m";
		}

		if(!this.revealed) {
			return isSelected ? "\033[30;47m.\033[0m" : ".";
		}

		if(this.mine) {
			return "\033[91mX\033[0m";
		}

		String s = "";
		if(isSelected) {
			s += "\033[30;47m";
		}
		if(this.adjacentMines == 0) {
			s += " ";
		} else {
			s += this.adjacentMines;
		}
		if(isSelected) {
			s += "\033[0m";
		}
		return s;
	}
}
