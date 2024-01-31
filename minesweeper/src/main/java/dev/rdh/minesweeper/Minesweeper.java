package dev.rdh.minesweeper;

import dev.rdh.games.Console;
import dev.rdh.games.SelectionModal;

public class Minesweeper {
	public static void main(String[] args) {
		if(System.getProperty("os.name").toLowerCase().contains("wind")) {
			System.err.println("This game is not supported on Windows!");
			return;
		}

		try(Console console = new Console()) {
			console.echo(false);
			console.clearScreen();
			console.hideCursor();

			SelectionModal main = SelectionModal.of("Welcome to Minesweeper!\nChoose an option:", "Play", "Controls", "Exit");

			final Difficulty[] diffs = Difficulty.values();
			SelectionModal diffSelection = SelectionModal.of("Choose a difficulty:", diffs);
			while(true) {
				int mainSelection = main.displayOn(console);
				if(mainSelection == 2) {
					console.clearScreen();
					break;
				}
				if(mainSelection == 1) {
					configureControls(console);
					continue;
				}
				Difficulty diff = diffs[diffSelection.displayOn(console)];
				new Game(console, diff).run();
			}
		} catch(Exception e) {
			e.printStackTrace(System.err);
		}
	}

	private static void configureControls(Console console) {
		console.clearScreen();

		Config config = Config.load();
		while(true) {
			SelectionModal controls = SelectionModal.of("Choose an option to change:",
					"Use WASD: " + (config.useWASD ? "Yes" : "No"), "Chord: " + key(config.chord),
					"Flag: " + key(config.flag), "Reveal: " + key(config.reveal), "Reset: " + key(config.reset),
					"Change Custom Difficulty",
					"Reset Controls to Defaults", "Back");
			int selection = controls.displayOn(console);

			switch(selection) {
				case 0 -> config.useWASD = !config.useWASD;
				case 1 -> {
					console.print("Enter a new chord key: _");
					config.chord = console.readChar();
				}
				case 2 -> {
					console.print("Enter a new flag key: _");
					config.flag = console.readChar();
				}
				case 3 -> {
					console.print("Enter a new reveal key: _");
					config.reveal = console.readChar();
				}
				case 4 -> {
					console.print("Enter a new reset key: _");
					config.reset = console.readChar();
				}
				case 5 -> {
					config.width = console.readInt("Enter a width: ", 1, Console.DEFAULT_TERMINAL_WIDTH / 2);
					config.height = console.readInt("Enter a height: ", 1, Console.DEFAULT_TERMINAL_HEIGHT - 2);
					config.mines = console.readInt("Enter a number of mines: ", 0, config.width * config.height - 1);
				}
				case 6 -> config = Config.defaultConfig();
				case 7 -> {
					config.save();
					return;
				}
			}
		}
	}

	private static String key(char c) {
		return switch(c) {
			case '\n' -> "Enter";
			case '\t' -> "Tab";
			case '\b' -> "Backspace";
			case ' ' -> "Space";
			default -> String.valueOf(c);
		};
	}
}
