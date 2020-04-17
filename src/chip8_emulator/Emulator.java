package chip8_emulator;

/**
 * The chip8 emulator.
 */
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
			// TODO Remove!!!!!!!
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			// Emulate Machine Cycle.
			this.machine.executeCycle();
			
			// Update the application display if thhe last mahcine operation updated any pixels. 
			if (this.machine.requiresDisplayUpdate()) {
				// Get the display bits.
				boolean[] pixels = this.machine.getDisplayBits();
				
				// Draw each pixel to the application display.
		        for (int y = 0; y < Constants.DISPLAY_HEIGHT; y++) {
		        	for (int x = 0; x < Constants.DISPLAY_WIDTH; x++) {
		        		this.display.setPixel(x, y, pixels[(y * Constants.DISPLAY_WIDTH) + x]);
		            }        	
		        }
				
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
