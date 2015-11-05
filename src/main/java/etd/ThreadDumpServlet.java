package etd;

import etd.rest.RestProcessor;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author pobedenniy.alexey
 * @since 03.11.2015
 */
public class ThreadDumpServlet extends HttpServlet {
	private static final int BUFFER_SIZE = 4096;
	public static final String OUR_PACKAGES_PARAM = "etd_our_packages";
	public static final String LIBS_MAPPING_PARAM = "etd_libs_mapping";

	private ThreadDumpServletConfig servletConfig;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		String ourPackages = config.getInitParameter(OUR_PACKAGES_PARAM);
		String libsMapping = config.getInitParameter(LIBS_MAPPING_PARAM);
		servletConfig = new ThreadDumpServletConfig(ourPackages, libsMapping);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (isRoot(req)) {
			final ServletOutputStream servletOutputStream = resp.getOutputStream();
			final InputStream pageStream = getClass().getResourceAsStream("/etd/web/main.html");
			transferBytes(servletOutputStream, pageStream);
		} else if (isStatic(req)) {
			String request = req.getPathInfo().replace("/static/", "").replace("..", "");
			InputStream resourceStream = getClass().getResourceAsStream("/etd/web/" + request);
			if (resourceStream == null) {
				resp.sendError(404);
				return;
			}
			final ServletOutputStream servletOutputStream = resp.getOutputStream();
			transferBytes(servletOutputStream, resourceStream);
		} else {
			String restUri = getRestURI(req);
			new RestProcessor(req.getMethod(), restUri, servletConfig).process(resp);
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String restUri = getRestURI(req);
		new RestProcessor(req.getMethod(), restUri, servletConfig).process(resp);
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String restUri = getRestURI(req);
		new RestProcessor(req.getMethod(), restUri, servletConfig).process(resp);
	}

	private String getRestURI(HttpServletRequest req) {
		return isRest(req) ? req.getPathInfo().replace("/rest/", "") : null;
	}

	private boolean isRest(HttpServletRequest req) {
		return req.getPathInfo() != null && req.getPathInfo().startsWith("/rest/");
	}

	private boolean isStatic(HttpServletRequest req) {
		return req.getPathInfo() != null && req.getPathInfo().startsWith("/static/");
	}

	private boolean isRoot(HttpServletRequest req) {
		return req.getPathInfo() == null ||
		       "/".equals(req.getPathInfo()) ||
		       req.getPathInfo().endsWith("/index.html");
	}


	private void transferBytes(ServletOutputStream servletOutputStream, InputStream resourceAsStream) throws IOException {
		int result;
		do {
			int bufferSize = Math.min(resourceAsStream.available(), BUFFER_SIZE);
			byte[] buff = new byte[bufferSize];
			result = resourceAsStream.read(buff);
			servletOutputStream.write(buff);
		} while (result > 0);
	}
}
