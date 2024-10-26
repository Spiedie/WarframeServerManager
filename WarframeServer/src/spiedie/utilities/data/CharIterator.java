package spiedie.utilities.data;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class CharIterator implements Iterator<Character>{
	private String data;
	private int i = 0;
	private boolean build;
	private StringBuilder buildVal;
	public CharIterator(String s){
		this(s, 0);
	}
	
	public CharIterator(String s, int off){
		this.data = s;
		seek(off);
	}
	
	public int available(){
		return Math.max(0, data.length() - i);
	}
	
	public boolean hasNext() {
		return available() > 0;
	}
	
	public int position(){
		return i;
	}

	public Character next() {
		if(!hasNext()) throw new NoSuchElementException("No more characters available.");
		char c = data.charAt(i++);
		if(build) buildVal.append(c);
		return c;
	}
	
	/**
	 * 
	 * @param i
	 * @return The character as position i.
	 */
	public Character get(int i){
		return data.charAt(i);
	}

	public void remove() {
		i++;
	}
	
	public Character peek(){
		if(!hasNext()) throw new NoSuchElementException("No more characters available.");
		return get(i);
	}
	
	public void seek(int pos){
		i = pos;
	}
	
	public void skip(int len){
		seek(i + len);
	}
	
	public String getString(int len){
		return getString(position(), len);
	}
	
	public String getString(int start, int len){
		int available = Math.max(0, data.length() - start);
		return data.substring(start, start + Math.min(len, available));
	}
	
	public CharIterator clone(){
		CharIterator it = new CharIterator("");
		it.data = data;
		it.i = i;
		return it;
	}
	
	public String toString(){
		return toString(Math.min(available(), 10));
	}
	
	public String toString(int len){
		if(len == 0) return "";
		String res = new String(data.toCharArray(), position(), Math.min(len, available()));
		if(len < available()){
			res += "...";
		}
		res = res.replace("\t", "\\t");
		res = res.replace("\n", "\\n");
		res = res.replace("\r", "\\r");
		return res;
	}
}
