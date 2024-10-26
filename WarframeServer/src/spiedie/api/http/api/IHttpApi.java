package spiedie.api.http.api;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface IHttpApi {
	public String getIp();
	public int getPort();
	public long getRequestTimeout();
	public void load() throws IOException;
	public List<IDataLoader> getLoaders();
	public Map<String, ISpHttpHandler> getHandlers();
	
	public void start();
	public void setFinished(boolean finished);
	public boolean isFinished();
}
