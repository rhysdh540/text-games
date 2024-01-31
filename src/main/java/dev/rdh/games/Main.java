package dev.rdh.games;

import dev.rdh.frogger.Frogger;
import dev.rdh.minesweeper.Minesweeper;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class Main {
	private static final Map<String, Consumer<String[]>> GAMES = Map.of(
			"Minesweeper", Minesweeper::main,
			"Frogger", Frogger::main
	);

	public static void main(String[] args) {
		while(true) {
			List<String> gameNames = GAMES.keySet().stream().toList();
			Console console = new Console();
			SelectionModal sm = SelectionModal.of("Choose a game:",
					gameNames,
					"Exit");
			int selection = sm.displayOn(console);
			console.close();
			if(selection == gameNames.size()) {
				break;
			}
			GAMES.get(gameNames.get(selection)).accept(args);
		}
	}
}
