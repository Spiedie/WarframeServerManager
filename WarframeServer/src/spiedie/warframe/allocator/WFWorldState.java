package spiedie.warframe.allocator;

import spiedie.data.json.Json;
import spiedie.data.json.data.IJson;
import spiedie.utilities.concurrency.AbstractThread;
import spiedie.utilities.net.NetUtils;
import spiedie.utilities.util.Constants;
import spiedie.utilities.util.Time;
import spiedie.utilities.util.log.Log;

public class WFWorldState extends AbstractThread{
	public static final String URL_WORLD_STATE = "http://content.warframe.com/dynamic/worldState.php";
	private String url;
	private long timeout, delay;
	private IJson j;
	public WFWorldState(String url, long timeout, long delay){
		this.url = url;
		this.timeout = timeout;
		this.delay = delay;
	}
	
	public IJson getJson(){
		return j == null ? Json.object() : j;
	}
	
	public void run() {
		while(isRunning()){
			try {
				String html = NetUtils.getHTMLCached(url, Constants.getSettings(), timeout);
				if(html != null){
					IJson j = Json.parse(html);
					if(j != null){
						this.j = j;
					}
				}
			} catch (Exception e) {
				Log.caught(this, e);
			}
			Time.sleep(delay);
		}
	}
	
	public IJson get(int type, String key){
		if(getJson() == null) return null;
		return Json.get(getJson(), type, key);
	}
	
	public String getEventMode(){
		IJson j = get(Json.VALUE, "PVPAlternativeModes.TargetMode=PVP.TargetMode");
		return j == null ? null : j.toJsonValue().getValue();
	}
}
