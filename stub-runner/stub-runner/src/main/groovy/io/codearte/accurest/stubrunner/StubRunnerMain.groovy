package io.codearte.accurest.stubrunner

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.codearte.accurest.stubrunner.util.StubsParser
import org.kohsuke.args4j.CmdLineException
import org.kohsuke.args4j.CmdLineParser
import org.kohsuke.args4j.Option

import static org.kohsuke.args4j.OptionHandlerFilter.ALL

@Slf4j
@CompileStatic
class StubRunnerMain {

	@Option(name = "-sr", aliases = ['--stubRepositoryRoot'], usage = "Location of a Jar containing server where you keep your stubs (e.g. http://nexus.net/content/repositories/repository)", required = true)
	private String stubRepositoryRoot

	@Option(name = "-ss", aliases = ['--stubsSuffix'], usage = "Suffix for the jar containing stubs (e.g. 'stubs' if the stub jar would have a 'stubs' classifier for stubs: foobar-stubs ). Defaults to 'stubs'")
	private String stubsSuffix = 'stubs'

	@Option(name = "-minp", aliases = ['--minPort'], usage = "Minimal port value to be assigned to the WireMock instance. Defaults to 10000")
	private Integer minPortValue = 10000

	@Option(name = "-maxp", aliases = ['--maxPort'], usage = "Maximum port value to be assigned to the WireMock instance. Defaults to 15000")
	private Integer maxPortValue = 15000

	@Option(name = "-wo", aliases = ['--workOffline'], usage = "Switch to work offline. Defaults to 'false'")
	private Boolean workOffline = Boolean.FALSE

	@Option(name = "-s", aliases = ['--stubs'], usage = 'Comma separated list of Ivy representation of jars with stubs. Eg. groupid:artifactid1,groupid2:artifactid2:classifier')
	private String stubs

	private final Arguments arguments

	StubRunnerMain(String[] args) {
		CmdLineParser parser = new CmdLineParser(this)
		try {
			parser.parseArgument(args)
			this.arguments = new Arguments(new StubRunnerOptions(minPortValue, maxPortValue, stubRepositoryRoot,
					workOffline, stubsSuffix))
		} catch (CmdLineException e) {
			printErrorMessage(e, parser)
			throw e
		}
	}

	private void printErrorMessage(CmdLineException e, CmdLineParser parser) {
		System.err.println(e.getMessage())
		System.err.println("java -jar stub-runner.jar [options...] ")
		parser.printUsage(System.err)
		System.err.println()
		System.err.println("Example: java -jar stub-runner.jar ${parser.printExample(ALL)}")
	}

	static void main(String[] args) {
		new StubRunnerMain(args).execute()
	}

	private void execute() {
		try {
			log.debug("Launching StubRunner with args: $arguments")
			// TODO: Pass StubsToRun either from String or File
			Collection<StubConfiguration> collaborators = StubsParser.fromString(stubs, stubsSuffix)
			BatchStubRunner stubRunner = new BatchStubRunnerFactory(arguments.stubRunnerOptions, collaborators).buildBatchStubRunner()
			RunningStubs runningCollaborators = stubRunner.runStubs()
			log.info(runningCollaborators.toString())
		} catch (Exception e) {
			log.error("An exception occurred while trying to execute the stubs", e)
			throw e
		}
	}

}