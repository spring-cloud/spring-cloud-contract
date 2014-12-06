package eu.coderate.accurest.util

/**
 * @author Jakub Kubrynski
 */
class NamesUtil {
	static String afterLastDot(String string) {
		if (string?.indexOf('.') > -1) {
			return string.substring(string.lastIndexOf('.')+1)
		}
		return string
	}

	static String uncapitalize(String className) {
		if (!className) {
			return className
		}
		String firstChar = className.charAt(0).toLowerCase() as String
		return firstChar + className.substring(1)
	}

	static String capitalize(String className) {
		if (!className) {
			return className
		}
		String firstChar = className.charAt(0).toUpperCase() as String
		return firstChar + className.substring(1)
	}

	static String toLastDot(String string) {
		if (string?.indexOf('.') > -1) {
			return string.substring(0, string.lastIndexOf('.'))
		}
		return string
	}
}
