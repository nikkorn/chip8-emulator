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
			 * (0x3XNN) Skips the next instruction if register X equals NN. 
			 */
			case 0x3000:
				PC += (registers[(opcode & 0x0F00) >> 8] == (opcode & 0x00FF)) ? 4 : 2;
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
			
				
				
				// TODO Replace this stolen code.
				short x = registers[(opcode & 0x0F00) >> 8];
				short y = registers[(opcode & 0x00F0) >> 4];
				short height = (short) (opcode & 0x000F);
				short pixel;
				 
				registers[0xF] = 0;
				for (int yline = 0; yline < height; yline++) {
				    pixel = memory[I + yline];
				    for(int xline = 0; xline < 8; xline++)
				    {
				      if((pixel & (0x80 >> xline)) != 0)
				      {
				        if(pixels[(x + xline + ((y + yline) * 64))])
				        	registers[0xF] = 1;                                 
				        pixels[x + xline + ((y + yline) * 64)] = !pixels[x + xline + ((y + yline) * 64)];
				      }
				    }
				}
				
				
				
				// We will need to redraw the display.
				hasDisplayChanged = true;
				PC += 2;
				break;
		
				
				
				
			
			/**
			 * (0xF000) Various register operations.
			 */
			case 0xF000:
				switch (opcode & 0x00FF) {
					case 0x0007:
						PC += 2;
						break;
						
					case 0x000A:
						PC += 2;
						break;
						
					case 0x0015:
						PC += 2;
						break;
						
					case 0x0018:
						PC += 2;
						break;
						
					/**
					 * Adds VX to I. VF is set to 1 when there is a range overflow (I+VX>0xFFF), and to 0 when there isn't.
					 */
					case 0x001E:
						registers[0xF] = (byte) ((I + registers[(opcode & 0x0F00) >> 8] > 0xFFF) ? 1 : 0);
						I += registers[(opcode & 0x0F00) >> 8];
						PC += 2;
						break;
						
					case 0x0029:
						PC += 2;
						break;
						
					case 0x0033:
						PC += 2;
						break;
						
					case 0x0055:
						PC += 2;
						break;
						
					case 0x0065:
						PC += 2;
						break;
						
					default:
						throw new RuntimeException("unknown opcode: 0x" + opcode);
				}
				break;
				
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
