package chip8_emulator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * The emulator display.
 */
public class Display extends JPanel {	
	private static final long serialVersionUID = 1L;
	/**
	 * The pixel values for the display.
	 * White if true, otherwise black.
	 */
	private boolean[] pixels = new boolean[Constants.DISPLAY_WIDTH * Constants.DISPLAY_HEIGHT];

	/**
	 * Creates a new instance of the Display class.
	 */
	private Display() {
		this.setPreferredSize(new Dimension(Constants.DISPLAY_SCALE * Constants.DISPLAY_WIDTH, Constants.DISPLAY_SCALE * Constants.DISPLAY_HEIGHT));
		this.setVisible(true);
	}
	
	/**
	 * Clear the display and set all pixels to black.
	 */
	public void clear() {
		for (int index = 0; index < this.pixels.length; index++) {
			this.pixels[index] = false;
		}
	}
	
	/**
	 * Sets a pixel value.
	 * @param x The x position.
	 * @param y The y position.
	 * @param isWhite Whether the pixel is white.
	 */
	public void setPixel(int x, int y, boolean isWhite) {
		this.pixels[(y * Constants.DISPLAY_WIDTH) + x] = isWhite;
	}
	
	/**
	 * Paint the display.
	 */
	@Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Draw each pixel to the display.
        for (int y = 0; y < Constants.DISPLAY_HEIGHT; y++) {
        	for (int x = 0; x < Constants.DISPLAY_WIDTH; x++) {
        		boolean isPixelWhite = this.pixels[(y * Constants.DISPLAY_WIDTH) + x];
        		g.setColor(isPixelWhite ? Color.WHITE : Color.BLACK);
        		g.fillRect(x * Constants.DISPLAY_SCALE, y * Constants.DISPLAY_SCALE, Constants.DISPLAY_SCALE, Constants.DISPLAY_SCALE);
            }        	
        }
    }
	
	/**
	 * Creates a Display instance wrapped in a JFrame.
	 */
	public static Display create(String title) {
		// Create the display.
		Display display = new Display();
		
		// Create the application jframe in which to show the display. 
		JFrame frame = new JFrame(title);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(display);
		frame.pack();
		frame.setVisible(true);
		
		return display;
	}
}