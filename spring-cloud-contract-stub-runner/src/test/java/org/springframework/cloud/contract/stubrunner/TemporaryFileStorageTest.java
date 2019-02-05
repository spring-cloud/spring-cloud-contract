package org.springframework.cloud.contract.stubrunner;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertFalse;

public class TemporaryFileStorageTest {

    @Test
    public void should_delete_file_inside_directory_from_temporary_file_storage() throws IOException {
        //given:
        Path tmpContractDirectory = Files.createTempDirectory("tmp");
        File tmpContractFile = new File(tmpContractDirectory.toString());
        File dummyContractFileInsideDirectory = Files.createTempFile(tmpContractDirectory, "dummy-contract-inside-folder", ".groovy").toFile();
        File dummyContractFile = Files.createTempFile("dummy-contract", ".groovy").toFile();
        TemporaryFileStorage.add(dummyContractFile);
        TemporaryFileStorage.add(tmpContractFile);

        //when:
        TemporaryFileStorage.cleanup(true);

        //then:
        assertFalse(dummyContractFile.exists());
        assertFalse(dummyContractFileInsideDirectory.exists());
        assertFalse(tmpContractDirectory.toFile().exists());
    }

}
