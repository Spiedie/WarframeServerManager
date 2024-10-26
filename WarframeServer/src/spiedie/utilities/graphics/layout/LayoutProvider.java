package spiedie.utilities.graphics.layout;

public class LayoutProvider {
	protected int x, y, w, h;
	protected double rx, ry, rw, rh;
	protected String name;

	public LayoutProvider(){
		
	}
	
	public LayoutProvider(String name){
		setName(name);
	}
	public String getName(){
		return name;
	}
	
	public double x() {
		return x;
	}

	public double y() {
		return y;
	}

	public double w() {
		return w;
	}

	public double h() {
		return h;
	}

	public double rx() {
		return rx;
	}

	public double ry() {
		return ry;
	}

	public double rw() {
		return rw;
	}

	public double rh() {
		return rh;
	}
	
	public LayoutProvider setName(String name){
		this.name = name;
		return this;
	}

	public LayoutProvider setAbs(int x, int y, int w, int h){
		setX(x);
		setY(y);
		setW(w);
		setH(h);
		return this;
	}
	
	public LayoutProvider setRel(double x, double y, double w, double h){
		setRX(x);
		setRY(y);
		setRW(w);
		setRH(h);
		return this;
	}
	
	public void setX(int x){
		this.x = x;
	}

	public void setY(int y){
		this.y = y;
	}

	public void setW(int w){
		this.w = w;
	}

	public void setH(int h){
		this.h = h;
	}

	public void setRX(double rx){
		this.rx = rx;
	}

	public void setRY(double ry){
		this.ry = ry;
	}

	public void setRW(double rw){
		this.rw = rw;
	}

	public void setRH(double rh){
		this.rh = rh;
	}
	
	public String toString(){
		return "LayProv:"+x()+","+y()+","+w()+"x"+h()+" "+rx()+","+ry()+","+rw()+"x"+rh();
	}
}
