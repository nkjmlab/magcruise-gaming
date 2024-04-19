package org.magcruise.gaming.ui;

import java.util.LinkedHashMap;
import java.util.Map;

public class HtmlUtils {
	public static String tabulate(String[] headers, Object... values) {
		Map<String, Object> tmp = new LinkedHashMap<>();
		for (int i = 0; i < headers.length; i++) {
			tmp.put(headers[i], values[i]);
		}
		return tabulate(tmp);
	}

	public static String tabulate(Map<String, Object> keyAndVals) {
		String str = "<div class='table-responsive-sm'>";
		str += "<table class='table table-bordered table-striped table-sm'>";
		for (String key : keyAndVals.keySet()) {
			str += "<th>" + key + "</th>";
		}
		str += "</tr>";

		for (String key : keyAndVals.keySet()) {
			str += "<td>" + keyAndVals.get(key) + "</td>";
		}
		str += "</tr>";
		str += "</table></div>";
		return str;

	}
}
