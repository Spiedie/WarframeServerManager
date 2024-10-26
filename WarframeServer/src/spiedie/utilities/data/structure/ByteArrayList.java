package spiedie.utilities.data.structure;

import java.util.Arrays;

public class ByteArrayList{
	private byte[] data;
	private int size;

	/**
	 * 
	 */
	public ByteArrayList(){
		data = new byte[10];
		size = 0;
	}

	/**
	 * 
	 * @param initialCapacity
	 */
	public ByteArrayList(int initialCapacity){
		data = new byte[initialCapacity];
		size = 0;
	}

	/**
	 * 
	 * @return
	 */
	public int size(){
		return size;
	}
	/**
	 * 
	 * @param x
	 */
	public void ensureCapacity(int x){
		if(x >= data.length){
			data = Arrays.copyOf(data, x + 1);
		}
	}
	
	/**
	 * 
	 * @param buf
	 * @param off
	 * @param len
	 */
	public void add(byte[] buf, int off, int len){
		if(size + len > data.length) ensureCapacity(size + size / 2 + len);
		System.arraycopy(buf, off, data, size, len);
		size += len;
	}

	/**
	 * 
	 * @return
	 */
	public byte[] toArray() {
		byte[] b = new byte[size()];
		System.arraycopy(data, 0, b, 0, b.length);
		return b;
	}

	public String toString(){
		if(size() == 0) {
			return "ByteArrayList[]";
		}
		StringBuilder res = new StringBuilder("ByteArrayList[" + data[0]);
		for(int i = 1;i < size(); i++){
			res.append("," + data[i]);
		}
		return res + "]";
	}
}
