package io.codearte.accurest.stubrunner.util

import groovy.util.logging.Slf4j
import spock.lang.Specification

@Slf4j
class ZipCategorySpec extends Specification {

	def 'should unzip a file to the specified location'() {
		given:
		File zipFile = new File(ZipCategorySpec.classLoader.getResource('file.zip').toURI())
		File tempDir = File.createTempDir()
		tempDir.deleteOnExit()
		when:
		use(ZipCategory) {
			zipFile.unzipTo(tempDir)
		}
		then:
		tempDir.listFiles().find {
			it.name == 'file.txt'
		}?.text?.trim() == 'test'
	}

}
