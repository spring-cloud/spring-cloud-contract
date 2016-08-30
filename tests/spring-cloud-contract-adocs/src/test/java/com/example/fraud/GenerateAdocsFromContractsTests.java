package com.example.fraud;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.regex.Pattern;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.verifier.util.ContractVerifierDslConverter;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class GenerateAdocsFromContractsTests {

	//final Asciidoctor asciidoctor = Asciidoctor.Factory.create();

	// TODO: Can be parametrized
	@Value("classpath:contracts") Resource contracts;
	// TODO: Can be parametrized
	@Value("classpath:contracts.adoc") Resource contractsAdoc;

	@Test public void should_convert_contracts_into_adoc() throws IOException {
		String contractsAdocAsString = fileAsString(this.contractsAdoc.getFile().toPath());
		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(contractsAdocAsString);
		final Path rootDir = contracts.getFile().toPath();

		Files.walkFileTree(rootDir, new FileVisitor<Path>() {
			private Pattern pattern = Pattern.compile("^.*groovy$");

			@Override
			public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes atts)
					throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path path, BasicFileAttributes mainAtts)
					throws IOException {
				boolean matches = pattern.matcher(path.toString()).matches();
				if (matches) {
					appendContract(stringBuilder, path);
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path path, IOException exc)
					throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override public FileVisitResult visitFileFailed(Path path, IOException exc)
					throws IOException {
				// If the root directory has failed it makes no sense to continue
				return path.equals(rootDir) ?
						FileVisitResult.TERMINATE :
						FileVisitResult.CONTINUE;
			}
		});

		//String outputAdoc = asciidoctor.convert(stringBuilder.toString(), new HashMap<String, Object>());
		String outputAdoc = stringBuilder.toString();
		// TODO: Can be parametrized
		File outputDir = new File("target/generated-snippets");
		outputDir.mkdirs();
		// TODO: Can be parametrized
		File outputFile = new File(outputDir, "contracts.adoc");
		if (outputFile.exists()) {
			outputFile.delete();
		}
		if (outputFile.createNewFile()) {
			Files.write(outputFile.toPath(), outputAdoc.getBytes());
		}
	}

	static StringBuilder appendContract(StringBuilder stringBuilder, Path path)
			throws IOException {
		Contract contract = ContractVerifierDslConverter.convert(path.toFile());
		// TODO: Can be parametrized
		return stringBuilder.append("### ")
				.append(path.getFileName().toString())
				.append("\n\n")
				.append(contract.getDescription())
				.append("\n\n")
				.append("#### Contract structure")
				.append("\n\n")
				.append("[source,java,indent=0]")
				.append("\n")
				.append("----")
				.append("\n")
				.append(fileAsString(path))
				.append("\n")
				.append("----")
				.append("\n\n");
	}

	static String fileAsString(Path path) throws IOException {
		byte[] encoded = Files.readAllBytes(path);
		return new String(encoded, StandardCharsets.UTF_8);
	}
}