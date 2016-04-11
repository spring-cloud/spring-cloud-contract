package io.codearte.accurest.stubrunner

interface StubDownloader {

	File downloadAndUnpackStubJar(boolean workOffline, String stubRepositoryRoot, String stubsGroup, String
			stubsModule, String classifier)
}