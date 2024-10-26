package spiedie.utilities.graphics.progress;

public interface ISetProgressable extends Progressable{
	void setValue(long val);
	void setMax(long val);
	void addValue(long val);
	void setFile(String file);
}
