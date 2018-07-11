package org.springframework.cloud.contract.stubrunner;

import org.junit.Test;
import org.springframework.cloud.contract.spec.Contract;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

/**
 * @author Sven Bayer
 */
public class StubRepositoryTest {
    private static final File YAML_REPOSITORY_LOCATION = new File("src/test/resources/customYamlRepository");

    @Test
    public void should_prefer_custom_yaml_converter_over_standard() {
        //given:
        StubRepository repository = new StubRepository(YAML_REPOSITORY_LOCATION, new ArrayList<>(), new StubRunnerOptionsBuilder().build());
        int expectedDescriptorsSize = 1;

        //when:
        Collection<Contract> descriptors = repository.getContracts();

        //then:
        assertEquals(expectedDescriptorsSize, descriptors.size());
    }
}
