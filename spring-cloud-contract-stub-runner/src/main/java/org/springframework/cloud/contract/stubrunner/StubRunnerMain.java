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

package org.springframework.cloud.contract.stubrunner;

import java.io.IOException;
import java.util.Arrays;

import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;

/**
 * Class to run stub runner as a standalone process.
 *
 * @author Marcin Grzejszczak
 * @deprecated - use Stub Runner Boot server
 */
@Deprecated
public class StubRunnerMain {

	private static final Log log = LogFactory.getLog(StubRunnerMain.class);

	private final Arguments arguments;

	private StubRunnerMain(String[] args) throws Exception {
		OptionParser parser = new OptionParser();
		try {
			ArgumentAcceptingOptionSpec<Integer> minPortValueOpt = parser.acceptsAll(
					Arrays.asList("minp", "minPort"),
					"Minimum port value to be assigned to the WireMock instance. Defaults to 10000")
					.withRequiredArg().ofType(Integer.class).defaultsTo(10000);
			ArgumentAcceptingOptionSpec<Integer> maxPortValueOpt = parser.acceptsAll(
					Arrays.asList("maxp", "maxPort"),
					"Maximum port value to be assigned to the WireMock instance. Defaults to 15000")
					.withRequiredArg().ofType(Integer.class).defaultsTo(15000);
			ArgumentAcceptingOptionSpec<String> stubsOpt = parser.acceptsAll(
					Arrays.asList("s", "stubs"),
					"Comma separated list of Ivy representation "
							+ "of jars with stubs. Eg. groupid:artifactid1,groupid2:artifactid2:classifier")
					.withRequiredArg();
			ArgumentAcceptingOptionSpec<String> classifierOpt = parser.acceptsAll(
					Arrays.asList("c", "classifier"),
					"Suffix for the jar containing stubs (e.g. 'stubs' "
							+ "if the stub jar would have a 'stubs' classifier for stubs: foobar-stubs ). Defaults to 'stubs'")
					.withRequiredArg().defaultsTo("stubs");
			ArgumentAcceptingOptionSpec<String> rootOpt = parser.acceptsAll(
					Arrays.asList("r", "root"),
					"Location of a Jar containing server where you keep "
							+ "your stubs (e.g. http://nexus.net/content/repositories/repository)")
					.withRequiredArg();
			ArgumentAcceptingOptionSpec<String> usernameOpt = parser
					.acceptsAll(Arrays.asList("u", "username"),
							"Username to user when connecting to repository")
					.withOptionalArg();
			ArgumentAcceptingOptionSpec<String> passwordOpt = parser
					.acceptsAll(Arrays.asList("p", "password"),
							"Password to user when connecting to repository")
					.withOptionalArg();
			ArgumentAcceptingOptionSpec<String> proxyHostOpt = parser
					.acceptsAll(Arrays.asList("phost", "proxyHost"),
							"Proxy host to use for repository requests")
					.withOptionalArg();
			ArgumentAcceptingOptionSpec<Integer> proxyPortOpt = parser
					.acceptsAll(Arrays.asList("pport", "proxyPort"),
							"Proxy port to use for repository requests")
					.withOptionalArg().ofType(Integer.class);
			ArgumentAcceptingOptionSpec<String> stubsMode = parser
					.acceptsAll(Arrays.asList("sm", "stubsMode"),
							"Stubs mode to be used. Acceptable values " + Arrays
									.toString(StubRunnerProperties.StubsMode.values()))
					.withRequiredArg()
					.defaultsTo(StubRunnerProperties.StubsMode.CLASSPATH.toString());
			OptionSet options = parser.parse(args);
			String stubs = options.valueOf(stubsOpt);
			StubRunnerProperties.StubsMode stubsModeValue = StubRunnerProperties.StubsMode
					.valueOf(options.valueOf(stubsMode));
			Integer minPortValue = options.valueOf(minPortValueOpt);
			Integer maxPortValue = options.valueOf(maxPortValueOpt);
			String stubRepositoryRoot = options.valueOf(rootOpt);
			String stubsSuffix = options.valueOf(classifierOpt);
			final String username = options.valueOf(usernameOpt);
			final String password = options.valueOf(passwordOpt);
			final String proxyHost = options.valueOf(proxyHostOpt);
			final Integer proxyPort = options.valueOf(proxyPortOpt);
			final StubRunnerOptionsBuilder builder = new StubRunnerOptionsBuilder()
					.withMinMaxPort(minPortValue, maxPortValue)
					.withStubRepositoryRoot(stubRepositoryRoot)
					.withStubsMode(stubsModeValue).withStubsClassifier(stubsSuffix)
					.withUsername(username).withPassword(password).withStubs(stubs);
			if (proxyHost != null) {
				builder.withProxy(proxyHost, proxyPort);
			}
			StubRunnerOptions stubRunnerOptions = builder.build();
			this.arguments = new Arguments(stubRunnerOptions);
		}
		catch (Exception e) {
			printErrorMessage(e, parser);
			throw e;
		}
	}

	public static void main(String[] args) throws Exception {
		new StubRunnerMain(args).execute();
	}

	private void printErrorMessage(Exception e, OptionParser parser) throws IOException {
		System.err.println(e.getMessage());
		System.err.println("java -jar stub-runner.jar [options...] ");
		parser.printHelpOn(System.err);
		System.err.println();
		System.err.println(
				"Example: java -jar stub-runner.jar ${parser.printExample(ALL)}");
	}

	private void execute() {
		try {
			if (log.isDebugEnabled()) {
				log.debug("Launching StubRunner with args: " + this.arguments);
			}
			// TODO: Pass StubsToRun either from String or File
			BatchStubRunner stubRunner = new BatchStubRunnerFactory(
					this.arguments.getStubRunnerOptions()).buildBatchStubRunner();
			RunningStubs runningCollaborators = stubRunner.runStubs();
			log.info(runningCollaborators.toString());
		}
		catch (Exception e) {
			log.error("An exception occurred while trying to execute the stubs", e);
			throw e;
		}
	}

}
