package spiedie.api.http.api;

public interface ISpHttpServerMetrics {
	public String getName();
	public void addRequestReceived(ISpHttpRequest r);
	public void addRequestClosed(ISpHttpRequest r);
}
