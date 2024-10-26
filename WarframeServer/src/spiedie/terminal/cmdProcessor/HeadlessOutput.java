package spiedie.terminal.cmdProcessor;

import java.io.PrintStream;

import javax.swing.JFrame;

import spiedie.utilities.graphics.display.ITextOutput;
import spiedie.utilities.util.log.Log;

public class HeadlessOutput implements ITextOutput{
	public void println(String text) {
		println(HeadlessOutput.class, text);
	}
	
	public void println(Object o, String text) {
		System.out.println(text);
		Log.write(o, text, false, false, true);
	}
	
	public void append(String text) {
		System.out.print(text);
		Log.write(HeadlessOutput.class, text, false, false, true);
	}

	public String getText() {
		return null;
	}

	public void setText(String text) {
		
	}

	public PrintStream asPrintStream() {
		return null;
	}

	public JFrame frame() {
		return null;
	}

}
