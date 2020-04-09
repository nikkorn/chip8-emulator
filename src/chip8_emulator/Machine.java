package chip8_emulator;

/**
 * Represents the Chip8 virtual machine.
 */
public class Machine {
	/**
	 * The 4kb machine memory.
	 */
	private byte[] memory = new byte[4096];
	/**
	 * The CPU registers.
	 */
	private byte[] registers = new byte[16];
	/**
	 * The program counter starting at the intial ROM address.
	 */
	private short PC = 0x200;
	/**
	 * The index register.
	 */
	private short I = 0;
	/**
	 * The pixel values for the display.
	 * White if true, otherwise black.
	 */
	private boolean[] pixels = new boolean[Constants.DISPLAY_WIDTH * Constants.DISPLAY_HEIGHT];
	/**
	 * Whether the display has changed as a side-effect of exectuing the last opcode.
	 */
	private boolean hasDisplayChanged = false;
	/**
	 * The ROM data.
	 */
	private byte[] rom;
	
	/**
	 * Sets the rom data.
	 * @param rom The rom data.
	 */
	public void setRomData(byte[] rom) {
		this.rom = rom;
		
		// Clear the portion of memeory reserved for rom data.
		for (int index = 0x200; index <= 0xFFF; index++) {
			memory[index] = 0;
		}
		
		// Load the rom into memory from address 0x200 to end 0xFFF.
		for (int index = 0; index < this.rom.length; index++) {
			memory[index + 0x200] = this.rom[index];
		}
	}
	
	/**
	 * Executes a single cycle of fetching the next program opcode and executing it.
	 */
	public void executeCycle() {
		// Reset the flag that would cause a redraw.
		hasDisplayChanged = false;
		
		// Fetch the next opcode. This is a 16bit value matching the two bytes in memory starting from the position defined by PC.
		int opcode = ((memory[PC] & 0xFF) << 8) | memory[PC + 1] & 0xFF;
		
		// Carry out an operation defined by the current opcode.
		switch (opcode & 0xF000) {
			/**
			 * Can either be 'Clear Screen' (0c00E0) or 'Return from subroutine' (0x00EE)
			 */
			case 0x0000:
				switch (opcode & 0x000F) {
					case 0x0000:
						// Clear the screen.
						pixels = new boolean[Constants.DISPLAY_WIDTH * Constants.DISPLAY_HEIGHT];
						
						// We will need to redraw the display.
						hasDisplayChanged = true;
						break;
						
					case 0x000E:
						// TODO Return from subroutine.
						break;
						
					default:
						throw new RuntimeException("unknown opcode: 0x" + opcode);
				}
				break;
				
			/**
			 * (0x1NNN) Jump to address NNN.
			 */
			case 0x1000:
				PC = (short) (opcode & 0x0FFF);
				break;
				
			/**
			 * (0x6XNN) Sets register X to NN.
			 */
			case 0x6000:
				registers[(opcode & 0x0F00) >> 8] = (byte) (opcode & 0x00FF);
				PC += 2;
				break;
				
			/**
			 * (0x7XNN) Adds NN to register X. (Carry flag is not changed)
			 */
			case 0x7000:
				registers[(opcode & 0x0F00) >> 8] += (byte) (opcode & 0x00FF);
				PC += 2;
				break;
				
			/**
			 * (0xANNN) Sets I to the address NNN.
			 */
			case 0xA000:
				I = (short) (opcode & 0x0FFF);
				PC += 2;
				break;
				
			/**
			 * (0xDXYN) Draws a sprite at coordinate (register X, register Y) that has a width of 8 pixels and a height of N.
			 */
			case 0xD000:
				// TODO Do this for real!
				System.out.println("draw!");
				pixels[23] = true;
				
				// We will need to redraw the display.
				hasDisplayChanged = true;
				PC += 2;
				break;
			
			// ....
				
			default:
				throw new RuntimeException("unknown opcode: 0x" + Integer.toHexString(opcode));
		}
		 
		// TODO Update timers
	}
	
	/**
	 * Get the display bits.
	 * @return The display bits.
	 */
	public boolean[] getDisplayBits() {
		return this.pixels;
	}
	
	/**
	 * Gets whether the application display will have to be updated.
	 * @return Whether the application display will have to be updated.
	 */
	public boolean requiresDisplayUpdate() {
		return this.hasDisplayChanged;
	}
	
	/**
	 * Reset the state of the machine.
	 */
	public void reset() {
		memory = new byte[4096];
		registers = new byte[16];
		PC = 0;
		I = 0;
		pixels = new boolean[Constants.DISPLAY_WIDTH * Constants.DISPLAY_HEIGHT];
		
		// Load the rom into memory from address 0x200 to end 0xFFF.
		for (int index = 0; index < this.rom.length; index++) {
			memory[index + 0x200] = this.rom[index];
		}
	}
}
