package etd.rest;

import etd.ThreadDumpServletConfig;
import etd.internal.ThreadInfoGetter;
import etd.internal.VmInfo;
import etd.util.Preconditions;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author pobedenniy.alexey
 * @since 03.11.2015
 */
public class RestProcessor {
	static final Logger LOG = Logger.getLogger(RestProcessor.class.getName());
	public static final Pattern PATTERN = Pattern.compile("(?<grp>[a-zA-z]{3,})/?((?<mtd>[a-zA-Z]{2,})/(?<data>[a-zA-Z\\d]*))?");
	private String result;

	public RestProcessor(String requestType, String restUri, ThreadDumpServletConfig servletConfig) {
		Matcher matcher = PATTERN.matcher(restUri);

		Preconditions.checkTrue(matcher.matches(), "Wrong request to REST API");

		String group = matcher.group("grp");
		String methodStr = matcher.group("mtd");
		String dataStr = matcher.group("data");

		Preconditions.checkNotNull(group, "Wrong request to REST");

		try {
			Method method = this.getClass().getDeclaredMethod(requestType.toLowerCase() + group.substring(0, 1).toUpperCase() + group.substring(1), ThreadDumpServletConfig.class, String.class, String.class);
			result = (String) method.invoke(this, servletConfig, methodStr, dataStr);
		} catch (NoSuchMethodException e) {
			result = JSON.renderError("Method %s not found on REST API", group);
			LOG.log(Level.SEVERE, "Method not found on REST API", e);
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to invoke REST API", e);
			result = JSON.renderError("Failed to invoke REST API: %s", e.getMessage());
		}
	}

	public void process(HttpServletResponse resp) throws IOException {
		resp.setContentType("application/json");
		resp.getWriter().write(result);
	}

	@SuppressWarnings("unused")
	public String getVmInfo(ThreadDumpServletConfig servletConfig, String ignore, String ignored) {
		return VmInfo.getVmInfo().toJSON();
	}

	public String putVmInfo(ThreadDumpServletConfig servletConfig, String param, String value) {
		VmInfo.getVmInfo().setStringParam(param, value);
		return JSON.ok();
	}

	public String getStacks(ThreadDumpServletConfig servletConfig, String ignore, String ingored) {
		final VmInfo vmInfo = VmInfo.getVmInfo();
		return new ThreadInfoGetter().getThreadDump(vmInfo).toJSON();
	}

	public String getPrefs(ThreadDumpServletConfig servletConfig, String ignore, String ingored) {
		return servletConfig.toJSON();
	}
}
