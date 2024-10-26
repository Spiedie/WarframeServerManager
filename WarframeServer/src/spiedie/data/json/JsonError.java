package spiedie.data.json;

public class JsonError extends Json{
	private String msg;
	public JsonError(String msg){
		this.msg = msg;
	}
	
	public String toJson(){
		return null;
	}
	
	public String toString(){
		return "JsonError: "+msg;
	}
	
	public int getType() {
		return ERROR;
	}
}
