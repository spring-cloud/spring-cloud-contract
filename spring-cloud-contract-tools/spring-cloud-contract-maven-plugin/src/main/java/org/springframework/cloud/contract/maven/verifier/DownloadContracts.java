package org.springframework.cloud.contract.maven.verifier;

/**
 * @author Marcin Grzejszczak
 *
 * @since 1.0.0
 */
public class DownloadContracts {

	/**
	 * Repo where the packaged contracts are placed
	 */
	private String repositoryUrl;

	/**
	 * Group id of the packaged contracts
	 */
	private String groupId;

	/**
	 * Artifact id of the packaged contracts
	 */
	private String artifactId;

	/**
	 * Classifier of the packaged contracts
	 */
	private String classifier = "stubs";

	/**
	 * Version id of the packaged contracts
	 */
	private String version = "+";

	/**
	 * Ivy notation of the packaged contracts
	 */
	private String ivy;

	/**
	 * Path to contracts in the packaged contracts. Will default to groupid/artifactid
	 */
	private String pathToContracts;

	public String getRepositoryUrl() {
		return this.repositoryUrl;
	}

	public void setRepositoryUrl(String repositoryUrl) {
		this.repositoryUrl = repositoryUrl;
	}

	public String getGroupId() {
		return this.groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getArtifactId() {
		return this.artifactId;
	}

	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	public String getClassifier() {
		return this.classifier;
	}

	public void setClassifier(String classifier) {
		this.classifier = classifier;
	}

	public String getVersion() {
		return this.version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getIvy() {
		return this.ivy;
	}

	public void setIvy(String ivy) {
		this.ivy = ivy;
	}
}
