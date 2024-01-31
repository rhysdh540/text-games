package dev.rdh.games;

import java.util.Iterator;
import java.util.LinkedList;

public class SelectionModal {
	protected final LinkedList<String> options;
	protected int selectedIndex;
	protected final String prompt;
	protected int colorCode = 96; // bright cyan

	private SelectionModal(String prompt) {
		this.prompt = prompt;
		this.options = new LinkedList<>();
		this.selectedIndex = 0;
	}

	@SafeVarargs
	public static <T> SelectionModal of(String prompt, T... options) {
		if(options.length == 0) {
			throw new IllegalArgumentException("Must have at least one option");
		}
		SelectionModal sm = new SelectionModal(prompt);
		for(T t : options) {
			if(t == null) continue;
			sm.options.addLast(String.valueOf(t));
		}

		return sm;
	}

	@SafeVarargs
	public static <T> SelectionModal of(String prompt, Iterable<T> options, T... moreOptions) {
		SelectionModal sm = new SelectionModal(prompt);
		for(T t : options) {
			if(t == null) continue;
			sm.options.addLast(String.valueOf(t));
		}
		for(T t : moreOptions) {
			if(t == null) continue;
			sm.options.addLast(String.valueOf(t));
		}

		return sm;
	}

	public SelectionModal withColor(int colorCode) {
		this.colorCode = colorCode;
		return this;
	}

	public int displayOn(Console console) {
		console.clearScreen();
		boolean hidden = console.isCursorShown();
		console.hideCursor();
		console.println(prompt);

		int numNewLines = (int) prompt.lines().count();

		while(true) {
			console.moveCursor(numNewLines, 0);

			Iterator<String> iter = options.iterator();
			for(int i = 0; iter.hasNext(); i++) {
				if(i == selectedIndex) {
					console.print("\033[" + colorCode + ";1m");
				}

				console.print(listItem(i, iter.next()));

				if(i == selectedIndex) {
					console.print("\033[0m");
				}
			}

			char input = console.readChar();
			if(input == '\033') {
				if(console.readChar() == '[') {
					updateSelection(console.readChar());
				}
			} else if (input == '\n' || input == ' ') {
				if(!hidden) {
					console.showCursor();
				}
				return selectedIndex;
			}
		}
	}

	protected String listItem(int index, String item) {
		return (index + 1) + ". " + item + "\n";
	}

	protected void updateSelection(char direction) {
		switch(direction) {
			case 'A' -> { // up arrow
				selectedIndex--;
				if(selectedIndex < 0) {
					selectedIndex = 0;
				}
			}
			case 'B' -> { // down arrow
				selectedIndex++;
				if(selectedIndex >= options.size()) {
					selectedIndex = options.size() - 1;
				}
			}
		}
	}

	@SafeVarargs
	public static <T> SelectionModal horizontal(String prompt, T... options) {
		if(options.length == 0) {
			throw new IllegalArgumentException("Must have at least one option");
		}
		HorizontalSelectionModal sm = new HorizontalSelectionModal(prompt);
		for(T t : options) {
			sm.options.addLast(String.valueOf(t));
		}

		return sm;
	}

	private static final class HorizontalSelectionModal extends SelectionModal {
		private HorizontalSelectionModal(String prompt) {
			super(prompt);
		}

		@Override
		protected String listItem(int index, String item) {
			return item + " ";
		}

		@Override
		protected void updateSelection(char direction) {
			switch(direction) {
				case 'D' -> { // left arrow
					selectedIndex--;
					if(selectedIndex < 0) {
						selectedIndex = 0;
					}
				}
				case 'C' -> { // right arrow
					selectedIndex++;
					if(selectedIndex >= options.size()) {
						selectedIndex = options.size() - 1;
					}
				}
			}
		}
	}
}
