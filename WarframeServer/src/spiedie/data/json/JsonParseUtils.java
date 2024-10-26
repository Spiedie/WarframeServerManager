package spiedie.data.json;

import spiedie.utilities.data.CharIterator;
import spiedie.utilities.util.log.Log;

public class JsonParseUtils {
	
	public static final char CURLY_OPEN = '{';
	public static final char CURLY_CLOSE = '}';
	public static final char BRACE_OPEN = '[';
	public static final char BRACE_CLOSE = ']';
	public static final char QUOTE = '"';
	public static final char COLON = ':';
	public static final char COMMA = ',';
	public static final char ESCAPE = '\\';
	
	protected CharIterator it;
	protected boolean useLog = false;
	public String errorContext(){
		return errorContext(it.position(), 30);
	}
	
	public void setUseLog(boolean log){
		this.useLog = log;
	}
	
	public String errorContext(int pos, int len){
		StringBuilder sb = new StringBuilder(it.getString(Math.max(0, pos - len/2), Math.min(len, it.available() + it.position())));
		String s  = sb.toString();
		s = s.replace("\t", "\\t");
		s = s.replace("\n", "\\n");
		s = s.replace("\r", "\\r");
		return s;
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
		sb.append(QUOTE);
		int escapeCount = 0;
		while(!(escapeCount % 2 == 0 && it.peek() == QUOTE)){
			sb.append(prev = it.next());
			if(prev == ESCAPE) escapeCount++;
			else escapeCount = 0;
		}
		sb.append(it.next());
		String res = sb.toString();
		return res;
	}
	
	public String readNumber(){
		StringBuilder sb = new StringBuilder();
		if(it.peek() == 'e' || it.peek() == 'E') return null;
		if(it.peek() == '-') {
			it.next();
			if(!Character.isDigit(it.peek())) return null;
			sb.append('-');
		}
		while(it.hasNext() && (it.peek() == '+' || it.peek() == '-' || Character.isDigit(it.peek()) || it.peek() == 'e' || it.peek() == 'E' || it.peek() == '.')){
			sb.append(it.next());
		}
		return sb.toString();
	}
	
	public void skipWhitespace(){
		while(it.hasNext() && Character.isWhitespace(it.peek())){
			it.next();
		}
	}
	
	public boolean isArray(){
		return it.peek() == '[';
	}
	
	public boolean isObject(){
		return it.peek() == '{';
	}
	
	public boolean isString(){
		return it.peek() == QUOTE;
	}
	
	public boolean isValue(){
		return !isArray() && !isObject();
	}
}
