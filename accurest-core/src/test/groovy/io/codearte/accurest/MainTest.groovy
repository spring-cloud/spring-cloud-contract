package io.codearte.accurest

import io.coderate.accurest.TestGenerator
import io.coderate.accurest.config.AccurestConfigProperties
import io.coderate.accurest.config.TestFramework
import io.coderate.accurest.config.TestMode

class MainTest {
	public static void main(String[] args) {
		AccurestConfigProperties properties = new AccurestConfigProperties(stubsBaseDirectory: '/home/devel/workspace/accurest/accurest-core/src/main/resources/stubs',
				targetFramework: TestFramework.SPOCK, testMode: TestMode.MOCKMVC, basePackageForTests: 'io.test',
                staticImports: ['com.pupablada.Test.*'], imports: ['org.innapypa.Test'], ignoredFiles: ["**/different/some.json", "**/other"])
		println new TestGenerator(properties).generate()
	}
}
