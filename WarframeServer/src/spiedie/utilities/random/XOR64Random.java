package spiedie.utilities.random;

import spiedie.utilities.util.Time;

public class XOR64Random {
	private long seed;
	public XOR64Random(){
		this(Time.nanos());
	}
	
	public XOR64Random(long seed){
		if(seed == 0) throw new IllegalArgumentException("Seed can not be 0.");
		this.seed = seed;
	}
	
	/**
	 * Get an int with the last n bits being pseudo random.
	 * @param nbits
	 * @return
	 */
	public int next(int nbits) {
		long x = seed;
		x ^= (x << 21);
		x ^= (x >>> 35);
		x ^= (x << 4);
		seed = x;
		x &= ((1L << nbits) - 1);
		return (int) x;
	}

}
