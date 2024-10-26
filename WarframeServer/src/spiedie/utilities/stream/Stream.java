package spiedie.utilities.stream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;

import spiedie.utilities.data.structure.ByteArrayList;
import spiedie.utilities.files.FileUtils;
import spiedie.utilities.graphics.progress.ISetProgressable;
import spiedie.utilities.graphics.progress.ProgressHandler;
import spiedie.utilities.graphics.progress.SetProgressable;
import spiedie.utilities.util.Time;
import spiedie.utilities.util.log.Log;
public class Stream{
	
	/**
	 * Utilities for data streams.
	 */
	private Stream(){}
//	
	public static final String charset = "UTF-8";
	public static final int DEFAULT_BUFFER_SIZE = 1 << 18;

	public static InputStream TAP = new InputStream() {
		public int read() throws IOException {
			return 0;
		}

		public int read(byte[] buf) throws IOException {
			return read(buf, 0, buf.length);
		}

		public int read(byte[] buf, int off, int len) throws IOException {
			return len;
		}
	};

	public static OutputStream DRAIN = new OutputStream() {
		public void write(int b) throws IOException {}
		public void write(byte[] buf) throws IOException {}
		public void write(byte[] buf, int off, int len) throws IOException {}
	};

	//stream getters
	/**
	 * 
	 * @param name
	 * @return
	 * @throws IOException
	 */
	public static BufferedInputStream getInputStream(String name) throws IOException{
		if(name.startsWith("http")){
			return getInputStream(new URL(name));
		} else{
			return getInputStream(new File(name));
		}
	}
	
	/**
	 * 
	 * @param file
	 * @return
	 * @throws FileNotFoundException
	 */
	public static BufferedInputStream getInputStream(File file) throws FileNotFoundException{
		return new BufferedInputStream(new FileInputStream(file), DEFAULT_BUFFER_SIZE);
	}
	
	/**
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public static BufferedInputStream getInputStream(URL url) throws IOException{
		return new BufferedInputStream(url.openStream(), DEFAULT_BUFFER_SIZE);
	}
	
	/**
	 * 
	 * @param in
	 * @return
	 */
	public static BufferedInputStream getInputStream(InputStream in){
		return new BufferedInputStream(in, DEFAULT_BUFFER_SIZE);
	}
	
	/**
	 * 
	 * @param name
	 * @return
	 * @throws FileNotFoundException
	 */
	public static BufferedOutputStream getOutputStream(String name) throws FileNotFoundException{
		return getOutputStream(new File(name));
	}
	
	/**
	 * 
	 * @param file
	 * @return
	 * @throws FileNotFoundException
	 */
	public static BufferedOutputStream getOutputStream(File file) throws FileNotFoundException{
		return new BufferedOutputStream(new FileOutputStream(file), DEFAULT_BUFFER_SIZE);
	}
	
	/**
	 * 
	 * @param out
	 * @return
	 */
	public static BufferedOutputStream getOutputStream(OutputStream out){
		return new BufferedOutputStream(out, DEFAULT_BUFFER_SIZE);
	}
	
	/**
	 * 
	 * @param out
	 * @return a PrintWriter that only writes a newline on println
	 */
	public static PrintWriter getPrintWriter(OutputStream out) {
		return new PrintWriter(new OutputStreamWriter(out, Charset.forName(Stream.charset)), false) {
			public void println() {
				write("\n");
			}
		};
	}
	
	// help methods for reading/writing files
	/**
	 * 
	 * @param f
	 * @return
	 * @throws IOException
	 */
	public static byte[] read(File f) throws IOException{
		return read(f, (int)f.length());
	}
	
	/**
	 * 
	 * @param f
	 * @param length
	 * @return
	 * @throws IOException
	 */
	public static byte[] read(File f, int length) throws IOException{
		ByteArrayList list = new ByteArrayList(length);
		InputStream in = Stream.getInputStream(f);
		byte[] buf = new byte[DEFAULT_BUFFER_SIZE];
		int len = 0;
		while(list.size() < length && (len = in.read(buf, 0, Math.min(buf.length, length - list.size()))) > 0){
			list.add(buf, 0, len);
		}
		in.close();
		byte[] res = list.toArray();
		if(res.length > length) {
			res = Arrays.copyOf(res, length);
		}
		return res;
	}
	
