package spiedie.utilities.files.prep;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import spiedie.utilities.files.FileUtils;
import spiedie.utilities.files.cache.FileEntry;
import spiedie.utilities.graphics.progress.ISetProgressable;
import spiedie.utilities.graphics.progress.ProgressHandler;
import spiedie.utilities.graphics.progress.SetProgressable;
import spiedie.utilities.stream.Stream;
import spiedie.utilities.util.log.Log;

public class FileCopyPrep {
	private List<IPrepTask> list;
	private boolean showProcessed, showProgress, syncToMedia;
	private long progressDelay;
	public FileCopyPrep(){
		list = new ArrayList<>();
		syncToMedia = true;
	}
	
	public static interface IPrepTask extends Comparable<IPrepTask>{
		public String getName();
		public int getPriority();
		public long getLength();
		public void execute(boolean syncToMedia, ISetProgressable p) throws IOException;
	}
	
	static class PrepTask implements IPrepTask{
		public static final int DELETE_FILE = 0;
		public static final int DELETE_FOLDER = 1;
		public static final int COPY_FILE = 2;
		public static final int COPY_FOLDER = 3;
		public static final int COPY_IF_NOT_EXISTS = 4;
		public static final int COPY_FOLDER_IF_NOT_EXISTS = 5;
		public int priority, operation;
		public FileEntry from, to;
		
		public PrepTask(String from, String to, int op, int priority){
			if(from != null) this.from = new FileEntry(new File(from));
			if(to != null) this.to = new FileEntry(new File(to));
			this.operation = op;
			this.priority = priority;
		}
		
		public String getName() {
			return from.path;
		}
		
		public int compareTo(IPrepTask t) {
			return t.getPriority() - this.getPriority();
		}

		public int getOperation() {
			return operation;
		}
		
		public int getPriority() {
			return priority;
		}

		public long getLength() {
			switch(getOperation()) {
			case DELETE_FILE: return 0;
			case DELETE_FOLDER: return 0;
			case COPY_FILE: return from.length;
			case COPY_IF_NOT_EXISTS: return from.length;
			case COPY_FOLDER: return FileUtils.foldersize(new File(from.path));
			case COPY_FOLDER_IF_NOT_EXISTS: return FileUtils.foldersize(new File(from.path));
			}
			return 0;
		}

		public void execute(boolean syncToMedia, ISetProgressable p) throws IOException{
			if(getOperation() == PrepTask.DELETE_FILE){
				p.setFile("Delete "+from);
				new File(from.path).delete();
			} else if(getOperation() == PrepTask.DELETE_FOLDER){
				p.setFile("Delete in "+from);
				FileUtils.delete(new File(from.path));
			} else if(getOperation() == PrepTask.COPY_FILE){
				p.setFile("Copy "+from);
				FileUtils.ensurePathExists(new File(to.path), true);
				InputStream in = Stream.getInputStream(from.path);
				OutputStream out = Stream.getOutputStream(to.path);
				Stream.copy(in, out, Long.MAX_VALUE, true, p);
			} else if(getOperation() == PrepTask.COPY_FOLDER){
				p.setFile("Copy from "+from);
				FileUtils.copyFolderProgressed(from.path, to.path, syncToMedia, p);
			} else if(getOperation() == PrepTask.COPY_IF_NOT_EXISTS){
				p.setFile("CopyIfNExist "+from);
				if(!to.exists || from.length != to.length){
					InputStream in = Stream.getInputStream(from.path);
					FileUtils.ensurePathExists(new File(to.path), true);
					OutputStream out = Stream.getOutputStream(to.path);
					Stream.copy(in, out, Long.MAX_VALUE, true, p);
				} else{
					p.addValue(from.length);
				}
			} else if(getOperation() == PrepTask.COPY_FOLDER_IF_NOT_EXISTS){
				p.setFile("CopyIfNExist in "+from);
				FileUtils.copyFolderIfNotExist(from.path, to.path);
				p.addValue(FileUtils.foldersize(new File(from.path)));
			}
		}
		
		public String toString(){
			StringBuilder sb = new StringBuilder();
			sb.append("priority = "+priority+", ");
			try{
				Field[] fs = PrepTask.class.getFields();
				for(Field f : fs){
					if(!f.getName().equalsIgnoreCase("priority") && !f.getName().equalsIgnoreCase("operation") && f.getType().equals(int.class)){
						f.setAccessible(true);
						int x = f.getInt(this);
						if(x == operation) sb.append("Operation = "+f.getName()+", ");
					}
				}
			} catch(Throwable t){
				Log.caught(this, t);
			}
			sb.append("file = "+from);
			return sb.toString();
		}
	}
	
	// methods to add tasks
	
	public void addCopyFolder(String from, String to, int priority){
		list.add(new PrepTask(from, to, PrepTask.COPY_FOLDER, priority));
	}
	
	public void addDeleteFolder(String file, int priority){
		list.add(new PrepTask(file, null, PrepTask.DELETE_FOLDER, priority));
	}
	
	// starting
	public void start() throws IOException{
		ProgressHandler h = null;
		final ISetProgressable p = new SetProgressable();
		if(showProgress) {
			h = new ProgressHandler();
			h.addProgressable(p);
			if(progressDelay == 0) {
				h.start();
			} else {
				throw new IllegalArgumentException("Delayed start not supported.");
			}
		}
		for(IPrepTask t : list){
			if(showProcessed) Log.write("Prep "+t);
			p.setFile(t.getName());
			p.setMax(p.getMax() + t.getLength());
		}
		p.setFile("Sorting files...");
		Collections.sort(list);
		for(IPrepTask t : list){
			if(showProcessed) Log.write("Process "+t);
			t.execute(syncToMedia, p);
		}
		p.setFile("");
		if(showProgress) {
			h.setCanEnd(true);
			h.setFinished(true);
		}
	}
}
