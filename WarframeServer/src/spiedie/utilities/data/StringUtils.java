package spiedie.utilities.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils{
	public static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
	public static final String UPPER = LOWER.toUpperCase();
	public static final String DIGITS = "1234567890";
	public static int tabReplacementSize =  3;
	private StringUtils(){}
	
	/**
	 * 
	 * @param s
	 * @return the parsed int, as though non-digits did not exist in the input.
	 */
	public static int parseInt(String s){
		return Integer.parseInt(parseIntegerPreparation(s));
	}
	
	/**
	 * 
	 * @param s
	 * @return the parsed long, as though non-digits did not exist in the input.
	 */
	public static long parseLong(String s){
		return Long.parseLong(parseIntegerPreparation(s));
	}
	
	/**
	 * 
	 * @param s
	 * @return
	 */
	private static String parseIntegerPreparation(String s) {
		if(s == null) return "0";
		boolean neg = s.startsWith("-");
		s = StringUtils.keep(s, DIGITS);
		if(s.isEmpty()) s = "0";
		if(neg) s = "-"+s;
		return s;
	}
	
	/**
	 * 
	 * @param s
	 * @param pat
	 * @param repl
	 * @return
	 */
	public static String replace(String s, String pat, String repl){
		if(!repl.contains(pat)) while(s.contains(pat)) s = s.replace(pat, repl);
		return s;
	}
	
	/**
	 * 
	 * @param s
	 * @param keep
	 * @return
	 */
	public static String keep(String s, String keep){
		if(s == null) return null;
		if(keep == null) return s;
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < s.length();i++){
			if(keep.contains(String.valueOf(s.charAt(i)))){
				sb.append(s.charAt(i));
			}
		}
		return sb.toString();
	}
	
	/**
	 * 
	 * @param text
	 * @param pattern
	 * @return
	 */
	public static String getFromPattern(String text, String pattern){
		return getFromPattern(text, Pattern.compile(pattern));
	}
	
	/**
	 * 
	 * @param text
	 * @param pattern
	 * @return
	 */
	public static String getFromPattern(String text, Pattern pattern){
		Matcher m = pattern.matcher(text);
		if(m.find()){
			if(m.groupCount() >= 1) return m.group(1);
		}
		return null;
	}
	
	/**
	 * 
	 * @param text
	 * @param pattern
	 * @return
	 */
	public static ArrayList<String> getAllFromPattern(String text, String pattern){
		return getAllFromPattern(text, Pattern.compile(pattern));
	}
	
	/**
	 * 
	 * @param text
	 * @param pattern
	 * @return
	 */
	public static ArrayList<String> getAllFromPattern(String text, Pattern pattern){
		ArrayList<String> matches = new ArrayList<>();
		Matcher m = pattern.matcher(text);
		while(m.find()){
			if(m.groupCount() >= 1) matches.add(m.group(1));
		}
		return matches;
	}
	
	/**
	 * 
	 * @param x the number of characters to generate.
	 * @return
	 */
	public static String random(int x){
		String possible = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!@#$%^*()=+_-;:";
		return random(x, possible);
	}
	
	/**
	 * 
	 * @param x the number of characters to generate.
	 * @param possible the possible characters present in the result.
	 * @return a random String of length x.
	 */
	public static String random(int x, String possible){
		Random r = new Random();
		return random(r, x, possible);
	}
	
	/**
	 * @param r the random source.
	 * @param x the number of characters to generate.
	 * @param possible the possible characters present in the result.
	 * @return a random String of length x.
	 */
	public static String random(Random r, int x, String possible){
		StringBuilder res = new StringBuilder();
		for(int i = 0; i < x; i++){
			res.append(possible.charAt(r.nextInt(possible.length())));
		}
		return res.toString();
	}

	/**
	 * 
	 * @param s
	 * @param size
	 * @return
	 */
	public static String makeToSize(String s, int size){
		return makeToSize(s, ' ', size);
	}
	
	/**
	 * 
	 * @param s
	 * @param c
	 * @param size
	 * @return
	 */
	public static String makeToSize(String s, char c, int size){
		if(size <= 0) return "";
		String replacement = "";
		for(int i = 0; i < tabReplacementSize;i++){
			replacement += " ";
		}
		s = s.replace("\t", replacement);
		while(s.length() < size){
			s += c;
		}
		return s.substring(0, size);
	}
	
	/**
	 * 
	 * @param listss
	 * @return
	 */
	public static String format(List<List<String>> listss){
		String[][] sss = new String[listss.size()][];
		for(int i = 0; i < sss.length;i++){
			sss[i] = listss.get(i).toArray(new String[listss.get(i).size()]);
		}
		return format(sss);
	}
	
	/**
	 * 
	 * @param sss
	 * @return
	 */
	public static String format(String[][] sss){
		int max = 0;
		for(String[] ss : sss) max = Math.max(max, ss.length);
		int[] lens = new int[max];
		for(String[] ss : sss){
			for(int i = 0; i < ss.length;i++){
				lens[i] = Math.max(lens[i], ss[i] == null ? 0 : ss[i].length());
			}
		}
		for(String[] ss : sss){
			for(int i = 0; i < lens.length;i++){
				if(ss.length > i){
					if(ss[i] == null) ss[i] = "";
					ss[i] = StringUtils.makeToSize(ss[i], lens[i]);
				}
			}
		}
		StringBuilder sb = new StringBuilder();
		for(String[] ss : sss){
			for(String s : ss){
				sb.append(s+" ");
			}
			if(ss.length > 0) sb.setLength(sb.length() - 1);
			sb.append("\n");
		}
		return sb.toString();
	}
	
}
