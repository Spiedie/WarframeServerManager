package spiedie.utilities.graphics.display;

import java.io.PrintStream;

import javax.swing.JFrame;

public interface ITextOutput {
	public String getText();
	public void setText(String text);
	public void append(String text);
	public void println(String text);
	public void println(Object o, String text);
	public PrintStream asPrintStream();
	public JFrame frame();
}
