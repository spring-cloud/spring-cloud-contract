package io.codearte.accurest.stubrunner.util

import groovy.transform.CompileStatic

import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * Based on <a href="https://github.com/timyates/groovy-common-extensions">https://github.com/timyates/groovy-common-extensions</a>.
 *
 * Category for {@link File} that adds a method that allows you to unzip
 * a given file to a specified location
 *
 */
@CompileStatic
class ZipCategory {

	/**
	 * Unzips this file. If the <tt>destination</tt>
	 * directory is not provided, it will fall back to this file's parent directory.
	 *
	 * @param self
	 * @param destination (optional), the destination directory where this file's content will be unzipped to.
	 * @return a {@link Collection} of unzipped {@link File} objects.
	 */
	static Collection<File> unzipTo(File self, File destination) {
		checkUnzipDestination(destination)
		// if destination directory is not given, we'll fall back to the parent directory of 'self'
		if (destination == null) destination = new File(self.parent)
		List<File> unzippedFiles = []
		final ZipInputStream zipInput = new ZipInputStream(new FileInputStream(self))
		zipInput.withStream {
			ZipEntry entry
			while (entry = zipInput.nextEntry) {
				if (!entry.isDirectory()) {
					final File file = new File(destination, entry.name)
					file.parentFile?.mkdirs()
					FileOutputStream output = new FileOutputStream(file)
					output.withStream {
						output << zipInput
					}
					unzippedFiles << file
				} else {
					final File dir = new File(destination, entry.name)
					dir.mkdirs()
					unzippedFiles << dir
				}
			}
		}
		return unzippedFiles
	}

	private static void checkUnzipDestination(File file) {
		if (file && !file.isDirectory()) throw new IllegalArgumentException("'destination' has to be a directory.")
	}
}
