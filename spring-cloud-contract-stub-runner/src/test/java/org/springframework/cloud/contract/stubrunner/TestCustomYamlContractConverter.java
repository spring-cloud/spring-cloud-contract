package org.springframework.cloud.contract.stubrunner;

import groovy.lang.Closure;
import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.spec.ContractConverter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * @author Sven Bayer
 */
public class TestCustomYamlContractConverter implements ContractConverter {

    @Override
    public boolean isAccepted(File file) {
        if (!file.getName().endsWith(".yml") && !file.getName().endsWith(".yaml")) {
            return false;
        }
        Optional<String> line;
        try {
            line = Files.lines(file.toPath()).findFirst();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return line.isPresent() && line.get().startsWith("custom_format: 1.0");
    }

    @Override
    public Collection<Contract> convertFrom(File file) {
        return Collections.singleton(Contract.make(Closure.IDENTITY));
    }

    @Override
    public Object convertTo(Collection contract) {
        return new Object();
    }
}