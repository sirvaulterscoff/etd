package etd.rest;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author pobedenniy.alexey
 * @since 03.11.2015
 */
public class JSON {
	public static final String ERROR = "{\"err\":\"%s\"}";
	public static final String PAIR = "\"%s\":\"%s\"";
	public static final String PAIR_NO_VALUE_QUOTES = "\"%s\":%s";

	public static String renderError(String err, String... args) {
		return String.format(ERROR, String.format(err, args));
	}

	public static String writePairs(String[] names, Object[] data) {
		StringBuilder sb = new StringBuilder("{");
		final int length = names.length;
		for (int i = 0; i < length; i++) {
			String name = names[i];
			Object val = data[i];
			writePairItem(sb, name, val);
			if (i < length - 1) {
				sb.append(",");
			}
		}
		return sb.append("}").toString();
	}

	public static String ok() {
		return "";
	}

	public static String writeList(List<? extends JSONObject> result) {
		StringBuilder sb = new StringBuilder("[");
		for (int i = 0; i < result.size(); i++) {
			JSONObject info = result.get(i);
			sb.append(info.toJSON());
			if (i < result.size() - 1) {
				sb.append(",");
			}
		}
		return sb.append("]").toString();
	}

	public static String writeToStringList(List<String> strings) {
		StringBuilder sb = new StringBuilder("[");
		for (int i = 0; i < strings.size(); i++) {
			String info = strings.get(i);
			sb.append("\"").append(info).append("\"");
			if (i < strings.size() - 1) {
				sb.append(",");
			}
		}
		return sb.append("]").toString();
	}

	public static <K, V> String writeMap(Map<K, V> map) {
		StringBuilder sb = new StringBuilder("{");
		Iterator<Map.Entry<K, V>> entrySetIterator = map.entrySet().iterator();
		while (entrySetIterator.hasNext()) {
			Map.Entry<K, V> entry = entrySetIterator.next();
			final Object key = entry.getKey();
			String strKey;
			if (key instanceof String) {
				strKey = (String) key;
			} else if (key instanceof JSONObject) {
				strKey = ((JSONObject) key).toJSON();
			} else {
				strKey = key.toString();
			}
			writePairItem(sb, strKey, entry.getValue());
			if (entrySetIterator.hasNext()) {
				sb.append(",");
			}
		}
		return sb.append("}").toString();
	}

	private static void writePairItem(StringBuilder sb, String name, Object val) {
		if (val instanceof String &&
		    (((String) val).startsWith("{") || ((String) val).startsWith("["))) {
			//don't quote inner JSON
			sb.append(String.format(PAIR_NO_VALUE_QUOTES, name, val));
		} else if (val instanceof JSONObject) {
			sb.append(String.format(PAIR_NO_VALUE_QUOTES, name, ((JSONObject) val).toJSON()));
		} else {
			sb.append(String.format(PAIR, name, val));
		}
	}
}
