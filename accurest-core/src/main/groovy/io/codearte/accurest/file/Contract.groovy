package io.codearte.accurest.file

import java.nio.file.Path

/**
 * @author Jakub Kubrynski
 */
class Contract {
	final Path path;
	final boolean ignored;
	final int groupSize
	final Integer order;

	Contract(Path path, boolean ignored, int groupSize, Integer order) {
		this.groupSize = groupSize
		this.path = path
		this.ignored = ignored
		this.order = order
	}

	@Override
	public String toString() {
		return "Contract{" +
				"fileName=" + path.fileName +
				", ignored=" + ignored +
				", groupSize=" + groupSize +
				", order=" + order +
				'}';
	}
}
