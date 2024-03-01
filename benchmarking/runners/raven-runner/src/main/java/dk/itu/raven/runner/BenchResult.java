package dk.itu.raven.runner;

import java.util.ArrayList;

public class BenchResult {
	private String name;
	private ArrayList<Long> times;
	private int iterations;
	public ArrayList<String> labels = new ArrayList<String>();



	public BenchResult(String name) {
		this.name = name;
		this.times = new ArrayList<Long>();
		this.iterations = 0;
	}

	public void addLabel(String label) {
		this.labels.add(label);
	}

	public String formatPath(String path) {
		String[] parts = path.split("/");
		return parts[parts.length - 1];
	}

	public void addEntry(long time) {
		this.times.add(time);
		this.iterations++;
	}
}