package eu.coderate.accurest.util

/**
 * @author Jakub Kubrynski
 */
class NamesUtil {
	static String getName(String qualifiedName) {
		if (qualifiedName?.indexOf('.') > -1) {
			return qualifiedName.substring(qualifiedName.lastIndexOf('.')+1)
		}
		return qualifiedName
	}

	static String fieldName(String className) {
		if (className) {
			return className
		}
		String name = getName(className)
		String firstChar = name.charAt(0).toLowerCase() as String
		return firstChar + name.substring(1)
	}
}
