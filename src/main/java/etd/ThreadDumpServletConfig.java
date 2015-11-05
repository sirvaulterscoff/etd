package etd;

import etd.rest.JSON;
import etd.rest.JSONObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author pobedenniy.alexey
 * @since 05.11.2015
 */
public class ThreadDumpServletConfig {
	PackageInfo projectPackages;
	Map<String, PackageInfo> libsMappings;
	static Logger log = Logger.getLogger(ThreadDumpServletConfig.class.getName());

	public ThreadDumpServletConfig(String ourPackages, String libsMappings) {
		this.projectPackages = parseProjectPackages(ourPackages);
		this.libsMappings = parseLibsMappings(libsMappings);
	}

	private Map<String, PackageInfo> parseLibsMappings(String libsMappings) {
		if (libsMappings == null) {
			return Collections.emptyMap();
		}
		String[] libItems = libsMappings.split(";");
		Map<String, PackageInfo> result = new HashMap<>(libItems.length);
		for (String libItem : libItems) {
			//if trailing ; in config
			if (libItem == null || libItem.length() < 3) {
				continue;
			}
			String[] libDefPart = libItem.split("=");
			if (libDefPart.length != 2) {
				log.log(Level.WARNING, "Error in library mapping definition. String \"" + libItem + "\" should be in form of" +
				                       "<library_name>=<library_package>,<library_package>,...;");
				continue;
			}
			String lhs = libDefPart[0].trim();
			String rhs = libDefPart[1].trim();
			String[] pacakgesToInclude = rhs.split(",");
			result.put(lhs, new PackageInfo(pacakgesToInclude));
		}
		return result;
	}

	private PackageInfo parseProjectPackages(String ourPackages) {
		if (ourPackages == null) {
			return new PackageInfo(new String[0]);
		}
		return new PackageInfo(ourPackages.split(";"));
	}

	public String toJSON() {
		return JSON.writePairs(
				new String[] {"local", "libs"},
				new Object[]{
						projectPackages.toJSON(),
						JSON.writeMap(libsMappings)
				});
	}

	private class PackageInfo implements JSONObject {
		private final List<String> packages;

		public PackageInfo(String[] pacakgesToInclude) {
			this.packages = Arrays.asList(pacakgesToInclude);
		}

		@Override
		public String toJSON() {
			return JSON.writeToStringList(packages);
		}
	}
}
