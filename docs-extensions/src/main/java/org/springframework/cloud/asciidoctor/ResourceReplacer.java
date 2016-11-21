package org.springframework.cloud.asciidoctor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Marcin Grzejszczak
 */
class ResourceReplacer {

	String replaceOutput(String output) {
		String css = resourceToText("/codeBlockSwitch.css");
		String javascript = resourceToText("/codeBlockSwitch.js");
		String replacement = "<style>\n" + css + "\n</style>\n"
				+ "\n<script src=\"http://cdnjs.cloudflare.com/ajax/libs/zepto/1.1.6/zepto.min.js\"></script>\n"
				+ "<script type=\"text/javascript\">\n" + javascript + "\n</script>\n"
				+ "</head>";
		return output.replace("</head>", replacement);
	}

	private String resourceToText(String name) {
		return getStringFromInputStream(getClass().getResourceAsStream(name));
	}

	private static String getStringFromInputStream(InputStream is) {
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
		String line;
		try {
			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			return "";
		}
		finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					return "";
				}
			}
		}
		return sb.toString();
	}
}
