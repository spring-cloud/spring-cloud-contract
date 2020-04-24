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

package org.springframework.cloud.contract.maven.verifier;

/**
 * Represents a single mapping of regex on package where contracts reside to the FQN of
 * the base test class.
 *
 * @author Marcin Grzejszczak
 * @since 1.0.0
 */
public class BaseClassMapping {

	private String contractPackageRegex;

	private String baseClassFQN;

	public String getContractPackageRegex() {
		return this.contractPackageRegex;
	}

	public void setContractPackageRegex(String contractPackageRegex) {
		this.contractPackageRegex = contractPackageRegex;
	}

	public String getBaseClassFQN() {
		return this.baseClassFQN;
	}

	public void setBaseClassFQN(String baseClassFQN) {
		this.baseClassFQN = baseClassFQN;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		BaseClassMapping that = (BaseClassMapping) o;
		if (this.contractPackageRegex != null
				? !this.contractPackageRegex.equals(that.contractPackageRegex)
				: that.contractPackageRegex != null) {
			return false;
		}
		return this.baseClassFQN != null ? this.baseClassFQN.equals(that.baseClassFQN)
				: that.baseClassFQN == null;

	}

	@Override
	public int hashCode() {
		int result = this.contractPackageRegex != null
				? this.contractPackageRegex.hashCode() : 0;
		result = 31 * result
				+ (this.baseClassFQN != null ? this.baseClassFQN.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "BaseClassMapping{" + "contractPackageRegex='" + this.contractPackageRegex
				+ '\'' + ", baseClassFQN='" + this.baseClassFQN + '\'' + '}';
	}

}
