package etd.model;

import etd.rest.JSON;
import etd.rest.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author pobedenniy.alexey
 * @since 03.11.2015
 */
public class ThreadInfo implements JSONObject {
	public static final int MS_TIMEUNIT_CAP = 999;
	private static final long TIMEUNIT_CAP = 99;
	private final long threadId;
	private final String threadName;
	private String state;
	private List<StackElement> stackElements;
	/**
	 * Number of times this thread was in {@link Thread.State#WAITING } or {@link Thread.State#TIMED_WAITING }
	 * {@link java.lang.management.ThreadInfo#getWaitedCount()}  }
	 */
	private long waitCount;
	/**
	 * Number of times this thread was in {@link Thread.State#BLOCKED }
	 * {@link java.lang.management.ThreadInfo#getBlockedCount()}  }
	 */
	private long blockCount;
	/**
	 * {@link java.lang.management.ThreadInfo#getWaitedTime()}  }
	 */
	private long waitedTime;
	private long blockedTime;
	private long threadCpuTime;
	private long threadUserTime;

	private boolean deadlocked = false;

	public ThreadInfo(long threadId, String threadName, String state) {
		this.threadId = threadId;
		this.threadName = threadName;
		this.state = state;
	}

	public ThreadInfo setCounts(long waitCount, long blockCount) {
		this.waitCount = waitCount;
		this.blockCount = blockCount;
		return this;
	}

	public ThreadInfo setTimes(long waitedTime, long blockedTime, long threadCpuTime, long threadUserTime) {
		this.waitedTime = waitedTime;
		this.blockedTime = blockedTime;
		this.threadCpuTime = threadCpuTime;
		this.threadUserTime = threadUserTime;
		return this;
	}

	public String toJSON() {
		return JSON.writePairs(new String[]{
						"header",
						"counts",
						"times",
						"stack"},
				new Object[]{
						JSON.writePairs(new String[]{"id", "name", "state", "deadlocked"}, new Object[]{threadId, threadName, state, deadlocked}),
						JSON.writePairs(new String[]{"wait", "block"}, new Object[]{waitCount, blockCount}),
						JSON.writePairs(new String[]{"wait", "block", "cpu", "userCpu"}, new Object[]{
								toReadableTime(waitedTime, TimeUnit.MILLISECONDS),
								toReadableTime(blockedTime, TimeUnit.MILLISECONDS),
								toReadableTime(threadCpuTime, TimeUnit.NANOSECONDS),
								toReadableTime(threadUserTime, TimeUnit.NANOSECONDS)}),
						JSON.writeList(stackElements)
				});
	}

	private String toReadableTime(long time, TimeUnit baseTimeUnit) {
		if (time == -1) {
			return "0";
		}
		long formatedTime = baseTimeUnit.toMillis(time);
		if (formatedTime > MS_TIMEUNIT_CAP) {
			formatedTime = baseTimeUnit.toSeconds(time);
			if (formatedTime > TIMEUNIT_CAP) {
				formatedTime = baseTimeUnit.toMinutes(time);
				if (formatedTime > TIMEUNIT_CAP) {
					formatedTime = baseTimeUnit.toHours(time);
					if (formatedTime > TIMEUNIT_CAP) {
						formatedTime = baseTimeUnit.toDays(time);
						return formatedTime + "d";
					}
				} else {
					return formatedTime + "h";
				}
			} else {
				return formatedTime + "s";
			}
		}
		return formatedTime + "ms";
	}

	public void setStackTrace(StackTraceElement[] stackTrace) {
		this.stackElements = new ArrayList<>(stackTrace.length);
		for (StackTraceElement nativeStackTraceElement : stackTrace) {
			final StackElement stackElement = new StackElement();
			stackElement.setTraceLine(nativeStackTraceElement.toString());
			stackElements.add(stackElement);
		}
	}

	public void setDeadlocked(boolean deadlocked) {
		this.deadlocked = deadlocked;
	}
}
