package spiedie.utilities.files;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.ArrayList;

import java.util.List;
import java.util.Queue;

import java.util.concurrent.atomic.AtomicLong;

import javax.swing.JOptionPane;

import spiedie.utilities.concurrency.AsyncCallHelper;

import spiedie.utilities.data.structure.StringList;
import spiedie.utilities.files.cache.FileEntry;
import spiedie.utilities.files.cache.FileEntryFilter;
import spiedie.utilities.graphics.progress.ISetProgressable;
import spiedie.utilities.graphics.progress.ProgressHandler;
import spiedie.utilities.graphics.progress.SetProgressable;
import spiedie.utilities.stream.Stream;
import spiedie.utilities.util.log.Log;
import spiedie.utilities.util.persistentInfo.Info;

public class FileUtils {
	public static boolean LIST_PROPERTIES_FILTER_DIRECTORY = false;
	public static boolean folderCopyLog;
	public static boolean fileCopyOverwrite;
	public static boolean logDeletedFiles;
	static{
		try {
			folderCopyLog = Info.isSet("folderCopyLog") && Info.getProperty("folderCopyLog").equals("true");
			fileCopyOverwrite = Info.isSet("fileCopyOverwrite") && Info.getProperty("fileCopyOverwrite").equals("true");
			logDeletedFiles = Info.isSet("logDeletedFiles") && Info.getProperty("logDeletedFiles").equals("true");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private FileUtils(){}
	//------------ path names -------------\\

	/**
	 * 
	 * @param f
	 * @param file
	 * @return
	 */
	public static boolean ensurePathExists(File f, boolean file){
		if(!f.exists()){
			if(file){
				return ensurePathExists(f.getAbsoluteFile().getParentFile(), false);
			} else{
				return f.mkdirs();
			}
		} else return f.isFile() == file;
	}
	
	//------------ file attributes -------------\\

	static class ExistsCheckThread extends AsyncCallHelper<Boolean>{
		private File f;
		
		public void fillResult() {
			result = Boolean.valueOf(f.exists());
		}

		public Boolean getDefaultResult() {
			return Boolean.FALSE;
		}
	}
	
	//------------ file comparison -------------\\
	
	/**
	 * 
	 * @param in
	 * @param length
	 * @param algorithm the algorithm, e.g. MD5, SHA-256...
	 * @param progress
	 * @return
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public static String getHash(InputStream in, long length, String algorithm, boolean progress) throws IOException{
		ISetProgressable p = progress ? new SetProgressable() : null;
		if(p != null) p.setMax(length);
		ProgressHandler h = null;
		if(progress){
			h = new ProgressHandler();
			h.addProgressable(p);
			h.start();
			h.setCanEnd(true);
		}
		String hash = getHash(in, length, algorithm, p);
		if(h != null){
			h.setCanEnd(true);
			h.setFinished(true);
		}
		return hash;
	}
	
	/**
	 * 
	 * @param in
	 * @param length
	 * @param algorithm the algorithm, e.g. MD5, SHA-256...
	 * @param progress
	 * @return
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public static String getHash(InputStream in, long length, String algorithm, ISetProgressable progress) throws IOException{
		try {
			MessageDigest md = MessageDigest.getInstance(algorithm);
			DigestInputStream din = new DigestInputStream(in, md);
			Stream.copy(din, Stream.DRAIN, length, false, progress);
			return getHex(md.digest());
		} catch (NoSuchAlgorithmException e) {
			throw new IOException(e);
		}
	}
	
	/**
	 * 
	 * @param b
	 * @return
	 */
	public static String getHex(byte[] b) {
		if(b == null) return null;
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < b.length; i++) {
			// add 0x100 and remove later to prevent removal of leading zeros in resulting hex string
			result.append(Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1));
		}
		return result.toString();
	}

	//--------------- size/count ------------\\
	
	/**
	 * 
	 * @param f
	 * @return
	 * @throws IOException 
	 */
	public static long foldersize(File f) {
		AtomicLong sum = new AtomicLong();
		traverse(f, (file) -> {
			if(file.isFile()) sum.addAndGet(file.length());
			return true;
		});
		return sum.get();
	}

	/**
	 * 
	 * @param f
	 * @return
	 */
	
	public static void traverse(File f, FileFilter ff) {
		Queue<File> q = new ArrayDeque<>();
		q.add(f);
		while(!q.isEmpty()) {
			File next = q.poll();
			ff.accept(next);
			if(next.isDirectory()) {
				File[] fs = next.listFiles();
				if(fs != null) {
					for(File file : fs) {
						q.add(file);
					}
				}
			}
		}
	}

