package spiedie.warframe.allocator.console;

import java.io.PrintStream;

import javax.swing.JFrame;

import spiedie.utilities.graphics.display.ITextOutput;
import spiedie.utilities.util.log.Log;
import spiedie.warframe.WFC;

public class WFRemoteOutput implements ITextOutput{
	private StringBuilder sb;
	
	public WFRemoteOutput(){
		sb = new StringBuilder();
	}
	
	public String getText() {
		return sb.toString();
	}

	public void setText(String text) {
		sb.setLength(0);
		sb.append(text);
	}

	public void append(String text) {
		sb.append(text);
	}

	public void println(String text) {
		println(WFRemoteOutput.class, text);
	}

	public void println(Object o, String text) {
		Log.write(o, text, false, WFC.logPrintEnabled, true);
		append(text);
		append("\n");
	}

	public PrintStream asPrintStream() {
		return null;
	}

	public JFrame frame() {
		return null;
	}
}
