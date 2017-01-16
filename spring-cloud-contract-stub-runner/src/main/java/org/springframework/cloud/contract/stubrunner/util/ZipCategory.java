/*
 *  Copyright 2013-2017 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.contract.stubrunner.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.util.StreamUtils;

/**
 * Based on
 * <a href="https://github.com/timyates/groovy-common-extensions">https://github.com/
 * timyates/groovy-common-extensions</a>.
 *
 * Category for {@link File} that adds a method that allows you to unzip a given file to a
 * specified location
 *
 */
public class ZipCategory {

	/**
	 * Unzips this file. If the <tt>destination</tt> directory is not provided, it will
	 * fall back to this file's parent directory.
	 *
	 * @param self
	 * @param destination (optional), the destination directory where this file's content
	 * will be unzipped to.
	 * @return a {@link Collection} of unzipped {@link File} objects.
	 */
	public static Collection<File> unzipTo(File self, File destination) {
		checkUnzipDestination(destination);
		// if destination directory is not given, we'll fall back to the parent directory
		// of 'self'
		if (destination == null)
			destination = new File(self.getParent());
		List<File> unzippedFiles = new ArrayList<>();
		try {
			ZipInputStream zipInput = new ZipInputStream(new FileInputStream(self));
			for (ZipEntry entry = zipInput.getNextEntry(); entry != null; entry = zipInput
					.getNextEntry()) {
				if (!entry.isDirectory()) {
					final File file = new File(destination, entry.getName());
					if (file.getParentFile() != null) {
						file.getParentFile().mkdirs();
					}
					try (FileOutputStream output = new FileOutputStream(file)) {
						StreamUtils.copy(zipInput, output);
					}
					unzippedFiles.add(file);
				}
				else {
					final File dir = new File(destination, entry.getName());
					dir.mkdirs();
					unzippedFiles.add(dir);
				}
			}
		}
		catch (IOException e) {
			throw new IllegalStateException("Cannot unzip archive", e);
		}
		return unzippedFiles;
	}

	private static void checkUnzipDestination(File file) {
		if (file != null && !file.isDirectory())
			throw new IllegalArgumentException("'destination' has to be a directory.");
	}
}
