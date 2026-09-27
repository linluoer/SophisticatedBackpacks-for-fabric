package net.neoforged.fml;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

/**
 * Information about a mod - simplified shim for Fabric port.
 * Provides mod id and version information.
 */
public class ModInfo {
	private final String modId;
	private final ArtifactVersion version;

	public ModInfo(String modId, String versionString) {
		this.modId = modId;
		this.version = new DefaultArtifactVersion(versionString);
	}

	public ModInfo(String modId, ArtifactVersion version) {
		this.modId = modId;
		this.version = version;
	}

	public String getModId() {
		return modId;
	}

	public ArtifactVersion getVersion() {
		return version;
	}
}
