package it.unibs.opt.alg.etb;

public class Configuration {
	
	private int presolve;
	private int kernelSize;
	private int bucketSize;
	private int timeFirstBucket;
	private int timeAddedToEachBucket;
	private int minExamsPerPeriod;
	private int method;
	private int numThreads;
	private int numIterations;
	
	
	public int getPresolve() {
		return presolve;
	}
	
	public void setPresolve(int presolve) {
		this.presolve = presolve;
	}
	
	public int getKernelSize() {
		return kernelSize;
	}
	
	public void setKernelSize(int kernelSize) {
		this.kernelSize = kernelSize;
	}
	
	public int getBucketSize() {
		return bucketSize;
	}
	
	public void setBucketSize(int bucketSize) {
		this.bucketSize = bucketSize;
	}
	
	public int getTimeFirstBucket() {
		return timeFirstBucket;
	}
	
	public void setTimeFirstBucket(int timeFirstBucket) {
		this.timeFirstBucket = timeFirstBucket;
	}
	
	public int getTimeAddedToEachBucket() {
		return timeAddedToEachBucket;
	}
	
	public void setTimeAddedToEachBucket(int timeAddedToEachBucket) {
		this.timeAddedToEachBucket = timeAddedToEachBucket;
	}
	
	public int getMinExamsPerPeriod() {
		return minExamsPerPeriod;
	}
	
	public void setMinExamsPerPeriod(int minExamsPerPeriod) {
		this.minExamsPerPeriod = minExamsPerPeriod;
	}

	public int getMethod() {
		return method;
	}
	
	public void setMethod(int method) {
		this.method = method;
	}
	public int getNumThreads() {
		return numThreads;
	}
	
	public void setNumThreads(int numThreads) {
		this.numThreads = numThreads;
	}
	
	public int getNumIterations() {
		return numIterations;
	}
	
	public void setNumIterations(int numIterations) {
		this.numIterations = numIterations;
	}

}