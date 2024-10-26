package spiedie.utilities.data.structure;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

public class StringList extends ArrayList<String> implements FileFilter{
	private static final long serialVersionUID = -3387080157759007421L;
	private FileFilter filter = this;
	
	/**
	 * 
	 */
	public StringList(){
		this(1024);
	}
	
	/**
	 * 
	 * @param initialCapacity
	 */
	public StringList(int initialCapacity){
		super(initialCapacity);
	}
	
	/**
	 * 
	 * @param filter
	 */
	public StringList(FileFilter filter){
		this();
		setFilter(filter);
	}
	
	/**
	 * 
	 * @param filter
	 */
	public void setFilter(FileFilter filter){
		if(filter != null) {
			this.filter = filter;
		}
	}
	
	/**
	 * 
	 * @param f
	 */
	
	public boolean accept(File pathname) {
		boolean accept = filter == this || filter.accept(pathname);
		if(accept) add(pathname.getAbsolutePath());
		return accept;
	}
}
