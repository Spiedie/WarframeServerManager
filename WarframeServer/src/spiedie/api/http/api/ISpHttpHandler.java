package spiedie.api.http.api;

import java.io.IOException;

public interface ISpHttpHandler {
	public void handle(ISpHttpRequest r) throws IOException;
}
