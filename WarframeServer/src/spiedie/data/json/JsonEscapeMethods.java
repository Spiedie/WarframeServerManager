package spiedie.data.json;

import spiedie.utilities.util.log.Log;

public class JsonEscapeMethods {
	
	public static boolean LOG_UNFINISHED_ESCAPE = true;
	
	public static IJsonEscapeMethod escapeMethod, unescapeMethod;
	
	private static boolean[] escapeChars = new boolean[70000];
	private static char[] escapeCharsMap = new char[70000];
	private static boolean[] unescapeChars = new boolean[70000];
	private static char[] unescapeCharsMap = new char[70000];
	
	static {
		escapeMethod = JsonEscapeMethods::escapeLazyCallIndexedMapped;
		unescapeMethod = JsonEscapeMethods::unescapeLazyCallIndexedMapped;
		escapeChars['\\'] = true;
		escapeChars['/'] = true;
		escapeChars['\"'] = true;
		escapeChars['\n'] = true;
		escapeChars['\r'] = true;
		escapeChars['\b'] = true;
		escapeChars['\f'] = true;
		escapeChars['\t'] = true;
		
		escapeCharsMap['\\'] = '\\';
		escapeCharsMap['/'] = '/';
		escapeCharsMap['"'] = '"';
		escapeCharsMap['\n'] = 'n';
		escapeCharsMap['\r'] = 'r';
		escapeCharsMap['\b'] = 'b';
		escapeCharsMap['\f'] = 'f';
		escapeCharsMap['\t'] = 't';
		
		unescapeChars['\\'] = true;
		unescapeChars['/'] = true;
		unescapeChars['"'] = true;
		unescapeChars['n'] = true;
		unescapeChars['r'] = true;
		unescapeChars['b'] = true;
		unescapeChars['f'] = true;
		unescapeChars['t'] = true;
		unescapeChars['u'] = true;
		
		unescapeCharsMap['\\'] = '\\';
		unescapeCharsMap['/'] = '/';
		unescapeCharsMap['"'] = '"';
		unescapeCharsMap['n'] = '\n';
		unescapeCharsMap['r'] = '\r';
		unescapeCharsMap['b'] = '\b';
		unescapeCharsMap['f'] = '\f';
		unescapeCharsMap['t'] = '\t';
		unescapeCharsMap['u'] = '?';
	}
	
	public static final String escape(String s){
		return escapeMethod.escape(s);
	}
	
	public static final String escapeMapped(String s, int offset){
		StringBuilder sb = new StringBuilder();
		if(offset > 0) sb.append(s, 0, offset);
		for(int i = offset; i < s.length();i++){
			if(escapeChars[s.charAt(i)]) {
				sb.append('\\');
				sb.append(escapeCharsMap[s.charAt(i)]);
			} else {
				sb.append(s.charAt(i));
			}
		}
		return sb.toString();
	}
	
	public static String escapeLazyCallIndexedMapped(String s) {
		for(int i = 0; i < s.length();i++) {
			if(escapeChars[s.charAt(i)]) return escapeMapped(s, i);
		}
		return s;
	}
	
	public static String unescape(String s) {
		return unescapeMethod.escape(s);
	}
	
	public static String unescapeMapped(String s, int offset) {
		StringBuilder sb = new StringBuilder();
		if(offset > 0) sb.append(s, 0, offset);
		for(int i = offset; i < s.length();i++) {
			char c = s.charAt(i);
			if(c == JsonParseUtils.ESCAPE) {
				if(i + 1 == s.length()) throw new InternalError("Nothing to escape at "+i+" in "+s);
				c = s.charAt(i + 1);
				if(unescapeChars[c]) {
					sb.append(unescapeCharsMap[c]);
				} else {
					if(LOG_UNFINISHED_ESCAPE) Log.err(JsonParseUtils.class, "Escape not followed by known escapable character: "+s.charAt(i + 1));
				}
				i++;
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}
	
	public static String unescapeLazyCallIndexedMapped(String s) {
		for(int i = 0; i < s.length();i++) {
			if(s.charAt(i) == JsonParseUtils.ESCAPE) return unescapeMapped(s, i);
		}
		return s;
	}
	
}
