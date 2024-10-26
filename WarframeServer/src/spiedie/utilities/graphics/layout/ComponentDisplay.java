package spiedie.utilities.graphics.layout;

import java.awt.EventQueue;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import spiedie.utilities.util.Time;
import spiedie.utilities.util.log.Log;

public class ComponentDisplay extends JPanel{
	private static final long serialVersionUID = -3487617715346877275L;
	private List<ContentProvider> contents;
	private List<LayoutProvider> layouts;
	private JFrame frame;
	private boolean showing;
	public ComponentDisplay(){
		contents = new ArrayList<ContentProvider>();
		layouts = new ArrayList<LayoutProvider>();
	}
	
	public void addContentProvider(ContentProvider p){
		contents.add(p);
		add(p.getComponent());
		revalidateAndRepaint();
	}
	
	public void addLayoutProvider(LayoutProvider p){
		layouts.add(p);
		revalidateAndRepaint();
	}
	
	public void revalidateAndRepaint(){
		if(EventQueue.isDispatchThread()){
			revalidate();
			repaint();
		} else{
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					revalidateAndRepaint();
				}
			});
		}
	}
	
	public void addSimple(String name, JComponent c, int x, int y, int w, int h, double rx, double ry, double rw, double rh){
		addContentProvider(new ContentProvider(c, name));
		LayoutProvider p = new LayoutProvider(name);
		p.setAbs(x, y, w, h);
		p.setRel(rx, ry, rw, rh);
		addLayoutProvider(p);
	}
	
	public LayoutProvider getLayoutProvider(String name){
		for(LayoutProvider p : getConcurrent(layouts)){
			if(name.equals(p.getName())) return p;
		}
		return null;
	}
	
	public JFrame frame(){
		if(!showing) showFrame();
		while(frame == null) Time.sleep(1);	
		return frame;
	}
	
	public void showFrame(){
		if(EventQueue.isDispatchThread()){
			if(showing) return;
			showing = true;
			frame = new JFrame();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.add(this);
			frame.setVisible(true);
			frame.repaint();
		} else{
			SwingUtilities.invokeLater(new Runnable(){
				public void run(){
					showFrame();
				}
			});
		}
	}
	
	public void update(){
		int fw = getWidth();
		int fh = getHeight();
		double ppw = fw/100d;
		double pph = fh/100d;
		for(ContentProvider c : getConcurrent(contents)){
			LayoutProvider l = getLayoutProvider(c.getName());
			if(l != null){
				int x = ((int) (l.x() + l.rx() * ppw));
				int y = ((int) (l.y() + l.ry() * pph));
				int w = ((int) (l.w() + l.rw() * ppw));
				int h = ((int) (l.h() + l.rh() * pph));
				c.getComponent().setBounds(x, y, w, h);
			} else{
				c.getComponent().setBounds(0, 0, 0, 0);
			}
		}
		for(ContentProvider c : getConcurrent(contents)){
			c.getComponent().validate();
		}
	}
	
	public void paintComponent(Graphics g){
		update();
		super.paintComponent(g);
	}
	
	private static <T> List<T> getConcurrent(List<T> list){
		List<T> res = new ArrayList<T>();
		for(int i = 0; i < list.size();i++){
			try{
				res.add(list.get(i));
			} catch(IndexOutOfBoundsException e){
				Log.err(ComponentDisplay.class, e);
			}
		}
		return res;
	}
}
