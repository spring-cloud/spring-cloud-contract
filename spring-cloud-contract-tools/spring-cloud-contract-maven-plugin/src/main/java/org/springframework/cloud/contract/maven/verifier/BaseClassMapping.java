package org.springframework.cloud.contract.maven.verifier;

/**
 * Represents a single mapping of regex on package where contracts reside
 * to the FQN of the base test class
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

	@Override public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		BaseClassMapping that = (BaseClassMapping) o;
		if (this.contractPackageRegex != null ?
				!this.contractPackageRegex.equals(that.contractPackageRegex) :
				that.contractPackageRegex != null)
			return false;
		return this.baseClassFQN != null ?
				this.baseClassFQN.equals(that.baseClassFQN) :
				that.baseClassFQN == null;

	}

	@Override public int hashCode() {
		int result = this.contractPackageRegex != null ? this.contractPackageRegex.hashCode() : 0;
		result = 31 * result + (this.baseClassFQN != null ? this.baseClassFQN.hashCode() : 0);
		return result;
	}
}
