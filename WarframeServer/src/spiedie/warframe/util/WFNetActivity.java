package spiedie.warframe.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import spiedie.data.json.Json;
import spiedie.data.json.data.IJson;
import spiedie.data.json.data.IJsonArray;
import spiedie.data.json.data.IJsonObject;
import spiedie.evolutionEngine.EELogUtils;
import spiedie.utilities.data.StringUtils;
import spiedie.utilities.stream.Stream;
import spiedie.utilities.stream.raf.RAFStream;
import spiedie.utilities.util.Time;
import spiedie.utilities.util.log.Log;

public class WFNetActivity {
	private List<File> fs;
	private long[] offsets, startTimes;
	private List<IntroRequest> requests;
	
	public WFNetActivity() {
		this(new ArrayList<>());
	}
	
	public WFNetActivity(List<File> fs) {
		this.fs = fs;
		this.offsets = new long[fs.size()];
		this.startTimes = new long[fs.size()];
		requests = new ArrayList<>();
	}
	
	public List<IntroRequest> get(){
		return requests;
	}
	
	public IJson toJson() {
		IJsonArray a = Json.array();
		for(IntroRequest r : get()) {
			a.add(r.toJson());
		}
		return a;
	}
	
	public void clean(long time) {
		List<IntroRequest> list = get();
		long max = list.stream().mapToLong(e -> e.time).max().orElse(0);
		long maxAllowed = max - time;
		Log.write(Time.toDateString(max));
		int rem = 0;
		Set<String> exist = new HashSet<>();
		for(int i = list.size() - 1; i >= 0;i--) {
			if(list.get(i).time < maxAllowed) {
				list.remove(i);
				rem++;
			} else if(exist.contains(list.get(i).ip)) {
				list.remove(i);
			} else {
				exist.add(list.get(i).ip);
			}
		}
		if(rem > 0) Log.write(this, "Deleted "+rem);
	}
	
	private void process(String input, int index) {
		String[] lines = input.split("\n");
		for(int i = 0; i < lines.length;i++) {
			String line = lines[i].trim();
			if(startTimes[index] == 0) {
				startTimes[index] = EELogUtils.getStartTimeFromEntry(line);
				if(startTimes[index] != 0) {
					startTimes[index] = startTimes[index] - EELogUtils.getRelativeTime(line);
				}
			}
			if(line.toLowerCase().contains("introduction request")) {
				String pattern = ".*Received IT_.* introduction request from .* task id \\d* \\((\\d*\\.\\d*\\.\\d*\\.\\d*):\\d*\\).*";
				String ip = StringUtils.getFromPattern(line, pattern);
				if(ip != null) {
					long time = startTimes[index] + EELogUtils.getRelativeTime(line);
					requests.add(new IntroRequest(fs.get(index), ip, time));
				}
			}
		}
	}
	
	private void reset(int index) {
		offsets[index] = 0;
		startTimes[index] = 0;
		for(int i = get().size() - 1; i >= 0;i--) {
			if(get().get(i).f == fs.get(index)) {
				get().remove(i);
			}
		}
	}
	
	public void update() {
		try {
			for(int i = 0; i < fs.size();i++) {
				File f = fs.get(i);
				if(f.exists()) {
					long current = f.length();
					if(current < offsets[i]) {
						reset(i);
					}
					if(current > offsets[i]) {
//						Log.write(this, current+" "+offsets[i]);
						RAFStream stream = new RAFStream(new RandomAccessFile(f, "r"));
						stream.seek(offsets[i]);
						int newBytes = (int)(current - offsets[i]);
						byte[] buf = new byte[newBytes];
						int read = Stream.readBlockingStrict(stream.in, buf, 0, buf.length);
						offsets[i] += read;
						stream.raf.close();
						String s = new String(buf, 0, read, Stream.charset);
						process(s, i);
					}
				}
			}
		} catch (IOException e) {
			Log.caught(this, e);
		}
	}
	
	public static WFNetActivity parse(IJsonArray a) {
		WFNetActivity activity = new WFNetActivity(new ArrayList<>());
		for(IJsonObject o : a.toObjectList()) {
			activity.get().add(IntroRequest.parse(o));
		}
		return activity;
	}

	public static class IntroRequest{
		public final String ip;
		public long time;
		public File f;
		
		public IntroRequest(File f, String ip, long time) {
			this.ip = getHash(ip);
			this.time = time;
			this.f = f;
		}
		
		public IJsonObject toJson() {
			IJsonObject o = Json.object();
			o.setProperty("ip", ip);
			o.setProperty("time", ""+time);
			return o;
		}
		
		public static final String getHash(String ip) {
			try {
				byte[] buf = ip.getBytes("UTF-8");
				long hash = buf.length;
				for(int i = 0; i < buf.length;i++){
					hash = 31 * hash + buf[i];
				}
				return Long.toHexString(hash);
			} catch (UnsupportedEncodingException e) {
				throw new AssertionError("JVM should support UTF-8.", e);
			}
		}
		
		public static IntroRequest parse(IJsonObject o) {
			return new IntroRequest(null, o.getProperty("ip"), StringUtils.parseLong(o.getProperty("time")));
		}
	}
	
}
