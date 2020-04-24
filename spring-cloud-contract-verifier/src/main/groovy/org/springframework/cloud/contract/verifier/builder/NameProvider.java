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

package org.springframework.cloud.contract.verifier.builder;

import java.io.File;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.verifier.file.ContractMetadata;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;
import org.springframework.cloud.contract.verifier.util.NamesUtil;
import org.springframework.util.StringUtils;

class NameProvider {

	private static final Log log = LogFactory.getLog(NameProvider.class);

	String methodName(SingleContractMetadata singleContractMetadata) {
		return "validate_" + generateMethodName(singleContractMetadata);
	}

	private String generateMethodName(SingleContractMetadata singleContractMetadata) {
		ContractMetadata contractMetadata = singleContractMetadata.getContractMetadata();
		File stubsFile = contractMetadata.getPath().toFile();
		Contract stubContent = singleContractMetadata.getContract();
		if (StringUtils.hasText(stubContent.getName())) {
			String name = NamesUtil.camelCase(
					NamesUtil.convertIllegalPackageChars(stubContent.getName()));
			if (log.isDebugEnabled()) {
				log.debug("Overriding the default test name with [" + name + "]");
			}
			return name;
		}
		else if (contractMetadata.getConvertedContract().size() > 1) {
			int index = findIndexOf(contractMetadata.getConvertedContract(), stubContent);
			String name = camelCasedMethodFromFileName(stubsFile) + "_" + index;
			if (log.isDebugEnabled()) {
				log.debug("Scenario found. The method name will be [" + name + "]");
			}
			return name;
		}
		String name = camelCasedMethodFromFileName(stubsFile);
		if (StringUtils.hasText(name) && log.isDebugEnabled()) {
			log.debug("The method name will be [" + name + "]");
		}
		return name;
	}

	private int findIndexOf(Collection<Contract> contracts, Contract stubContent) {
		int i = 0;
		for (Contract contract : contracts) {
			if (contract.equals(stubContent)) {
				return i;
			}
			i++;
		}
		return -1;
	}

	private String camelCasedMethodFromFileName(File stubsFile) {
		return NamesUtil.camelCase(NamesUtil.convertIllegalMethodNameChars(NamesUtil
				.toLastDot(NamesUtil.afterLast(stubsFile.getPath(), File.separator))));
	}

}
