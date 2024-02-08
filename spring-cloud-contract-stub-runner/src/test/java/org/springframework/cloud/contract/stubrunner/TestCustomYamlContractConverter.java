/*
 * Copyright 2013-2020 the original author or authors.
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.spec.ContractConverter;

/**
 * @author Sven Bayer
 */
public class TestCustomYamlContractConverter implements ContractConverter {

	@Override
	public boolean isAccepted(File file) {
		if (!file.getName().endsWith(".yml") && !file.getName().endsWith(".yaml")) {
			return false;
		}
		Optional<String> line;
		try {
			line = Files.lines(file.toPath()).findFirst();
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
		return line.isPresent() && line.get().startsWith("custom_format: 1.0");
	}

	@Override
	public Collection<Contract> convertFrom(File file) {
		return Collections.singleton(new Contract());
	}

	@Override
	public Object convertTo(Collection contract) {
		return new Object();
	}

}
