package spiedie.utilities.graphics;

import java.awt.EventQueue;
import java.awt.Font;

import javax.swing.SwingUtilities;

public class GraphicsUtils {
	private GraphicsUtils(){}
	
	public static void runOnEDT(final Showable s){
		if(EventQueue.isDispatchThread()) s.createAndShowGUI();
		else SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				s.createAndShowGUI(); 
			}
		});
	}
	
	// byte for data array
	
	public static Font getMonoFont(Font font){
		Font f = new Font(Font.MONOSPACED, font == null ? Font.PLAIN : font.getStyle(), font == null ? 12 : font.getSize());
		return f;
	}
	
}
