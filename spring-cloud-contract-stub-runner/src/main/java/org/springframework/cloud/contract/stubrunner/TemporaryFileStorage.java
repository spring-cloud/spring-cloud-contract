/*
 * Copyright 2013-2020 the original author or authors.
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

package org.springframework.cloud.contract.stubrunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Stores all generated temporary folders with stubs.
 *
 * @author Marcin Grzejszczak
 * @since 2.0.0
 */
final class TemporaryFileStorage {

	private static final Log log = LogFactory.getLog(TemporaryFileStorage.class);

	private static final int TEMP_DIR_ATTEMPTS = 10000;

	/**
	 * There are problems with removal of stubs unpacked to a temporary folder. That's why
	 * we're creating a bounded in-memory storage of unpacked files and later we register
	 * a shutdown hook to remove all these files.
	 */
	private static final BlockingQueue<File> TEMP_FILES_LOG = new LinkedBlockingQueue<>(20_000);

	private TemporaryFileStorage() {
		throw new IllegalStateException("Can't instantiate a utility class");
	}

	static void add(File file) {
		TEMP_FILES_LOG.offer(file);
	}

	static Queue<File> files() {
		return TEMP_FILES_LOG;
	}

	static void cleanup(boolean deleteStubsAfterTest) {
		if (!deleteStubsAfterTest) {
			log.info("Will not clear temporary files due to switch");
			return;
		}
		for (File file : TemporaryFileStorage.files()) {
			try {
				if (file.isDirectory()) {
					Files.walkFileTree(file.toPath(), new SimpleFileVisitor<Path>() {
						@Override
						public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
							if (log.isTraceEnabled()) {
								log.trace("Removing file [" + file + "]");
							}
							Files.delete(file);
							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
							if (log.isTraceEnabled()) {
								log.trace("Removing dir [" + dir + "]");
							}
							Files.delete(dir);
							return FileVisitResult.CONTINUE;
						}
					});
				}
				else {
					Files.delete(file.toPath());
				}
			}
			catch (NoClassDefFoundError | IOException e) {
				// Added NoClassDefFoundError cause sometimes it's visible in the builds
				// this error is completely harmless
				if (log.isTraceEnabled()) {
					log.trace("Failed to remove temporary file", e);
				}
			}
			TemporaryFileStorage.files().clear();
		}
	}

	// taken from Guava
	static File createTempDir(String tempDirPrefix) {
		File baseDir = new File(System.getProperty("java.io.tmpdir"));
		String baseName = tempDirPrefix + "-" + System.currentTimeMillis() + "-";
		for (int counter = 0; counter < TEMP_DIR_ATTEMPTS; counter++) {
			File tempDir = new File(baseDir, baseName + counter);
			if (tempDir.mkdir()) {
				return tempDir;
			}
		}
		throw new IllegalStateException("Failed to create directory within " + TEMP_DIR_ATTEMPTS + " attempts (tried "
				+ baseName + "0 to " + baseName + (TEMP_DIR_ATTEMPTS - 1) + ")");
	}

}
