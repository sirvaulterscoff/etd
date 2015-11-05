package etd.model;

import etd.rest.JSON;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author pobedenniy.alexey
 * @since 03.11.2015
 */
public class ThreadDump {
	public static final String DATE_FORMAT = "dd-MM-yyyy HH:mm:ss.SSS";
	private long createdOn = System.currentTimeMillis();
	private List<ThreadInfo> result;
	private long totalStartedThreadCount;
	private int daemonThreadCount;
	private int peakThreadCount;
	private int threadCount;
	private int blocked;
	private int waiting;
	private int runnable;
	private List<String> deadlocks;

	public ThreadDump(List<ThreadInfo> result) {
		this.result = result;
	}

	public ThreadDump setCounts(long totalStartedThreadCount, int daemonThreadCount, int peakThreadCount, int threadCount, int blocked, int waiting, int runnabled) {
		this.totalStartedThreadCount = totalStartedThreadCount;
		this.daemonThreadCount = daemonThreadCount;
		this.peakThreadCount = peakThreadCount;
		this.threadCount = threadCount;
		this.blocked = blocked;
		this.waiting = waiting;
		this.runnable = runnabled;
		return this;
	}

	public String toJSON() {
		return JSON.writePairs(
				new String[]{
						"info",
						"threads",
						"deadlocks"},
				new Object[]{
						JSON.writePairs(new String[]{"createdOn", "started", "daemon", "peak", "count", "runnable", "blocked", "waiting"},
								new Object[]{getSnapshotDate(), totalStartedThreadCount, daemonThreadCount, peakThreadCount, threadCount, runnable, blocked, waiting}),
						JSON.writeList(result),
						JSON.writeToStringList(deadlocks)
				});
	}

	private String getSnapshotDate() {
		return new SimpleDateFormat(DATE_FORMAT).format(new Date(createdOn));
	}

	public ThreadDump setDeadlocks(long[] deadlocks) {
		if (deadlocks == null) {
			this.deadlocks = Collections.emptyList();
			return this;
		}
		this.deadlocks = new ArrayList<>(deadlocks.length);
		for (long deadlock : deadlocks) {
			this.deadlocks.add(String.valueOf(deadlock));
		}
		return this;
	}
}
