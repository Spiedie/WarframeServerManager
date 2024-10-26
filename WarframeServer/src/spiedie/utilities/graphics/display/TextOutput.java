package spiedie.utilities.graphics.display;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import spiedie.utilities.graphics.AbstractFrame;
import spiedie.utilities.graphics.GraphicsUtils;
import spiedie.utilities.util.log.Log;

public class TextOutput extends AbstractFrame implements ITextOutput{
	private JTextArea text;
	private boolean disposed = false;
	private JScrollPane scrollPane;
	private boolean autoVisible = true;
	private int characterLimit = 0;
	public TextOutput(){
		this(100, 100, 400, 300);
	}
	
	public TextOutput(int x, int y, int w, int h){
		this(x, y, w, h, true);
	}
	
	public TextOutput(int x, int y, int w, int h, boolean autoVisible){
		super(false);
		frame().setLocation(x, y);
		frame().setSize(w, h);
		this.autoVisible = autoVisible;
		create();
	}

	public void initFrame() {
		frame().setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		text = new JTextArea();
		text.setForeground(Color.white);
		text.setBackground(Color.black);
		text.setFont(GraphicsUtils.getMonoFont(text.getFont()));
		text.setEditable(false);
		scrollPane = new JScrollPane(text);
		scrollPane.setBorder(null);
		frame().add(scrollPane);
		if(autoVisible)
			frame().setVisible(true);
	}
	
	public TextOutput withDisposeOnclose(){
		return withOnClose(JFrame.DISPOSE_ON_CLOSE);
	}
	
	private TextOutput withOnClose(int option){
		frame().setDefaultCloseOperation(option);
		return this;
	}
	
	public JFrame frame(){
		return super.frame();
	}
	
	public String getText(){
		synchronized(this.text){
			return disposed ? "" : text.getText();
		}
	}
	
	public void setText(String text){
		if(!disposed)
			if(characterLimit > 0 && text.length() > characterLimit) text = text.substring(text.length() - characterLimit, text.length());
			synchronized(this.text){
				this.text.setText(text);
				this.text.setCaretPosition(text.length());
			}
	}
	
	public void append(String text){
		setText(getText()+text);
	}
	
	public void println(String text){
		println(this, text);
	}
	
	public void println(Object o, String text){
		Log.write(o, text);
		append(text+"\n");
	}
	
	public PrintStream asPrintStream(){
		return new PrintStream(new TextOutputWriter());
	}
	
	private class TextOutputWriter extends OutputStream{
		public void write(int b) throws IOException {
			write(new byte[]{(byte) (0xff & b)});
		}
		
		public void write(byte[] buf, int off, int len){
			TextOutput.this.append(new String(buf, off, len));
		}
	}
}
