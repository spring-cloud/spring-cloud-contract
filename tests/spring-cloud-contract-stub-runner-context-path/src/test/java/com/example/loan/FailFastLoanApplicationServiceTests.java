package com.example.loan;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * @author Andrew Morgan
 */
public class FailFastLoanApplicationServiceTests {

    @Test
    public void shouldFailToStartContextWhenNoStubCanBeFound() {
        // When
        final Throwable throwable = catchThrowable(() -> new SpringApplicationBuilder(Application.class, StubRunnerConfiguration.class)
                .properties(ImmutableMap.of(
                        "stubrunner.repositoryRoot", "classpath:m2repo/repository/",
                        "stubrunner.ids", new String[]{"org.springframework.cloud.contract.verifier.stubs:should-not-be-found"}))
                .run());

        // Then
        assertThat(throwable).isInstanceOf(BeanCreationException.class);
        assertThat(throwable.getCause()).isInstanceOf(BeanInstantiationException.class);
        assertThat(throwable.getCause().getCause())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("For groupId [org.springframework.cloud.contract.verifier.stubs] artifactId [should-not-be-found] and classifier [stubs] the version was not resolved!");
    }

}
