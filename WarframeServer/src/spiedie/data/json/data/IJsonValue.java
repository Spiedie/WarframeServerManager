package spiedie.data.json.data;

public interface IJsonValue extends IJson{
	public String getValue();
	public boolean isString();
	public boolean isNumber();
	public boolean isBoolean();
	public boolean isTrue();
	public boolean isFalse();
}
