package spiedie.data.json.data;

public interface IJson {
	public int getType();

	/**
	 * 
	 * @return this IJson as JsonArray, or null if this IJson does not represent a JsonArray.
	 */
	public IJsonArray toJsonArray();
	
	/**
	 * 
	 * @return this IJson as JsonObject, or null if this IJson does not represent a JsonObject.
	 */
	public IJsonObject toJsonObject();

	/**
	 * 
	 * @return this IJson as IJsonValue, or null if this IJson does not represent a JsonValue.
	 */
	public IJsonValue toJsonValue();

	/**
	 * 
	 * @return a String representation of this IJson, following the IJson format.
	 */
	public String toJson();
}
