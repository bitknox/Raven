package dk.itu.raven.beast;

import java.util.ArrayList;

public class BenchResult {
	private String name;
	private ArrayList<Long> time;
	private int iterations;

	public BenchResult(String name) {
		this.name = name;
		this.time = new ArrayList<Long>();
		this.iterations = 0;
	}

	public void addEntry(long time) {
		this.time.add(time);
		this.iterations++;
	}
}