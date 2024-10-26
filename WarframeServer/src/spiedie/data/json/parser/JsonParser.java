package spiedie.data.json.parser;

import spiedie.data.json.IJsonParser;
import spiedie.data.json.Json;
import spiedie.data.json.JsonError;
import spiedie.data.json.JsonParseUtils;
import spiedie.data.json.data.IJson;
import spiedie.data.json.data.IJsonArray;
import spiedie.data.json.data.IJsonObject;
import spiedie.utilities.data.CharIterator;
import spiedie.utilities.util.log.Log;

public class JsonParser extends JsonParseUtils implements IJsonParser{

	public JsonParser(CharIterator it) {
		this.it = it;
	}
	
	public IJson parse(){
		if(!it.hasNext()) return null;
		skipWhitespace();
		if(isObject()) return parseObject();
		if(isArray()) return parseArray();
		if(isValue()) return parseValue();
		return new JsonError("No valid start of json: "+it.peek()+" at index "+it.position()+" => "+errorContext());
	}
	
	private IJson parseValue(){
		skipWhitespace();
		if(useLog) Log.write(this, "parseValue "+it);
		String val = readValue();
		skipWhitespace();
		return val == null ? new JsonError("error reading value => "+errorContext()) : new JsonValue(val);
	}
	
	private IJson parseObject(){
		skipWhitespace();
		if(useLog) Log.write(this, "parseObject");
		IJsonObject o = new JsonObject();
		if(it.next() != CURLY_OPEN) return new JsonError("Object has to start with "+CURLY_OPEN+" => "+errorContext());
		skipWhitespace();
		while(it.peek() != CURLY_CLOSE){
			skipWhitespace();
			if(useLog) Log.write(this, "parse object item "+ (o.size() + 1)+", avail = "+it.available()+", it = "+it);
			String key = readString();
			skipWhitespace();
			if(useLog) Log.write(this, "got key "+ (o.size() + 1)+": "+key);
			if(!it.hasNext() || it.next() != COLON) return new JsonError("No colon between key:value in Object at "+it.position()+" => "+errorContext());
			IJson value = parse();
			if(useLog) Log.write(this, "got value "+value);
			if(value.getType() != Json.ERROR) o.putJson(key, value);
			skipWhitespace();
			if(useLog) Log.write(this, "end of object item "+o.size()+" search, it = "+it);
			if(it.peek() != CURLY_CLOSE && it.next() != COMMA) return new JsonError("No comma between object elements in Object at "+it.position()+" => "+errorContext());
			if(useLog) Log.write(this, "end of object item "+o.size()+", it = "+it);
		}
		if(it.peek() == CURLY_CLOSE) it.next();
		if(useLog) Log.write(this, "end of object: size = "+o.size()+", next char = "+(it.hasNext() ? it.peek() : "empty"));
		skipWhitespace();
		return o;
	}
	
	private IJson parseArray(){
		if(useLog) Log.write(this, "parseArray "+it);
		IJsonArray a = Json.array();
		if(it.next() != BRACE_OPEN) return new JsonError("Array has to start with "+BRACE_OPEN+" => "+errorContext());
		skipWhitespace();
		while(it.peek() != BRACE_CLOSE){
			skipWhitespace();
			if(useLog) Log.write(this, "parse array item "+(a.size()+1)+", avail = "+it.available()+", it = "+it);
			IJson value = parse();
			if(useLog) Log.write(this, "got value "+value);
			if(value.getType() != Json.ERROR) a.add(value);
			skipWhitespace();
			if(useLog) Log.write(this, "end of array item "+a.size()+" search, it = "+it);
			if(it.peek() != BRACE_CLOSE && it.next() != COMMA) return new JsonError("No comma between array elements in Array at "+it.position()+" => "+errorContext());
			if(useLog) Log.write(this, "end of array item "+a.size()+", it = "+it);
		}
		if(it.peek() == BRACE_CLOSE) it.next();
		if(useLog) Log.write(this, "end of array: size = "+a.size());
		skipWhitespace();
		return a;
	}
	
	public String readValue(){
		char c = it.peek();
		if(useLog) Log.write(this, "read value");
		if(c == QUOTE) return readString();
		if(it.available() >= 4){
			if(useLog) Log.write(this, "check first chars: \""+it.getString(4)+"\"");
			if("true".equals(it.getString(4))) {
				it.skip(4);
				return "true";
			}
			if(it.available() >= 5 && "false".equals(it.getString(5))) {
				it.skip(5);
				return "false";
			}
			if("null".equals(it.getString(4))) {
				it.skip(4);
				return "null";
			}
		}
		if(isObject() || isArray()){
			throw new InternalError("value is not a value");
		}
		return readNumber();
		
	}
	
	public String readString(){
		char prev = it.next();
		if(prev != QUOTE) return null;
		StringBuilder sb = new StringBuilder();
		int escapeCount = 0;
		while(!(escapeCount % 2 == 0 && it.peek() == QUOTE)){
			sb.append(prev = it.next());
			if(prev == ESCAPE) escapeCount++;
			else escapeCount = 0;
		}
		it.next();
		String res = sb.toString();
		return res;
	}
}
