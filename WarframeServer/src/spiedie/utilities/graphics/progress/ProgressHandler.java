package spiedie.utilities.graphics.progress;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import spiedie.utilities.concurrency.AbstractThread;

import spiedie.utilities.util.Time;

public class ProgressHandler extends AbstractThread{
	public static int SPEED_SHOW_MEMORY_RATIO_DEFAULT = 5;
	protected ProgressBar bar;
	private Set<Progressable> list;
	private long lastMax = 0;
	private boolean canEnd;
	private long isFinishedOffset = 0;
	private boolean stopped;
	// speed show
	private String speedShowString;
	private long speedShowUpdateTime = 1000;
	private int speedShowMemoryRatio = SPEED_SHOW_MEMORY_RATIO_DEFAULT;
	private double prevSpeed = -1;
	private long speedShowTime = Time.millis();
	private long lastSpeedProg;
	
	public ProgressHandler(){
		super(true);
		this.bar = new ProgressBar(true, true, true, false);
		this.bar.setBarMax(0);
		this.list = Collections.synchronizedSet(new HashSet<Progressable>());
	}
	
	public void start(){
		bar.setVisible(true);
		super.start("Progress");
	}
	
	public void run() {
		while(!canEnd || isRunning()){
			Time.sleep(100);
			update();
			if(!isFinished()) setFinished(bar.getValue() >= bar.getBarMax()-isFinishedOffset);
		}
		setFinished(true);
		stop();
	}
	
	public void setFinished(boolean finished){
		if(canEnd){
			super.setFinished(finished);
		}
	}
	
	public void setCanEnd(boolean val){
		canEnd = val;
	}
	
	public void stop(){
		if(!stopped){
			stopped = true;
			clear();
			bar.dispose();
		}
	}
	
	public void clear(){
		synchronized(list){
			list.clear();
		}
	}
	
	public void update(){
		long max = getMaxFromList();
		long prog = getProgressedFromList();
//		Log.write(this, "Update: "+prog+"/"+max);
		if(max != lastMax){
			bar.setBarMax(max);
			lastMax = max;
		}
		if(bar != null) bar.setBarDone(prog);
		setRequestedFileFromList();
		long left = max-prog;
		String size = "B";
		if(Math.abs(left) > 5 * 1024){
			left = left/1024;
			size = "KB";
		}
		if(Math.abs(left) > 5 * 1024){
			left = left/1024;
			size = "MB";
		}
		if(Math.abs(left) > (1024 << 3)){
			left = left/1024;
			size = "GB";
		}
		if(bar != null){
			String onBar = left+" "+size+" left";
			String show = getSpeedString();
			if(show != null){
				speedShowString = show;
			}
			if(speedShowString != null){
				onBar += ": "+speedShowString;
			}
			bar.setSpeed(onBar);
		}
	}

	public long getProgressedFromList(){
		long progress = 0;
		synchronized(list){
			for(Progressable p : list){
				progress += p.getValue();
			}
		}
		return progress;
	}

	public long getMaxFromList(){
		long max = 0;
		synchronized(list){
			for(Progressable p : list){
				max += p.getMax();
			}
		}
		return max;
	}

	private void setRequestedFileFromList(){
		synchronized(list){
			String file = null;
			for(Progressable p : list){
				String req = p.getRequestedFile();
				if(req != null) file = req;
			}
			if(file != null){
				String cur = getFile();
				if(!file.equals(cur)) setFile(file);
			}
		}
	}
	
	public void addProgressable(Progressable p){
		list.add(p);
	}
	
	public void setFile(String file){
		bar.setFile(file);
		bar.setPostTitle(file);
	}
	
	public String getFile(){
		return bar.getFile();
	}
	
	public String getSpeedString() {
		if(Time.millis() - speedShowTime > speedShowUpdateTime){
			double tmpProg = getProgressedFromList() - lastSpeedProg;
			double tmpTimeMs = Time.millis() - speedShowTime;
			double bytesPerSecond = 1000 * tmpProg/tmpTimeMs;
			int nums = 0;
			if(prevSpeed != -1) bytesPerSecond = ((prevSpeed * (speedShowMemoryRatio - 1)) + bytesPerSecond) / speedShowMemoryRatio;
			prevSpeed = bytesPerSecond;
			double value = bytesPerSecond;
			while(value > 5000){
				value/=1024;
				nums++;
			}
			speedShowTime = Time.millis();
			String ext = null;
			switch(nums){
			case 0: ext = "Bytes"; break;
			case 1: ext = "KB"; break;
			case 2: ext = "MB"; break;
			case 3: ext = "GB"; break;
			case 4: ext = "TB"; break;
			default: ext = "Lots"; break;
			}
			lastSpeedProg = getProgressedFromList();
			long todo = getMaxFromList() - lastSpeedProg;
			double seconds = todo / bytesPerSecond;
			String timeExt = "Seconds";
			if(seconds > 3600 * 24 * 365 * 2){
				timeExt = "Years";
				seconds /= 3600 * 24 * 365;
			} else if(seconds > 2 * 3600 * 24 * 365 / 12){
				timeExt = "Months";
				seconds /= 3600 * 24 * 365 / 12;
			} else if(seconds > 3600 * 48){
				timeExt = "Days";
				seconds /= 3600 * 24;
			} else if(seconds > 7200){
				timeExt = "Hours";
				seconds /= 3600;
			} else if(seconds > 120){
				timeExt = "Minutes";
				seconds /= 60;
			}
			return String.format("%.2f %s/s (%.1f), %.0f %s remaining.", value, ext, (1000 * tmpProg/tmpTimeMs)/1024/1024, seconds, timeExt);
		}
		return null;
	}
	
}
