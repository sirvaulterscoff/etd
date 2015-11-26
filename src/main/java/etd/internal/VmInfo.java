package etd.internal;

import etd.rest.JSON;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * @author pobedenniy.alexey
 * @since 03.11.2015
 */
public class VmInfo {
	final boolean synchronizerUsageSupported;
	final boolean threadContentionMonitoringSupported;
	final boolean threadCpuTimeSupported;
	final boolean objectMonitorUsageSupported;

	boolean threadContentionMonitoringEnabled;
	boolean threadCpuTimeEnabled;
	public boolean includeMonitors = false;

	private static final VmInfo INSTANCE = new VmInfo();
	private final ThreadMXBean mxBean;

	public VmInfo() {
		mxBean = ManagementFactory.getThreadMXBean();
		synchronizerUsageSupported = mxBean.isSynchronizerUsageSupported();
		threadContentionMonitoringSupported = mxBean.isThreadContentionMonitoringSupported();
		threadCpuTimeSupported = mxBean.isThreadCpuTimeSupported();
		objectMonitorUsageSupported = mxBean.isObjectMonitorUsageSupported();

		threadContentionMonitoringEnabled = mxBean.isThreadContentionMonitoringEnabled();
		threadCpuTimeEnabled = mxBean.isThreadCpuTimeEnabled();
	}

	public static VmInfo getVmInfo() {
		return INSTANCE;
	}

	public String toJSON() {
		return JSON.writePairs(
				new String[] {"synchSuported", "contentionSupported", "cpuSupported", "monitorUsageSupported", "contentionEnabled", "cpuEnabled"},
				new Object[] {synchronizerUsageSupported, threadContentionMonitoringSupported, threadCpuTimeSupported, objectMonitorUsageSupported, threadContentionMonitoringEnabled, threadCpuTimeEnabled});
	}

	public void setStringParam(String param, String value) {
		switch (param) {
			case "contentionEnabled":
				mxBean.setThreadContentionMonitoringEnabled(threadContentionMonitoringEnabled = threadContentionMonitoringSupported && "true".equals(value));
				break;
			case "cpuEnabled":
				mxBean.setThreadCpuTimeEnabled(threadCpuTimeEnabled = threadCpuTimeSupported && "true".equals(value));
				break;
			case "monitorsEnabled":
				includeMonitors = "true".equals(value) && objectMonitorUsageSupported;
		}
	}
}
