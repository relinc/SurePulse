package net.relinc.libraries.dev;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TaskMonitor {
	
	private static List<Task> tasks = new ArrayList<Task>();
	private static int taskCount = 0;
	public static int start(String taskName) {
		tasks.add(new Task(taskName, ++taskCount));
		return taskCount;
	}
	
	public static void end(int taskId) {
		Task endTask = tasks.stream().filter(t -> t.getId() == taskId).findFirst().get();
		endTask.end();
	}
	
	public static void printTasks() {
		System.out.println("==================================================================");
		System.out.println("Tasks: " + taskCount);
		List<Task> sortedTasks = tasks.stream().sorted((a, b) -> a.getDuration().compareTo(b.getDuration())).collect(Collectors.toList());
		sortedTasks.stream().forEach(task -> {
			System.out.println(task);
		});
		System.out.println("==================================================================");
	}
}

class Task {
	private Optional<Instant> startTime = Optional.empty(); // using `null` is for noobs
	private Optional<Instant> endTime = Optional.empty();
	private String name = "";
	private int id = -1;
	
	public Task(String name, int id) {
		this.name = name;
		this.id = id;
		this.start();
	}
	
	public void start() {
		this.setStartTime(java.time.Instant.now());
	}
	
	public void end() {
		this.setEndTime(java.time.Instant.now());
	}
	
	public Optional<Instant> getStartTime() {
		return startTime;
	}
	public void setStartTime(Instant startTime) {
		this.startTime = Optional.of(startTime);
	}
	public Optional<Instant> getEndTime() {
		return endTime;
	}
	public void setEndTime(Instant endTime) {
		this.endTime = Optional.of(endTime);
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	public void checkStartEndPresent() {
		if(!(this.getStartTime().isPresent() && this.getEndTime().isPresent()))
			throw new RuntimeException("Cannot getDuration if end or start is not set");
	}
	
	public Duration getDuration() {
		checkStartEndPresent();
		return Duration.between(this.getStartTime().get(), this.getEndTime().get());
	}
	
	@Override
	public String toString() {
		String res = "Task\n";
		res += String.format("id=%d\n", this.getId());
		res += String.format("name=%s\n", this.getName());
		boolean startPresent = this.getStartTime().isPresent();
		boolean endPresent = this.getEndTime().isPresent();
		if(startPresent && endPresent)
			res += String.format("duration (s)=%.1f\n", this.getDuration().toMillis() * .001);
		if(startPresent)
			res += String.format("start=%s\n", this.getStartTime().get().toEpochMilli());
		if(endPresent)
			res += String.format("end=%s\n", this.getEndTime().get().toEpochMilli());
		res += "\n";
		return res;
	}

}