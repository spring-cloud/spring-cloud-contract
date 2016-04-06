package io.codearte.accurest.maven;

import io.codearte.accurest.config.AccurestConfigProperties;
import io.codearte.accurest.wiremock.DslToWireMockClientConverter;
import io.codearte.accurest.wiremock.RecursiveFilesConverter;

public class AccurestConverter {

	public void convert(AccurestConfigProperties config){
		RecursiveFilesConverter converter = new RecursiveFilesConverter(new DslToWireMockClientConverter(), config);
		converter.processFiles();
	}

}
