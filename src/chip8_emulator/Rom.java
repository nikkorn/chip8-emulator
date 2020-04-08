package chip8_emulator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * A game rom.
 */
public class Rom {
	/**
	 * The rom name.
	 */
	private String name;
	/**
	 * The rom data.
	 */
	private byte[] data;
	
	/**
	 * Creates a new instance of the Rom class.
	 * @param name The rom name.
	 * @param data The rom data.
	 */
	private Rom(String name, byte[] data) {
		this.name = name;
		this.data = data;
	}
	
	/*
	 * Gets the rom name.
	 */
	public String getName() {
		return this.name;
	}
	
	/*
	 * Gets the rom data.
	 */
	public byte[] getData() {
		return this.data;
	}
	
	/**
	 * Load a game rom.
	 * @param name The name of the rom file.
	 * @return A game rom.
	 */
	public static Rom load(String name) {
		try {
			// Load data from disk!
			byte[] data = Files.readAllBytes(Paths.get("roms/" + name));
			
			// Create and return the rom.
			return new Rom(name, data);
		} catch (IOException e) {
			throw new RuntimeException("cannot find rom: roms/" + name);
		}
	}
}
