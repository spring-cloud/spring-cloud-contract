package org.springframework.cloud.contract.stubrunner;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.Assert.assertFalse;

public class TemporaryFileStorageTest {
    @Test
    public void should_cleanup_temporary_file_storage() throws IOException {
        //given:
        File dummyContractFile = Files.createTempFile("dummy-contract", ".groovy").toFile();
        TemporaryFileStorage.add(dummyContractFile);

        //when:
        TemporaryFileStorage.cleanup(true);

        //then:
        assertFalse(dummyContractFile.exists());
    }
}