	/**
	 * 
	 * @param s
	 * @return
	 */
	public static ArrayList<String> deepArrayList(String s){
		return deepArrayList(new File(s));
	}

	/**
	 * 
	 * @param f
	 * @return
	 */
	public static StringList deepArrayList(File f){
		StringList list = new StringList();
		deepFillList(list,f);
		return list;
	}

	/**
	 * 
	 * @param list
	 * @param f
	 */
	public static void deepFillList(List<String> list, File f){
		deepFillListFiltered(list, null, f);
	}

	/**
	 * 
	 * @param list
	 * @param ff
	 * @param f
	 * @return
	 */
	public static StringList deepFillListFiltered(List<String> list, FileFilter ff, File f) {
		StringList tmp = new StringList(ff);
		if(list != null) {
			tmp.addAll(list);
		}
		traverse(f, (file) -> {
			tmp.add(file.getAbsolutePath());
			return true;
		});
		if(list != null){
			list.clear();
			list.addAll(tmp);
		}
		return tmp;
	}
	
	//--------- lists using nio -------\\
	
	/**
	 * 
	 * @param f
	 * @param p
	 * @return
	 * @throws IOException
	 */
	public static ArrayList<String> deepArrayListNIO(File f, final ISetProgressable p) throws IOException {
		final ArrayList<String> list = new ArrayList<String>();
		Files.walkFileTree(f.toPath(), new FileVisitor<Path>() {
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				list.add(dir.toFile().getAbsolutePath());
				return FileVisitResult.CONTINUE;
			}

			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				File f = file.toFile();
				list.add(f.getAbsolutePath());
				if(p != null) p.setMax(p.getMax() + attrs.size());
				return FileVisitResult.CONTINUE;
			}

			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}
		});
		return list;
	}
	
	/**
	 * 
	 * @param f
	 * @param p
	 * @param ff
	 * @return
	 * @throws IOException
	 */
	public static ArrayList<FileEntry> deepArrayListProperties(File f, final ISetProgressable p, final FileEntryFilter ff) throws IOException {
		final ArrayList<FileEntry> list = new ArrayList<FileEntry>();
		Files.walkFileTree(f.toPath(), new FileVisitor<Path>() {
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				FileEntry e = new FileEntry();
				File f = dir.toFile();
				e.isDirectory = attrs.isDirectory();
				e.length = attrs.size();
				e.lastModified = attrs.lastModifiedTime().toMillis();
				e.exists = true;
				e.path = f.getAbsolutePath();
				e.name = f.getName();
				if(!LIST_PROPERTIES_FILTER_DIRECTORY || ff == null || ff.accept(f, attrs)){
					list.add(e);
				}
				return FileVisitResult.CONTINUE;
			}

			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				File f = file.toFile();
				if(ff == null || ff.accept(f, attrs)){
					FileEntry e = new FileEntry();
					e.isDirectory = attrs.isDirectory();
					e.length = attrs.size();
					e.lastModified = attrs.lastModifiedTime().toMillis();
					e.exists = true;
					e.path = f.getAbsolutePath();
					e.name = f.getName();
					list.add(e);
					if(p != null) p.setMax(p.getMax() + e.length);
				}
				return FileVisitResult.CONTINUE;
			}

			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}
		});
		return list;
	}

	//------------ copy -------------\\
	
	/**
	 * 
	 * @param from
	 * @param to
	 * @param syncToMedia
	 * @param p
	 * @return
	 * @throws IOException
	 */
	public static boolean copyFolderProgressed(String from, String to, boolean syncToMedia, ISetProgressable p) throws IOException{
		File f = new File(from);
		if(!f.isFile()){
			FileUtils.ensurePathExists(new File(to), false);
			String[] fs = f.list();
			if(fs != null){
				for(int i = 0; i < fs.length;i++){
					copyFolderProgressed(from+File.separator+fs[i],to+File.separator+fs[i], syncToMedia, p);
				}
			}
		} else if(f.isFile()){
			try{
				if(p != null) p.setFile(from);
				FileUtils.ensurePathExists(new File(to), true);
				InputStream in = Stream.getInputStream(from);
				FileOutputStream out = new FileOutputStream(to);
				Stream.copy(in, out, new File(from).length(), false, p);
				Stream.close(in);
				if(syncToMedia) out.getFD().sync();
				Stream.close(out);
			} catch(Exception e){
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 
	 * @param from
	 * @param to
	 * @param ff
	 * @return
	 * @throws IOException
	 */
	public static boolean copyFolder(String from, String to, FileFilter ff) throws IOException{
		if(!from.endsWith(File.separator)) from += File.separator;
		if(!to.endsWith(File.separator)) to += File.separator;
		File f = new File(from);
		if(f.isDirectory()){
			String[] fs = f.list();
			if(fs != null){
				for(int i = 0; i < fs.length;i++){
					copyFolder(from+fs[i], to+fs[i], ff);
				}
			}
		} else{
			try{
				if(ff == null || ff.accept(new File(from))){
					if(folderCopyLog) Log.write(FileUtils.class, "copy "+from+" to "+to);
					FileUtils.ensurePathExists(new File(to), true);
					FileUtils.copyFile(from, to);
				}
			} catch(Exception e){
				Log.caught(FileUtils.class, e);
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @param from
	 * @param to
	 * @return
	 * @throws IOException
	 */
	public static boolean copyFile(String from, String to) throws IOException{
		File f = new File(from);
		if(!f.isFile()){
			return false;
		}
		int ver = 1;
		if(!fileCopyOverwrite && new File(to).exists()){
			int dot = to.lastIndexOf('.');
			String pre, post;
			if(dot < 0){
				pre = to;
				post = "";
			} else{
				pre = to.substring(0, dot);
				post = to.substring(dot);
			}
			while(new File(pre + "-" + ver + post).exists()){
				ver++;
			}
			to = pre + "-" + ver + post;
		}
		ensurePathExists(new File(to), true);
		InputStream in = Stream.getInputStream(f);
		OutputStream out = Stream.getOutputStream(to);
		byte[] buf = new byte[Stream.DEFAULT_BUFFER_SIZE];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
		new File(to).setLastModified(new File(from).lastModified());
		return true;
	}

	/**
	 * 
	 * @param from
	 * @param to
	 * @return
	 * @throws IOException
	 */
	public static boolean copyFolderIfNotExist(String from, String to) throws IOException{
		for(final String s : FileUtils.deepArrayList(from)){
			if(!new File(s).isDirectory()){
				String toFile = to+"\\"+new File(s).getAbsolutePath().substring((from).length());
				final String toFileF = toFile.replace("\\\\", "\\");
				FileUtils.copyFileIfNotExist(s, toFileF);
			}
		}
		return true;
	}
	
	/**
	 * 
	 * @param from
	 * @param to
	 * @return
	 * @throws IOException
	 */
	public static boolean copyFileIfNotExist(String from, String to) throws IOException{
		return copyFileIfNotExist(from, to, false);
	}
	
	/**
	 * 
	 * @param from
	 * @param to
	 * @param progressed
	 * @return
	 * @throws IOException
	 */
	public static boolean copyFileIfNotExist(String from, String to, boolean progressed) throws IOException{
		if(new File(from).exists() && (!new File(to).exists() || new File(from).length() != new File(to).length())){
			Stream.copyFile(from, to, progressed);
			return true;
		}
		return false;
	}

	//------------ deletion -----------\\
	
	/**
	 * 
	 * @param folder
	 * @param force deletion without asking for confirmation.
	 * @return true if the content of the folder has been deleted
	 */
	public static boolean deleteIn(String folder, boolean force){
		if(new File(folder).exists()){
			boolean del = force || JOptionPane.showConfirmDialog(null, "Delete all in folder:\n"+folder) == 0;
			if(del){
				File f = new File(folder);
				File[] fs = f.listFiles();
				if(fs != null){
					for(int i = 0; i < fs.length;i++){
						delete(fs[i]);
					}
				}
			}
			return del;
		} 
		return false;
	}

	/**
	 * 
	 * @param f
	 * @return
	 */
	public static boolean delete(File f){
		return delete(f, true);
	}

	/**
	 * 
	 * @param f
	 * @param deep
	 * @return
	 */
	public static boolean delete(File f, boolean deep){
		if(!Files.isSymbolicLink(f.toPath()) && deep && f.isDirectory()){
			File[] fs = f.listFiles();
			if(fs != null)
				for(int i = 0; i < fs.length;i++){
					delete(fs[i], deep);
				}
		}
		return deleteFile(f);
	}

	public static boolean deleteFile(File f){
		boolean res = f.delete();
		if(logDeletedFiles) Log.write(FileUtils.class, "deleted "+f);
		return res;
	}

	//------------ runtime folders -------------\\
	
}
