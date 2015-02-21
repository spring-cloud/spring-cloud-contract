package io.codearte.accurest

import io.coderate.accurest.TestGenerator
import io.coderate.accurest.config.AccurestConfigProperties
import io.coderate.accurest.config.TestFramework
import io.coderate.accurest.config.TestMode

class MainTest {
	public static void main(String[] args) {
		AccurestConfigProperties properties = new AccurestConfigProperties(
				stubsBaseDirectory: new File('/home/devel/projects/codearte/accurest/accurest-core/src/test/resources/dsl'),
				generatedTestSourcesDir: new File('/tmp/accurest'),
				targetFramework: TestFramework.SPOCK, testMode: TestMode.MOCKMVC, basePackageForTests: 'io.test',
				staticImports: ['com.pupablada.Test.*'], imports: ['org.innapypa.Test'], ignoredFiles: ["**/other"])
		println new TestGenerator(properties).generate()
	}
}
