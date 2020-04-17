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
	private short I;
	/**
	 * The stack and stack pointer used for tracking calls through subroutines.
	 */
	private short[] stack = new short[16];
	private short SP;
	/**
	 * The pixel values for the display.
	 * White if true, otherwise black.
	 */
	private boolean[] pixels = new boolean[Constants.DISPLAY_WIDTH * Constants.DISPLAY_HEIGHT];
	/**
	 * Whether the display has changed as a side-effect of executing the last opcode.
	 */
	private boolean hasDisplayChanged = false;
	/**
	 * The ROM data.
	 */
	private byte[] rom;
	/**
	 * The input key states.
	 */
	private boolean[] keys = new boolean[16];
	/** 
	 * The machine timers.
	 */
	private byte delayTimer, soundTimer;
	
	/**
	 * Sets the rom data.
	 * @param rom The rom data.
	 */
	public void setRomData(byte[] rom) {
		this.rom = rom;
		
		// Clear the portion of memory reserved for rom data.
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
					/**
					 * Clear the screen.
					 */
					case 0x0000:
						// Clear the screen.
						pixels = new boolean[Constants.DISPLAY_WIDTH * Constants.DISPLAY_HEIGHT];
						
						// We will need to redraw the display.
						hasDisplayChanged = true;
						break;
						
					/**
					 * Return from the current subroutine.
					 */
					case 0x000E:
						// Set the program counter to the last counter position we were at.
						PC = stack[--SP];
						PC += 2;
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
			 * (0x2NNN) Calls subroutine at NNN.
			 */
			 case 0x2000:
				// Add the current program counter to the stack.
				stack[SP++] = PC;
				
				// Set the program counter to the address defined by the opcode.
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
			 * (0x8000) Various register operations.
			 */
			 case 0x8000:
				switch (opcode & 0x000F) {
                   /**
				    * (0x8XY0) Sets register X to the value of register Y.
				    */
				   case 0x0000:
					   registers[(opcode & 0x0F00) >> 8] = registers[(opcode & 0x00F0) >> 4];
					   PC += 2;
					   break;
				                  
				   default:
					   throw new RuntimeException("unknown opcode: 0x" + opcode);
				}
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
			 * (0xE000) Various key operations.
			 */
			case 0xE000:
				switch (opcode & 0x000F) {
					/**
					 * (0xEX01) Skips the next instruction if the key stored in register X is not pressed.
					 */
					case 0x0001:
						// TODO
						PC += 4;
						break;
						
					/**
					 * (0xEX0E) Skips the next instruction if the key stored in register X is pressed.
					 */
					case 0x000E:
						// TODO
						PC += 2;
						break;
						
					default:
						throw new RuntimeException("unknown opcode: 0x" + opcode);
				}
				break;
			
			/**
			 * (0xF000) Various register operations.
			 */
			case 0xF000:
				switch (opcode & 0x00FF) {
					/**
					 * Sets register X to the value of the delay timer.
					 */
					case 0x0007:
						registers[(opcode & 0x0F00) >> 8] = delayTimer;
						PC += 2;
						break;
						
					case 0x000A:
						throw new RuntimeException("unknown opcode: 0x" + Integer.toHexString(opcode));
						
					/**
					 * Sets the delay timer to register X.
					 */
					case 0x0015:
						delayTimer = registers[(opcode & 0x0F00) >> 8];
						PC += 2;
						break;
						
					case 0x0018:
						throw new RuntimeException("unknown opcode: 0x" + Integer.toHexString(opcode));
						
					/**
					 * Adds VX to I. VF is set to 1 when there is a range overflow (I+VX>0xFFF), and to 0 when there isn't.
					 */
					case 0x001E:
						registers[0xF] = (byte) ((I + registers[(opcode & 0x0F00) >> 8] > 0xFFF) ? 1 : 0);
						I += registers[(opcode & 0x0F00) >> 8];
						PC += 2;
						break;
						
					case 0x0029:
						throw new RuntimeException("unknown opcode: 0x" + Integer.toHexString(opcode));
						
					case 0x0033:
						throw new RuntimeException("unknown opcode: 0x" + Integer.toHexString(opcode));
						
					case 0x0055:
						throw new RuntimeException("unknown opcode: 0x" + Integer.toHexString(opcode));
						
					/**
					 * Fills register 0 to X (including X) with values from memory starting at address I.
					 * The offset from I is increased by 1 for each value written, but I itself is left unmodified.
					 */
					case 0x0065:
						for (int index = 0; index <= (opcode & 0x0F00) >> 8; index++) {
							registers[index] = memory[I + index];
						}
						PC += 2;
						break;
						
					default:
						throw new RuntimeException("unknown opcode: 0x" + Integer.toHexString(opcode));
				}
				break;
				
			default:
				throw new RuntimeException("unknown opcode: 0x" + Integer.toHexString(opcode));
		}
		 
		// TODO Update timers
		if (delayTimer > 0) {
			delayTimer--;
		}
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
