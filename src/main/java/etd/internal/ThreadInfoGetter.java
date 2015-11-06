package etd.internal;

import etd.model.ThreadDump;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.LinkedList;
import java.util.List;

/**
 * @author pobedenniy.alexey
 * @since 03.11.2015
 */
public class ThreadInfoGetter {
	private static  ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
	public ThreadDump getThreadDump(VmInfo vmInfo) {
		return getThreadDump(vmInfo.includeMonitors, vmInfo.synchronizerUsageSupported, vmInfo.threadContentionMonitoringEnabled, vmInfo.threadCpuTimeEnabled);
	}

	/**
	 * @param includeMonitors                       this option enables collection of monitors for threads as well as monitor deadlock detection,
	 *                                              produced by {@link ThreadMXBean#findMonitorDeadlockedThreads()}  }
	 * @param isSynchronizerUsageSupported          this option enables collection of locks info for threads, as well as deadlock detection.
	 *                                              Can only be enabled if {@link ThreadMXBean#isSynchronizerUsageSupported()}  }
	 * @param isThreadContentionMonitoringSupported determines whether to include info about waitedTime and blockedTime for each thread.
	 *                                              Can only be enabled if {@link ThreadMXBean#isThreadContentionMonitoringSupported()}  }
	 * @param isThreadCpuTimeSupported              determines whether to include info about total thread CPU/user time.
	 *                                              Can only be enabled if {@link ThreadMXBean#isThreadCpuTimeSupported()}  }
	 * @return
	 */
	private ThreadDump getThreadDump(
			boolean includeMonitors,
			boolean isSynchronizerUsageSupported,
			boolean isThreadContentionMonitoringSupported,
			boolean isThreadCpuTimeSupported) {
		long[] deadLockThreads = isSynchronizerUsageSupported ? mxBean.findDeadlockedThreads() : new long[0];
		List<etd.model.ThreadInfo> result = new LinkedList<>();
		int blocked = 0, waiting = 0, runnabled = 0;
		for (ThreadInfo systemThreadInfo : mxBean.dumpAllThreads(includeMonitors, isSynchronizerUsageSupported)) {
			final long threadId = systemThreadInfo.getThreadId();
			switch (systemThreadInfo.getThreadState()) {
				case BLOCKED:
					blocked++;
					break;
				case WAITING:
				case TIMED_WAITING:
					waiting++;
					break;
				case RUNNABLE:
					runnabled++;
					break;
			}
			etd.model.ThreadInfo threadInfo = getThreadInfo(isThreadContentionMonitoringSupported, isThreadCpuTimeSupported, systemThreadInfo);
			result.add(threadInfo);
		}

		return new ThreadDump(result)
				.setCounts(
						mxBean.getTotalStartedThreadCount(),
						mxBean.getDaemonThreadCount(),
						mxBean.getPeakThreadCount(),
						mxBean.getThreadCount(),
						blocked,
						waiting,
						runnabled)
				.setDeadlocks(deadLockThreads);
	}

	public String getThreadStack(VmInfo vmInfo, String threadId) {
		ThreadInfo systemThreadInfo = mxBean.getThreadInfo(Long.valueOf(threadId), Integer.MAX_VALUE);
		return getThreadInfo(vmInfo.threadContentionMonitoringEnabled, vmInfo.threadCpuTimeEnabled, systemThreadInfo).toJSON();
	}

	private etd.model.ThreadInfo getThreadInfo(boolean isThreadContentionMonitoringSupported, boolean isThreadCpuTimeSupported, ThreadInfo systemThreadInfo) {
		long threadId = systemThreadInfo.getThreadId();
		etd.model.ThreadInfo threadInfo = new etd.model.ThreadInfo(threadId, systemThreadInfo.getThreadName(), systemThreadInfo.getThreadState().name());
		threadInfo
				.setCounts(
						systemThreadInfo.getWaitedCount(),
						systemThreadInfo.getBlockedCount())
				.setTimes(
						isThreadContentionMonitoringSupported ? systemThreadInfo.getWaitedTime() : -1,
						isThreadContentionMonitoringSupported ? systemThreadInfo.getBlockedTime() : -1,
						isThreadCpuTimeSupported ? mxBean.getThreadCpuTime(threadId) : -1,
						isThreadCpuTimeSupported ? mxBean.getThreadUserTime(threadId) : -1
				)
				.setStackTrace(systemThreadInfo.getStackTrace());
		return threadInfo;
	}
}
