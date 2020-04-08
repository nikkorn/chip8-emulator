package chip8_emulator;

public class Emulator {
	/**
	 * The display.
	 */
	private Display display;
	/**
	 * The chip8 virtual machine.
	 */
	private Machine machine; 
	
	/**
	 * Loads the given rom.
	 * @param rom The rom to load.
	 */
	public void load(Rom rom) {
		// Create the display.
		this.display = Display.create(rom.getName());
		
		// Create the virtual machine.
		this.machine = new Machine();
		this.machine.setRomData(rom.getData());
		
		// The emulation loop.
		while (true) {
			// Emulate Machine Cycle
			this.machine.executeCycle();
			
			// Update Display (maybe)
			if (this.machine.requiresDisplayUpdate()) {
				// TODO Update the display.
				
				// Repaint the display.
				this.display.repaint();
			}
		}
	};
	
	/**
	 * Application entry point.
	 * @param args
	 */
	public static void main(String[] args) {
		// We need a rom name.
		if (args.length != 1) {
			System.out.println("Expected name of rom file");
			return;
		}
		
		// Create the emulator.
		Emulator emulator = new Emulator();
		
		// Load the rom from disk.
		Rom rom = Rom.load(args[0]);
		
		// Load the rom into the emulator and run it.
		emulator.load(rom);
	}
}
