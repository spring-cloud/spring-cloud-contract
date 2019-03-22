/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.verifier.util

import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

/**
 * A utility class that helps to convert names
 *
 * @author Jakub Kubrynski, codearte.io
 *
 * @since 1.0.0
 */
class NamesUtil {

	/**
	 * Returns the first element before the last separator presence.
	 * @return empty string if separator is not found.
	 */
	static String beforeLast(String string, String separator) {
		if (string?.indexOf(separator) > -1) {
			return string.substring(0, string.lastIndexOf(separator))
		}
		return ''
	}

	/**
	 * Returns the first element after the last separator presence
	 * @return the provided string if separator is not found.
	 */
	static String afterLast(String string, String separator) {
		if (hasSeparator(string, separator)) {
			return string.substring(string.lastIndexOf(separator) + 1)
		}
		return string
	}

	/**
	 * Returns {@code true} if has a separatot in the string
	 */
	static boolean hasSeparator(String string, String separator) {
		return string?.indexOf(separator) > -1
	}

	/**
	 * Returns the first element after the last dot presence
	 * @return the provided string if separator is not found.
	 */
	static String afterLastDot(String string) {
		return afterLast(string, '.')
	}

	/**
	 * @return {@code true} if has a dot
	 */
	static boolean hasDot(String string) {
		return hasSeparator(string, '.')
	}

	/**
	 * @param file - file with contracts
	 * @param contracts - collection of contracts
	 * @param counter - given contract index
	 * @return the default contract name, resolved from file name, taking
	 * 	 * into consideration also the index of contract (for multiple contracts
	 * 	 * stored in a single file).
	 */
	static String defaultContractName(File file, Collection contracts, int counter) {
		String tillExtension = file.name.substring(0, file.name.lastIndexOf("."))
		return tillExtension + (counter > 0 || contracts.size() > 1 ? "_" + counter : "")
	}

	/**
	 * Converts a string into a camel case format
	 */
	static String camelCase(String className) {
		if (!className) {
			return className
		}
		String firstChar = className.charAt(0).toLowerCase() as String
		return firstChar + className.substring(1)
	}

	/**
	 * Capitalizes the provided string
	 */
	static String capitalize(String className) {
		if (!className) {
			return className
		}
		String firstChar = className.charAt(0).toUpperCase() as String
		return firstChar + className.substring(1)
	}

	/**
	 * Returns the whole string to the last present dot.
	 * @return input string if there is no dot
	 */
	static String toLastDot(String string) {
		if (string?.indexOf('.') > -1) {
			return string.substring(0, string.lastIndexOf('.'))
		}
		return string
	}

	/**
	 * Converts the Java package notation to a path format
	 */
	static String packageToDirectory(String packageName) {
		return packageName.replace('.' as char, File.separatorChar)
	}

	/**
	 * Converts the path format to a Java package notation
	 */
	static String directoryToPackage(String directory) {
		return directory
				.replace('.', '_')
				.replace(File.separator, '.')
				.replaceAll('\\.([0-9])', '._$1')
				.replaceAll('^([0-9].*)', '_$1')
	}

	/**
	 * Traverses the directories and converts renames illegal folder names
	 * to package names
	 *
	 * @param rootDir - folder from which to start traversing
	 */
	static void recrusiveDirectoryToPackage(File rootDir) {
		try {
			if (!rootDir.exists()) {
				return
			}
			InvalidFolderRenamer renamer = new InvalidFolderRenamer()
			Files.walkFileTree(rootDir.toPath(), renamer)
			renamer.rename()
		}
		catch (IOException ex) {
			throw new IllegalStateException(ex)
		}
	}

	private static class InvalidFolderRenamer extends SimpleFileVisitor<Path> {

		private Deque<FileAndNewName> filesToRename = new ArrayDeque<>()

		@Override
		FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
			String name = dir.toFile().getName()
			String convertedName = directoryToPackage(name)
			if (name != convertedName) {
				this.filesToRename.
						addFirst(new FileAndNewName(dir.toFile(), convertedName))
			}
			return FileVisitResult.CONTINUE
		}

		void rename() {
			this.filesToRename.each {
				it.file.renameTo(new File(it.file.parentFile, it.newName))
			}
		}
	}

	private static class FileAndNewName {
		private final File file
		private final String newName

		private FileAndNewName(File file, String newName) {
			this.file = file
			this.newName = newName
		}
	}

	/**
	 * Converts illegal package characters to underscores
	 */
	static String convertIllegalPackageChars(String packageName) {
		return packageName.replaceAll('[_\\- .+]', '_')
	}

	/**
	 * Converts illegal characters in method names to underscores
	 */
	static String convertIllegalMethodNameChars(String methodName) {
		String result = methodName.replaceAll('^[^a-zA-Z_$0-9]', '_')
		return result.replaceAll('[^a-zA-Z_$0-9]', '_')
	}
}
