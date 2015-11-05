package etd.model;

import etd.rest.JSON;
import etd.rest.JSONObject;

/**
 * @author pobedenniy.alexey
 * @since 03.11.2015
 */
public class StackElement implements JSONObject {
	private String trace;

	public void setTraceLine(String trace) {
		this.trace = trace;
	}

	@Override
	public String toJSON() {
		return JSON.writePairs(new String[]{"line"}, new Object[]{trace});
	}
}
