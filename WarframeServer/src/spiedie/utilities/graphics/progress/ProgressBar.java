package spiedie.utilities.graphics.progress;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

public class ProgressBar extends JFrame{
	private static final long serialVersionUID = 2708874015296670306L;
	
	public static boolean HEADLESS = false;
	
	private JProgressBar bar;
	private JProgressBar size;
	private JProgressBar num;
	private JTextField file = new JTextField(25);
	private JTextField speed = new JTextField(25);
	private Map<JProgressBar, Integer> ratios = new ConcurrentHashMap<JProgressBar, Integer>();
	
	private boolean single;
	private boolean useFilenameDisplay;
	private boolean useSpeedDisplay;
	
	private String prependTitle = "";
	private String postTitle = "";
	
	public ProgressBar(boolean single, boolean useFilenameDisplay, boolean useSpeedDisplay, boolean startVisible){
		super();
		this.single = single;
		this.useFilenameDisplay = useFilenameDisplay;
		this.useSpeedDisplay = useSpeedDisplay;
		init();
		initComponents();
		initLocation();
		initExtraDisplays();
		setSizes();
		if(startVisible)
			setVisible(true);
	}
	
	private void init(){
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		getContentPane().setBackground(Color.DARK_GRAY);
		addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				if(JOptionPane.showConfirmDialog(null, "Stop progress?\nNo/cancel makes this run in background.") == 0){
					System.exit(0);
				}
			}
		});
		setLayout(new FlowLayout());
		setTitle("0%");
		setResizable(false);
	}
	
	public void setVisible(boolean isVisible) {
		super.setVisible(isVisible && !HEADLESS);
	}
	
	private void initComponents(){
		int a = 320;
		int b = 110;
		if(!isUndecorated()) b += 30;
		if(single){
			bar = new JProgressBar(0, 100);
			b -= 25;
			add(bar);
		} else{
			size = new JProgressBar(0, 100);
			num = new JProgressBar(0, 100);
			add(num);
			add(size);
		}
		
		if(!useSpeedDisplay){
			b -= 25;
		}
		if(!useFilenameDisplay){
			b -= 25;
		}
		setSize(a,b);
	}
	
	private void initLocation(){
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (dim.width-getSize().width)/2;
		int y = (dim.height-getSize().height)/2;
		setLocation(x, y);
	}
	
	private void initExtraDisplays(){
		if(useFilenameDisplay){
			add(file);
			file.setEditable(false);
		}
		if(useSpeedDisplay){
			add(speed);
			speed.setEditable(false);
		}
	}
	
	private void setSizes(){
		if(bar != null){
			bar.setPreferredSize(new Dimension(280, 20));
			bar.setStringPainted(true);
			bar.setValue(0);
		}
		if(num != null){
			num.setPreferredSize(new Dimension(280, 20));
			num.setStringPainted(true);
			num.setValue(0);
		}
		if(size != null){
			size.setPreferredSize(new Dimension(280, 20));
			size.setStringPainted(true);
			size.setValue(0);
		}
	}
	//---------- get/set bar maximum - default 100----------\\
	public void setBarMax(long max){
		setMaximum(bar, max);
	}
	
	private void setMaximum(JProgressBar bar, long max){
		if(bar != null){
			if(max > Integer.MAX_VALUE){
				int rat = 2;
				while(max/rat > Integer.MAX_VALUE) rat++;
				ratios.put(bar, rat);
			} else{
				ratios.put(bar, 1);
			}
			max = max/ratios.get(bar);
			bar.setMaximum((int)max);
		}
	}
	
	public int getBarMax(){
		return getMaximum(bar);
	}
	
	private int getMaximum(JProgressBar bar){
		return bar.getMaximum();
	}
	
	//---------- get/set bar done ----------\\
	
	public void setBarDone(long x){
		setDone(bar, x);
	}
	
	private void setDone(JProgressBar bar, long val){
		val = val / ratios.get(bar);
		if(bar != null){
			if(val < 0) val = 0;
			else if(val > bar.getMaximum()) val = bar.getMaximum();
			bar.setValue((int)val);
			setTitlePercent();
		}
	}
	
	public int getValue(){
		if(single) return bar.getValue();
		else return size.getValue()+num.getValue();
	}
	
	//---------- set title ----------\\
	
	public void setPostTitle(String title){
		this.postTitle = title;
	}
	
	public void setTitle(String title){
		super.setTitle(this.prependTitle+" "+title+" "+postTitle);
	}
	
	private void setTitlePercent(){
		if(bar == null){
			double per = (size.getPercentComplete()+num.getPercentComplete())/2;
			setTitlePercent(per);
		} else setTitlePercent(bar);
	}

	private void setTitlePercent(JProgressBar bar){
		setTitlePercent(bar.getPercentComplete());
	}
	
	private void setTitlePercent(double val){
		setTitlePercent(((int)(val*100)));
	}
	
	//---------- set bar content ----------\\
	public void setFile(String file){
		if(this.file != null)
			this.file.setText(file);
	}
	
	public String getFile(){
		return this.file.getText();
	}
	
	public void setSpeed(String s){
		if(speed != null){
			String text = speed.getText();
			if(s != null && !text.equals(s))
				speed.setText(s);
		}
	}
	
	//---------- end ----------\\
}
