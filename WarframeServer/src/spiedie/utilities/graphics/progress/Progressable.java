package spiedie.utilities.graphics.progress;

import spiedie.utilities.util.ISettings;

public interface Progressable {
	long getValue();
	long getMax();
	String getRequestedFile();
	ISettings getSettings();
}
