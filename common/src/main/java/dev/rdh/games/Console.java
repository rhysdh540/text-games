package dev.rdh.games;

import lombok.Getter;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;

public class Console implements AutoCloseable {
	public static final int DEFAULT_TERMINAL_WIDTH = 80;
	public static final int DEFAULT_TERMINAL_HEIGHT = 25;

	private final String originalStty;

	@Getter
	private boolean echo = false;

	@Getter
	private boolean cursorShown = true;

	private final Thread shutdownHook = new Thread(this::actuallyClose);

	@Getter
	private final InputStream in;

	@Getter
	private final PrintStream out;

	public Console(InputStream in, OutputStream out) {
		this.originalStty = stty("-g");

		stty("-icanon min 1 -echo");

		Runtime.getRuntime().addShutdownHook(shutdownHook);

		this.in = in;
		this.out = out instanceof PrintStream p ? p : new PrintStream(out);
	}

	public Console() {
		this(System.in, System.out);
	}

	@Override
	public void close() {
		Runtime.getRuntime().removeShutdownHook(shutdownHook);
		actuallyClose();
	}

	private void actuallyClose() {
		stty(originalStty.trim());
		showCursor();
	}

	public void echo(boolean echo) {
		if(this.echo != echo) {
			stty(echo ? "echo" : "-echo");
			this.echo = echo;
		}
	}

	public char readChar() {
		try {
			return (char) in.read();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public String readLine() {
		boolean cursorShown = this.cursorShown;
		boolean echo = this.echo;
		showCursor();
		echo(false);
		StringBuilder line = new StringBuilder(DEFAULT_TERMINAL_WIDTH);

		int cursorPos = 0;
		while(true) {
			char c = readChar();
			if(c == '\n' || c == '\r') {
				break;
			} else if(c == '\b' || c == 127) {
				if (cursorPos <= 0) continue;
				cursorPos--;
				line.deleteCharAt(cursorPos);
				moveCursorLeft();
				print(line.substring(cursorPos) + " ");
				moveCursorLeft(line.length() - cursorPos + 1);
			} else if(c == '\033') {
				cursorPos += handleArrowKeys(cursorPos, line.length());
			} else {
				line.insert(cursorPos, c);
				print(line.substring(cursorPos));
				cursorPos++;
				moveCursorLeft(line.length() - cursorPos - 1);
			}
		}

		if(!cursorShown) {
			hideCursor();
		}

		echo(echo);
		return line.toString();
	}

	private int handleArrowKeys(int cursorPos, int lineLength) {
		if(readChar() != '[') {
			return 0;
		}

		char direction = readChar();
		return switch(direction) {
			case 'C' -> {
				if(cursorPos >= lineLength) yield 0;
				moveCursorRight();
				yield 1;
			}
			case 'D' -> {
				if(cursorPos <= 0) yield 0;
				moveCursorLeft();
				yield -1;
			}
			default -> 0;
		};
	}

	public void waitForEnter() {
		boolean echo = this.echo;
		echo(false);
		while(true) {
			char c = readChar();
			if(c == '\n' || c == '\r') {
				break;
			}
		}
		echo(echo);
	}

	public void moveCursorLeft() {
		print("\033[D");
	}

	public void moveCursorLeft(int n) {
		print("\033[" + n + "D");
	}

	public void moveCursorRight(int n) {
		print("\033[" + n + "C");
	}

	public void moveCursorRight() {
		print("\033[C");
	}

	public void clearScreen() {
		print("\033[H\033[2J");
	}

	public void hideCursor() {
		if(cursorShown) {
			print("\033[?25l");
			cursorShown = false;
		}
	}

	public void showCursor() {
		if(!cursorShown) {
			print("\033[?25h");
			cursorShown = true;
		}
	}

	public void moveCursor(int row, int col) {
		print("\033[" + (row + 1) + ";" + (col + 1) + "H");
	}

	public void print(Object s) {
		out.print(s);
	}

	public void println(Object s) {
		out.println(s);
	}

	public void println() {
		out.println();
	}

	private static String stty(final String args) {
		try {
			ProcessBuilder pb = new ProcessBuilder("sh", "-c", "stty " + args + " < /dev/tty");
			Process process = pb.start();
			InputStream in = process.getInputStream();
			ByteArrayOutputStream out = new ByteArrayOutputStream();

			int c;
			while ((c = in.read()) != -1) {
				out.write(c);
			}

			return out.toString();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public int readInt(String prompt) {
		return readInt(prompt, Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	public void setBackgroundColor(int color) {
		int r = (color >> 16) & 0xFF;
		int g = (color >> 8) & 0xFF;
		int b = color & 0xFF;
		print(String.format("\033[48;2;%d;%d;%dm", r, g, b));
	}

	public void setBackgroundColor(Color color) {
		setBackgroundColor(color == null ? 0 : color.getRGB());
	}

	public void resetBackgroundColor() {
		print("\033[49m");
	}

	public void setForegroundColor(Color color) {
		setForegroundColor(color == null ? 0 : color.getRGB());
	}

	public void resetForegroundColor() {
		print("\033[39m");
	}

	public void setForegroundColor(int color) {
		int r = (color >> 16) & 0xFF;
		int g = (color >> 8) & 0xFF;
		int b = color & 0xFF;
		print(String.format("\033[38;2;%d;%d;%dm", r, g, b));
	}

	public int readInt(String prompt, int min, int max) {
		while(true) {
			clearScreen();
			print(prompt);
			try {
				int i = Integer.parseInt(readLine());
				if(i < min || i > max) {
					hideCursor();
					print("\nNumber must be between " + min + " and " + max + "!");
					waitForEnter();
				} else return i;
			} catch(NumberFormatException e) {
				hideCursor();
				print("\nInvalid number!");
				waitForEnter();
			}
		}
	}
}