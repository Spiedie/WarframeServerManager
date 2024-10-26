package spiedie.data.json.parser;

import spiedie.data.json.Json;
import spiedie.data.json.JsonEscapeMethods;
import spiedie.data.json.JsonParseUtils;
import spiedie.data.json.data.IJsonValue;
import spiedie.utilities.data.StringUtils;

public class JsonValue extends Json implements IJsonValue{
	public static boolean TO_JSON_QUOTE_NUMBER = true;
	public static boolean TO_JSON_QUOTE_BOOLEAN = true;
	private String value;
	private boolean isBoolean, isNumber;
	
	public JsonValue(String value) {
		this(value, true);
	}
	
	public JsonValue(String value, boolean unescape) {
		if(unescape) value = JsonEscapeMethods.unescape(value);
		this.value = value;
	}
	
	public int getType() {
		return Json.VALUE;
	}
	
	public String getValue() {
		return value;
	}
	
	public boolean isString() {
		throw new UnsupportedOperationException();
	}
	
	public boolean isNumber() {
		return isNumber;
	}
	
	public boolean isBoolean() {
		return isBoolean;
	}
	
	public boolean isTrue() {
		throw new UnsupportedOperationException();
	}
	
	public boolean isFalse() {
		throw new UnsupportedOperationException();
	}
	
	public String toJson(){
		String val = getValue();
		boolean isNumber = StringUtils.keep(val, "+-."+StringUtils.DIGITS).equals(val);
		boolean isBoolean = val.equals("true") || val.equals("false");
		boolean isOther = !isNumber && !isBoolean;
		boolean quote = val.isEmpty() || (TO_JSON_QUOTE_NUMBER && isNumber) || (TO_JSON_QUOTE_BOOLEAN && isBoolean) || isOther;
		StringBuilder sb = new StringBuilder();
		if(quote) sb.append(JsonParseUtils.QUOTE);
		sb.append(JsonEscapeMethods.escape(val));
		if(quote) sb.append(JsonParseUtils.QUOTE);
		return sb.toString();
	}
	
	public String toString() {
		return "\""+getValue()+"\"";
	}
}
