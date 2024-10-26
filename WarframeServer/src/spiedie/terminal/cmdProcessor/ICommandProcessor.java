package spiedie.terminal.cmdProcessor;

import java.util.Map;
import java.util.Set;

public interface ICommandProcessor <T> {
	public Set<String> supported();
	public Map<String, ICommandProcessor<T>> processors();
	public boolean process(String arg);
	public boolean process(String arg, T settings);
	public String description();
	public String help();
}
