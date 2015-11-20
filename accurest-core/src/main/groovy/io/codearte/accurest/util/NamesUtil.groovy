package io.codearte.accurest.util

/**
 * @author Jakub Kubrynski
 */
class NamesUtil {

	static String beforeLast(String string, String separator) {
		if (string?.indexOf(separator) > -1) {
			return string.substring(0, string.lastIndexOf(separator))
		}
		return ''
	}

	static String afterLast(String string, String separator) {
		if (string?.indexOf(separator) > -1) {
			return string.substring(string.lastIndexOf(separator) + 1)
		}
		return string
	}

	static String afterLastDot(String string) {
		return afterLast(string, '.')
	}

	static String camelCase(String className) {
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

	static String packageToDirectory(String packageName) {
		return packageName.replace('.' as char, File.separatorChar)
	}

	static String directoryToPackage(String directory) {
		return directory.replace(File.separator, '.')
	}
}
