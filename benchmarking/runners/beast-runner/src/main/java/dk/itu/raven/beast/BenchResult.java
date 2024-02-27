package dk.itu.raven.beast;

import java.util.ArrayList;

public class BenchResult {
	private String name;
	private ArrayList<Long> times;
	private int iterations;

	public BenchResult(String name) {
		this.name = name;
		this.times = new ArrayList<Long>();
		this.iterations = 0;
	}

	public void addEntry(long time) {
		this.times.add(time);
		this.iterations++;
	}
}