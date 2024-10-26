package spiedie.utilities.files.cache;

import java.io.File;
import java.nio.file.attribute.BasicFileAttributes;

public interface FileEntryFilter{
	public boolean accept(File f, BasicFileAttributes attrs);
}
