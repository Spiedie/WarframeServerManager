package spiedie.data.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

import spiedie.data.json.data.IJson;
import spiedie.data.json.data.IJsonArray;
import spiedie.data.json.data.IJsonObject;

public class JsonArray extends Json implements IJsonArray {
	public static boolean TO_JSON_WHITESPACE = false;
	
	private List<IJson> list;
	
	public JsonArray(){
		list = Collections.synchronizedList(new ArrayList<>());
	}
	
	public boolean add(String s) {
		return add(Json.value(JsonEscapeMethods.escape(s)));
	}
	
	public boolean add(IJson e) {
		return list.add(Objects.requireNonNull(e));
	}
	
	public void add(int index, IJson e) {
		list.add(index, Objects.requireNonNull(e));
	}
	
	public boolean addAll(Collection<? extends IJson> c) {
		return list.addAll(c);
	}

	public boolean addAll(int i, Collection<? extends IJson> c) {
		return list.addAll(i, c);
	}

	public void clear() {
		list.clear();
	}

	public boolean contains(Object o) {
		return list.contains(o);
	}

	public boolean containsAll(Collection<?> c) {
		return list.containsAll(c);
	}

	public IJson get(int index) {
		return list.get(index);
	}

	public int indexOf(Object o) {
		return list.indexOf(o);
	}

	public boolean isEmpty() {
		return list.isEmpty();
	}

	public Iterator<IJson> iterator() {
		return list.iterator();
	}

	public int lastIndexOf(Object o) {
		return list.lastIndexOf(o);
	}

	public ListIterator<IJson> listIterator() {
		return list.listIterator();
	}

	public ListIterator<IJson> listIterator(int index) {
		return list.listIterator(index);
	}

	public boolean remove(Object o) {
		return list.remove(o);
	}

	public IJson remove(int index) {
		return list.remove(index);
	}

	public boolean removeAll(Collection<?> c) {
		return list.removeAll(c);
	}

	public boolean retainAll(Collection<?> c) {
		return list.retainAll(c);
	}

	public IJson set(int index, IJson e) {
		return list.set(index, e);
	}

	public int size() {
		return list.size();
	}

	public List<IJson> subList(int from, int to) {
		return list.subList(from, to);
	}

	public <T> T[] toArray(T[] a) {
		return list.toArray(a);
	}
	
	public Object[] toArray() {
		return list.toArray();
	}
	
	public List<String> toStringList(){
		List<String> list = new ArrayList<>();
		for(IJson j : this){
			if(j.getType() == Json.VALUE){
				list.add(j.toJsonValue().getValue());
			}
		}
		return list;
	}
	
	public List<IJsonObject> toObjectList(){
		List<IJsonObject> list = new ArrayList<>();
		for(IJson j : this){
			if(j.getType() == Json.OBJECT){
				list.add(j.toJsonObject());
			}
		}
		return list;
	}
	
	public String toJson(){
		StringBuilder sb = new StringBuilder();
		sb.append(JsonParseUtils.BRACE_OPEN);
		if(TO_JSON_WHITESPACE) sb.append("\n");
		for(int i = 0; i < list.size();i++){
			
			sb.append(list.get(i).toJson());
			if(i != list.size() - 1){
				sb.append(JsonParseUtils.COMMA);
				if(TO_JSON_WHITESPACE) sb.append("\n");
			}
		}
		if(TO_JSON_WHITESPACE) sb.append("\n");
		sb.append(JsonParseUtils.BRACE_CLOSE);
		return sb.toString();
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("[\n");
		for(int i = 0; i < list.size();i++){
			sb.append("\t"+list.get(i).toString().replace("\n", "\n\t"));
			if(i != list.size() - 1) sb.append(",");
			sb.append("\n");
		}
		sb.append(']');
		return sb.toString();
	}
	
	public boolean equals(Object that){
		if(that instanceof JsonArray){
			JsonArray a = (JsonArray) that;
			return list.equals(a.list);
		}
		return false;
	}
	
	public int hashCode(){
		return 33 + list.hashCode();
	}
	
	public int getType() {
		return ARRAY;
	}
	
}