	/**
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public static byte[] read(InputStream in) throws IOException{
		byte[] buf = new byte[DEFAULT_BUFFER_SIZE];
		ByteArrayList list = new ByteArrayList();
		int len = 0;
		while((len = in.read(buf)) != -1){
			list.add(buf, 0, len);
		}
		if(in.available() > 0){
			Log.err(Stream.class, "Stream still has bytes left");
		}
		close(in);
		return list.toArray();
	}
	
	/**
	 * 
	 * @param f
	 * @return
	 * @throws IOException
	 */
	public static String readToEOF(String f) throws IOException{
		return readToEOF(new File(f));
	}
	
	/**
	 * 
	 * @param f
	 * @return
	 * @throws IOException
	 */
	public static String readToEOF(File f) throws IOException{
		InputStream in = Stream.getInputStream(f);
		String res = readToEOF(in);
		in.close();
		return res;
	}
	
	/**
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public static String readToEOF(InputStream in) throws IOException{
		return readToEOF(in, charset);
	}
	
	/**
	 * 
	 * @param in
	 * @param charset
	 * @return
	 * @throws IOException
	 */
	public static String readToEOF(InputStream in, String charset) throws IOException{
		StringBuilder sb = new StringBuilder();
		byte[] buf = new byte[DEFAULT_BUFFER_SIZE];
		int len = 0;
		while((len = in.read(buf)) != -1){
			sb.append(new String(buf, 0, len, charset));
		}
		return sb.toString();
	}
	
	/**
	 * 
	 * @param c
	 * @return
	 */
	public static boolean close(Closeable c){
		if(c != null)
			try{
				c.close();
				return true;
			} catch(IOException e){
				Log.caught(Stream.class, e);
			}
		return false;
	}
	
	/**
	 * 
	 * @param in
	 * @param f
	 * @throws IOException
	 */
	public static void writeToFile(String in, File f) throws IOException{
		writeToFile(in.toString().getBytes(charset), f);
	}
	
	/**
	 * 
	 * @param buf
	 * @param f
	 * @throws IOException
	 */
	private static void writeToFile(byte[] buf, File f) throws IOException{
		FileUtils.ensurePathExists(f.getAbsoluteFile(), true);
		OutputStream out = getOutputStream(f);
		out.write(buf);
		out.close();
		f.setLastModified(Time.millis());
	}
	
	/**
	 * 
	 * @param from
	 * @param to
	 * @param progressed
	 * @throws IOException
	 */
	public static void copyFile(String from, String to, boolean progressed) throws IOException{
		copyFile(from, to, Long.MAX_VALUE, progressed);
	}
	
	/**
	 * 
	 * @param from
	 * @param to
	 * @param length
	 * @param progressed
	 * @throws IOException
	 */
	public static void copyFile(String from, String to, long length, boolean progressed) throws IOException{
		FileUtils.ensurePathExists(new File(to), true);
		InputStream in = Stream.getInputStream(from);
		OutputStream out = Stream.getOutputStream(to);
		SetProgressable p = null;
		ProgressHandler h = null;
		if(progressed){
			h = new ProgressHandler();
			p = new SetProgressable();
			h.addProgressable(p);
			p.setMax(new File(from).length());
			p.setFile(from);
			h.start();
		}
		copy(in, out, length, true, p);
		if(progressed){
			h.setCanEnd(true);
			h.setFinished(true);
		}
	}
	
	/**
	 * 
	 * @param in
	 * @param out
	 * @param max
	 * @param close
	 * @param progress
	 * @return
	 * @throws IOException
	 */
	public static long copy(InputStream in, OutputStream out, long max, boolean close, ISetProgressable progress) throws IOException{
		int len;
		long done = 0;
		byte[] buf = new byte[Stream.DEFAULT_BUFFER_SIZE];
		while((max == Long.MAX_VALUE || max > done) && (len = in.read(buf, 0, (int) Math.min(buf.length, max - done))) != -1){
			out.write(buf, 0, len);
			out.flush();
			done += len;
			if(progress != null) progress.addValue(len);
		}
		out.flush();
		if(close){
			close(in);
			close(out);
		}
		return done;
	}
	
	// help methods for reading/writing streams
	/**
	 * 
	 * @param in
	 * @param buf
	 * @param off
	 * @param length
	 * @return the number of byte read.
	 * @throws IOException
	 */
	public static int readBlockingStrict(InputStream in, byte[] buf, int off, int length) throws IOException{
		if(length == 0) return 0;
		int pos = off;
		// until length bytes are read
		while (pos < length + off) {
			// starting at current location, read until the end of the buffer
			int len = 0;
			try{
				len = in.read(buf, pos, off - pos + length);
			} catch(IndexOutOfBoundsException e){
				Log.err(Stream.class, "buf.length = "+buf.length+", pos = "+pos+", off = "+off+", length = "+length);
				throw e;
			}
			if(len == -1) break;
			pos += len;
		}
		int read = pos - off;
		return read == 0 ? -1 : read;
	}

