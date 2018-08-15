package net.relinc.libraries.dev;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
	
	public static void printTasks(String orderBy) {
		System.out.println("==================================================================");
		System.out.println("Tasks: " + taskCount + " ordered by: " + orderBy);
		List<Task> sortedTasks = Collections.emptyList();
		switch(orderBy) {
		case "duration":
			sortedTasks = tasks.stream().sorted((a, b) -> a.getDuration().compareTo(b.getDuration())).collect(Collectors.toList());
			break;
		case "start":
			sortedTasks = tasks.stream().sorted((a, b) -> a.getStartTime().get().compareTo(b.getStartTime().get())).collect(Collectors.toList());
			break;
		default:
			throw new RuntimeException("orderBy: " + orderBy + " is not supported!");
		}
		
		sortedTasks.stream().forEach(task -> {
			System.out.println(task);
		});
		System.out.println("==================================================================");
	}
	
	public static void printTimeline() {
		Map<Instant, Task> allInstants = new HashMap<Instant, Task>();
		tasks.stream().forEach(task -> {
			allInstants.put(task.getStartTime().get(), task);
			allInstants.put(task.getEndTime().get(), task);
		});
		allInstants.entrySet().stream().sorted((a,b) -> a.getKey().compareTo(b.getKey())).forEach(entry -> {
			Task task = entry.getValue();
			if(entry.getKey().equals(task.getStartTime().get())) {
				System.out.println(String.format("Start task=%s, id=%d", task.getName(), task.getId()));
			} else {
				System.out.println(String.format("End task=%s id=%d", task.getName(), task.getId()));
			}
		});
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