package spiedie.utilities.files.cache;

import java.io.File;

import spiedie.utilities.files.fs.IKVMetadata;

public class FileEntry implements Comparable<FileEntry>, IKVMetadata{
	public String path, name;
	public boolean isDirectory, exists;
	public long length, lastModified;
	
	public FileEntry() {
		
	}
	
	public FileEntry(File f) {
		exists = f.exists();
		isDirectory = f.isDirectory();
		lastModified = f.lastModified();
		length = f.length();
		name = f.getName();
		path = f.getAbsolutePath();
	}
	
	public int compareTo(FileEntry e) {
		if(length == e.length) return 0;
		return length < e.length ? -1 : 1;
	}
	
	public String toString(){
		return path;
	}
	
	public String getKey() {
		return path;
	}

	public long getLength() {
		return length;
	}

	public long getLastModified() {
		return lastModified;
	}
}