	/**
	 * 
	 * @param in
	 * @param buf
	 * @param off
	 * @param length
	 * @param timeout
	 * @return
	 * @throws IOException
	 */
	public static int readBlocking(InputStream in, byte[] buf, int off, int length, long timeout) throws IOException{
		if(length == 0) return 0;
		long time = timeout == -1 ? -1 : Time.millis();
		int pos = off;
		// until length bytes are read
		while (pos < length + off) {
			// starting at current location, read until the end of the buffer
			int len = in.available() == 0 ? 0 : in.read(buf, pos, off - pos + length);
			if(len == -1) break;
			pos += len;
			// avoid high cpu when no data available
			if(len == 0){
				if(timeout != -1 && Time.millis() - time > timeout){
					break;
				}
				Time.sleep(1);
			} else if(timeout != -1) {
				time = Time.millis();
			}
		}
		int read = pos - off;
		return read == 0 ? -1 : read;
	}
	
	/**
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public static String readString(InputStream in) throws IOException{
		return readString(in, -1, -1);
	}
	
	/**
	 * 
	 * @param in
	 * @param readLimit
	 * @return
	 * @throws IOException
	 */
	public static String readString(InputStream in, int readLimit, long readTimeout) throws IOException{
		int len = readInt(in);
		if(len == -1) return null;
		else {
			if(readLimit != -1 && len > readLimit) {
				throw new IOException("Read of "+len+" breaks the limit of "+readLimit+".");
			}
			byte[] buf = new byte[len];
			int res = readBlocking(in, buf, 0, len, readTimeout);
			if(res != len) throw new IOException("readString format error.");
			return new String(buf, "UTF-8");
		}
	}
	
	/**
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public static int readInt(InputStream in) throws IOException{
		return (int) readBytesToValue(in, 4);
	}

	// read maximum of 8 bytes
	/**
	 * 
	 * @param in
	 * @param bytes
	 * @return
	 * @throws IOException
	 */
	public static long readBytesToValue(InputStream in, int bytes) throws IOException{
		byte[] buf = new byte[bytes];
		int len = Stream.readBlockingStrict(in, buf, 0, bytes);
		if(len != bytes) throw new IOException("Could not read required data.");
		return readBytesToValue(buf, bytes);
	}
	
	/**
	 * 
	 * @param buf
	 * @param bytes
	 * @return
	 */
	public static long readBytesToValue(byte[] buf, int bytes){
		return readBytesToValue(buf, 0, bytes);
	}
	
	/**
	 * Read a value from the given bytes.
	 * @param buf
	 * @param off
	 * @param bytes the number of bytes to write.
	 * @return
	 */
	public static long readBytesToValue(byte[] buf, int off, int bytes){
		long res = 0;
		for (int i = 0; i < bytes;i++){
			res = (res << 8);
			res += (buf[i + off] & 0xFF);
		}
		return res;
	}
	
	/**
	 * 
	 * @param out
	 * @param s
	 * @throws IOException
	 */
	public static void writeString(OutputStream out, String s) throws IOException{
		if(s == null) {
			writeInt(out, -1);
		} else {
			byte[] buf = s.getBytes("UTF-8");
			writeInt(out, buf.length);
			out.write(buf);
		}
		out.flush();
	}
	
	/**
	 * 
	 * @param out
	 * @param x
	 * @throws IOException
	 */
	public static void writeInt(OutputStream out,int x) throws IOException{
		writeBytesFromValue(out, x, 4);
	}

	/**
	 * 
	 * @param out
	 * @param x
	 * @param bytes
	 * @throws IOException
	 */
	public static void writeBytesFromValue(OutputStream out, long x, int bytes) throws IOException{
		byte[] data = new byte[bytes];
		writeBytesFromValue(data, x, bytes);
		out.write(data);
	}
	
	/**
	 * write value <tt>x</tt> as individual bytes to <tt>buf</tt>.
	 * @param data
	 * @param x
	 * @param bytes
	 */
	public static void writeBytesFromValue(byte[] data, long x, int bytes){
		writeBytesFromValue(data, 0, x, bytes);
	}
	
	/**
	 * write value <tt>x</tt> as individual bytes to <tt>data</tt> starting at offset <tt>off</tt>.
	 * @param data
	 * @param off
	 * @param x
	 * @param bytes
	 */
	public static void writeBytesFromValue(byte[] data, int off, long x, int bytes){
		for (int i = bytes-1; i >= 0;i--){
			data[off + i] = (byte)(x & 0xFF);
			x = x >> 8;
		}
	}
	
}
