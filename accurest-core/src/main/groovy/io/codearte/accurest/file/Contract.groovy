package io.codearte.accurest.file

import java.nio.file.Path

/**
 * @author Jakub Kubrynski
 */
class Contract {
	final Path path;
	final boolean ignored;

	Contract(Path path, boolean ignored) {
		this.path = path
		this.ignored = ignored
	}

}
