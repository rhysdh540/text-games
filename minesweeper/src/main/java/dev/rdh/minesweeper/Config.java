package dev.rdh.minesweeper;

import dev.rdh.games.util.Pair;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InvalidObjectException;
import java.io.Serial;
import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.WrongMethodTypeException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;


public class Config implements Serializable {

	@Serial
	private static final long serialVersionUID = -2394573435L;

	private static final Path CONFIG_PATH = Path.of(System.getProperty("user.home")).resolve(".rdh").resolve("WINMINE.ini");

	@SerializeMe public boolean useWASD = true;
	@SerializeMe public char chord = 'c';
	@SerializeMe public char flag = 'f';
	@SerializeMe public char reveal = ' ';
	@SerializeMe public char reset = 'r';

	@SerializeMe public int width = 10;
	@SerializeMe public int height = 10;
	@SerializeMe public int mines = 10;

	/**
	 * key: field name
	 * value first: getter
	 * value second: setter
	 */
	private static final LinkedHashMap<String, Pair<MethodHandle, MethodHandle>> FIELDS;
	static {
		FIELDS = new LinkedHashMap<>();
		try {
			MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(Config.class, MethodHandles.lookup());
			for(Field field : Config.class.getDeclaredFields()) {
				if(!field.isAnnotationPresent(SerializeMe.class)) continue;
				field.setAccessible(true);
				MethodHandle getter = lookup.unreflectGetter(field);
				MethodHandle setter = lookup.unreflectSetter(field);

				FIELDS.put(field.getName(), Pair.of(getter, setter));
			}
		} catch (IllegalAccessException e) {
			throw uncheck(e);
		}
	}

	private Config() {}

	public static Config defaultConfig() {
		return new Config();
	}

	public static Config load() {
		Config config = new Config();
		if(!CONFIG_PATH.toFile().exists()) {
			return config;
		}

		try(BufferedReader in = Files.newBufferedReader(CONFIG_PATH)) {
			if(!in.readLine().equals("[Minesweeper]")) {
				return config;
			}
			String line;
			while((line = in.readLine()) != null) {
				String[] parts = line.split("=");
				if(parts.length != 2) {
					continue;
				}
				String key = parts[0].substring(0, 1).toLowerCase() + parts[0].substring(1);
				String value = parts[1];
				if(!FIELDS.containsKey(key)) {
					throw new InvalidObjectException("Unknown config key: " + key);
				}

				MethodHandle setter = FIELDS.get(key).getSecond();
				Class<?> type = setter.type().parameterType(1);
				if(type == boolean.class) {
					setter.invokeExact(config, Boolean.parseBoolean(value));
				} else if(type == char.class) {
					setter.invokeExact(config, value.charAt(0));
				} else if(type == int.class) {
					setter.invokeExact(config, Integer.parseInt(value));
				} else {
					throw new WrongMethodTypeException("Unknown config type for key: " + key + ": " + type);
				}
			}
		} catch (Throwable e) {
			throw uncheck(e);
		}

		return config;
	}

	public void save() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			Files.deleteIfExists(CONFIG_PATH);
			Files.createFile(CONFIG_PATH);
			try(BufferedWriter out = Files.newBufferedWriter(CONFIG_PATH)) {
				out.write("[Minesweeper]\n");
				for(var field : FIELDS.entrySet()) {
					String capitalized = field.getKey().substring(0, 1).toUpperCase() + field.getKey().substring(1);
					out.write(capitalized + "=" + field.getValue().getFirst().invoke(this) + "\n");
				}
			}
		} catch(Throwable e) {
			throw uncheck(e);
		}
	}

	@SuppressWarnings("unchecked")
	private static <T extends Throwable> RuntimeException uncheck(Throwable t) throws T {
		throw (T) t;
	}

	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	private @interface SerializeMe {}
}
