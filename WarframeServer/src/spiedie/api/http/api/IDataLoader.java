package spiedie.api.http.api;

import java.io.IOException;

public interface IDataLoader {
	/**
	 * Loads any number of handlers for the api to use.
	 * @param api
	 */
	public void load(IHttpApi api) throws IOException;
}
