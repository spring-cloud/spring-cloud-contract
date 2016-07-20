/*
 *  Copyright 2013-2016 the original author or authors.
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

package org.springframework.cloud.contract.stubrunner;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StubRunnerMain {
	
	private static final Logger log = LoggerFactory.getLogger(StubRunnerMain.class);

	@Option(name = "-sr", aliases = "--stubRepositoryRoot", usage = "Location of a Jar containing server where you keep your stubs (e.g. http://nexus.net/content/repositories/repository)", required = true)
	private String stubRepositoryRoot;

	@Option(name = "-ss", aliases = "--stubsSuffix", usage = "Suffix for the jar containing stubs (e.g. 'stubs' if the stub jar would have a 'stubs' classifier for stubs: foobar-stubs ). Defaults to 'stubs'")
	private String stubsSuffix = "stubs";

	@Option(name = "-minp", aliases = "--minPort", usage = "Minimal port value to be assigned to the WireMock instance. Defaults to 10000")
	private Integer minPortValue = 10000;

	@Option(name = "-maxp", aliases = "--maxPort", usage = "Maximum port value to be assigned to the WireMock instance. Defaults to 15000")
	private Integer maxPortValue = 15000;

	@Option(name = "-wo", aliases = "--workOffline", usage = "Switch to work offline. Defaults to 'false'")
	private Boolean workOffline = Boolean.FALSE;

	@Option(name = "-s", aliases = "--stubs", usage = "Comma separated list of Ivy representation of jars with stubs. Eg. groupid:artifactid1,groupid2:artifactid2:classifier")
	private String stubs;

	private final Arguments arguments;

	private StubRunnerMain(String[] args) throws CmdLineException {
		CmdLineParser parser = new CmdLineParser(this);
		try {
			parser.parseArgument(args);
			StubRunnerOptions stubRunnerOptions = new StubRunnerOptionsBuilder()
					.withMinMaxPort(minPortValue, maxPortValue)
					.withStubRepositoryRoot(stubRepositoryRoot)
					.withWorkOffline(workOffline)
					.withStubsClassifier(stubsSuffix)
					.withStubs(stubs)
					.build();
			this.arguments = new Arguments(stubRunnerOptions);
		} catch (CmdLineException e) {
			printErrorMessage(e, parser);
			throw e;
		}
	}

	private void printErrorMessage(CmdLineException e, CmdLineParser parser) {
		System.err.println(e.getMessage());
		System.err.println("java -jar stub-runner.jar [options...] ");
		parser.printUsage(System.err);
		System.err.println();
		System.err.println("Example: java -jar stub-runner.jar ${parser.printExample(ALL)}");
	}

	static void main(String[] args) throws CmdLineException {
		new StubRunnerMain(args).execute();
	}

	private void execute() {
		try {
			log.debug("Launching StubRunner with args: " + arguments);
			// TODO: Pass StubsToRun either from String or File
			BatchStubRunner stubRunner = new BatchStubRunnerFactory(arguments.getStubRunnerOptions()).buildBatchStubRunner();
			RunningStubs runningCollaborators = stubRunner.runStubs();
			log.info(runningCollaborators.toString());
		} catch (Exception e) {
			log.error("An exception occurred while trying to execute the stubs", e);
			throw e;
		}
	}

}