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

import java.util.List;

import org.springframework.cloud.contract.spec.Contract;

/**
 * @author Marcin Grzejszczak
 */
public class StubData {
	final Integer port;
	final List<Contract> contracts;
	
	public StubData(Integer port, List<Contract> contracts) {
		this.port = port;
		this.contracts = contracts;
	}
	public Integer getPort() {
		return port;
	}
	public List<Contract> getContracts() {
		return contracts;
	}
	@Override
	public String toString() {
		return "StubData [port=" + port + ", contracts=" + contracts + "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((contracts == null) ? 0 : contracts.hashCode());
		result = prime * result + ((port == null) ? 0 : port.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StubData other = (StubData) obj;
		if (contracts == null) {
			if (other.contracts != null)
				return false;
		}
		else if (!contracts.equals(other.contracts))
			return false;
		if (port == null) {
			if (other.port != null)
				return false;
		}
		else if (!port.equals(other.port))
			return false;
		return true;
	}
}
