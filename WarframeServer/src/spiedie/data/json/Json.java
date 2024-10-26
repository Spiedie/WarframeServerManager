package spiedie.data.json;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Objects;

import spiedie.data.json.data.IJson;
import spiedie.data.json.data.IJsonArray;
import spiedie.data.json.data.IJsonObject;
import spiedie.data.json.data.IJsonValue;
import spiedie.data.json.parser.JsonFactory;
import spiedie.data.json.parser.JsonParserFactory;
import spiedie.utilities.data.StringUtils;
import spiedie.utilities.files.FileUtils;
import spiedie.utilities.stream.Stream;
import spiedie.utilities.util.log.Log;

public abstract class Json implements IJson{
	public static final int UNKNOWN = 0;
	public static final int OBJECT = 1;
	public static final int ARRAY = 2;
	public static final int VALUE = 3;
	public static final int ERROR = 4;
	
	private static final IJsonFactory factory = new JsonFactory();
	private static final IJsonParserFactory parserFactory = new JsonParserFactory();
	
	public static boolean logActivity = false;
	
	/**
	 * 
	 * @return this Json as JsonArray, or null if this Json does not represent a JsonArray.
	 */
	public IJsonArray toJsonArray() {
		return (this instanceof IJsonArray) ? (IJsonArray) this : null;
	}
	
	/**
	 * 
	 * @return this Json as JsonObject, or null if this Json does not represent a JsonObject.
	 */
	public IJsonObject toJsonObject() {
		return (this instanceof IJsonObject) ? (IJsonObject) this : null;
	}

	/**
	 * 
	 * @return this Json as JsonValue, or null if this Json does not represent a JsonValue.
	 */
	public IJsonValue toJsonValue() {
		return (this instanceof IJsonValue) ? (IJsonValue) this : null;
	}

	/**
	 * 
	 * @return a String representation of this Json, following the Json format.
	 */
	public abstract String toJson();

	public static IJsonObject object() {
		return factory.object();
	}
	
	public static IJsonArray array() {
		return factory.array();
	}
	
	public static IJsonValue value(String value) {
		return factory.value(value);
	}
	
	public static IJson parse(String json){
		return parse(json, false);
	}
	
	public static IJson parse(String json, boolean useLog){
		IJsonParser p = parserFactory.create(Objects.requireNonNull(json));
		p.setUseLog(useLog);
		if(useLog) Log.write(p, "Parse "+json);
		IJson j = p.parse();
		if(useLog) Log.write(p, j);
		return j;
	}
	
	public static String toJsonReadable(IJson j){
		String json = j.toJson();
		return toJsonReadable(json);
	}
	
	public static String toJsonReadable(String json){
		StringBuilder sb = new StringBuilder();
		int brace = 0;
		boolean quote = false;
		char[] cs = json.toCharArray();
		for(int i = 0; i < cs.length;i++){
			char c = cs[i];
			if(c == '\"') quote = !quote;
			if(c == '}'){
				brace--;
				sb.append("\n");
				sb.append(StringUtils.makeToSize("", '\t', brace));
			}
			sb.append(c);
			if(c == ',' && !quote){
				sb.append("\n");
				sb.append(StringUtils.makeToSize("", '\t', brace));
			} 
			if(c == '{'){
				brace++;
				sb.append("\n");
				sb.append(StringUtils.makeToSize("", '\t', brace));
			}
		}
		return sb.toString();
	}
	
	public static IJson read(InputStream in) throws IOException{
		if(logActivity) Log.write(Json.class, "read input");
		String s = Stream.readString(in);
		if(logActivity) Log.write(Json.class, "read "+s+", parsing");
		return Json.parse(s);
	}
	
	public static IJsonObject readObject(InputStream in) throws IOException{
		IJson j = read(in);
		if(j == null) return null;
		else return j.toJsonObject();
	}
	
	public static void write(OutputStream out, IJson j) throws IOException{
		write(out, j, false);
	}

	public static void write(OutputStream out, IJson j, boolean flush) throws IOException{
		String json = j.toJson();
		if(logActivity) Log.write(Json.class, "Writing "+json);
		Stream.writeString(out, json);
		if(flush) out.flush();
	}
	
	/**
	 * Provides a hash of the given Json.
	 * This string is not guaranteed to be unique for different Json objects.
	 * @param a
	 * @return
	 * @throws IOException
	 */
	public static String hash(IJson a) throws IOException{
		String json = a.toJson();
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < json.length();i++){
			if(!Character.isWhitespace(json.charAt(i))){
				sb.append(json.charAt(i));
			}
		}
		json = sb.toString();
		byte[] buf = json.getBytes("UTF-8");
		Arrays.sort(buf);
		return FileUtils.getHash(new ByteArrayInputStream(buf), buf.length, "SHA-256", false);
	}
	
	public static String getString(IJson j, String path){
		IJson res = get(j, VALUE, path.split("\\."));
		if(res == null) return null;
		String value = res.toJsonValue().getValue();
		if(value == null || value.equals("null")) return null;
		return value;
	}
	
	public static IJsonObject getObject(IJson j, String path){
		IJson res = get(j, Json.OBJECT, path);
		return res == null ? null : res.toJsonObject();
	}
	
	public static IJsonArray getArray(IJson j, String path){
		IJson res = get(j, Json.ARRAY, path);
		return res == null ? null : res.toJsonArray();
	}
	
	public static IJson get(IJson j, int type, String path){
		return get(j, type, path.split("\\."));
	}
	
	/**
	 * Find a Json in the given Json, usig the given path.
	 * "a.b.c=d.e", where a, b, (c=d) are objects, c is a key and d is a value.
	 * The method (c=d) is used on an array of objects.
	 * @param j
	 * @param type
	 * @param path
	 * @return The found Json, or null if the path did not resolve to Json.
	 */
	public static IJson get(IJson j, int type, String... path){
		if(path == null || path.length == 0) return null;
		for(int i = 0; i < path.length;i++){
			if(j != null){
				if(j.getType() == Json.ARRAY){
					IJsonArray a = j.toJsonArray();
					int is = path[i].indexOf('=');
					if(is < 0){
						String num = StringUtils.keep(path[i], StringUtils.DIGITS);
						if(num.isEmpty()) return null;
						int idx = Integer.parseInt(num);
						if(!(0 <= idx && idx < a.size())) return null;
						j = a.get(idx);
					} else{
						String key = path[i].substring(0, is);
						String value = path[i].substring(is + 1, path[i].length());
						for(IJson item : a){
							IJsonObject itemOject = item.toJsonObject();
							if(itemOject != null && itemOject.isSet(key) && itemOject.getProperty(key).contains(value)){
								j = item;
								break;
							}
						}
					}
				} else if(j.getType() == Json.OBJECT){
					IJsonObject o = j.toJsonObject();
					if(o.isSet(path[i])) {
						j = o.getJson(path[i]);
					}
					else{
						return null;
					}
				} else if(j.getType() == Json.VALUE){
					if((type == Json.VALUE || type == Json.UNKNOWN) && i == path.length - 1){
						return j.toJsonValue();
					} else{
						return null;
					}
				}
			}
		}
		if(j != null && (type != Json.UNKNOWN && j.getType() != type)) return null;
		return j;
	}
}
