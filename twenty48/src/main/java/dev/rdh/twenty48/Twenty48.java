package dev.rdh.twenty48;

import dev.rdh.games.Console;

public class Twenty48 {
	public static void main(String[] args) {
		Board board = new Board();
		try(Console console = new Console()) {
			console.echo(false);
			console.clearScreen();
			console.hideCursor();

			a: while(true) {
				board.display(console);
				int key = console.getIn().read();

				switch(key) {
					case 'q' -> { break a; }
					case 'w' -> board.moveUp();
					case 'a' -> board.moveLeft();
					case 's' -> board.moveDown();
					case 'd' -> board.moveRight();
				}

				if(board.isFull()) {
					console.println("Game Over!");
					console.waitForEnter();
					board = new Board();
				}

				console.print("\033[H");
			}
		} catch(Exception e) {
			e.printStackTrace(System.err);
		}
	}
}
