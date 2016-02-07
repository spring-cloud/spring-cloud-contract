package io.codearte.accurest

import io.codearte.accurest.config.AccurestConfigProperties
import io.codearte.accurest.config.TestFramework
import io.codearte.accurest.config.TestMode

class MainTest {
	public static void main(String[] args) {
		AccurestConfigProperties properties = new AccurestConfigProperties(
				contractsDslDir: new File('/home/devel/projects/codearte/accurest/accurest-core/src/test/resources/dsl'),
				generatedTestSourcesDir: new File('/tmp/accurest'),
				targetFramework: TestFramework.SPOCK, testMode: TestMode.MOCKMVC, basePackageForTests: 'io.test',
				staticImports: ['com.pupablada.Test.*'], imports: ['org.innapypa.Test'], excludedFiles: ["**/other"])
		println new TestGenerator(properties).generate()
	}
}
