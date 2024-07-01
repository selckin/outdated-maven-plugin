package com.giovds;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OutdatedMavenPluginMojoTest {

    @Mock
    private QueryClient client = mock(QueryClient.class);

    @InjectMocks
    private OutdatedMavenPluginMojo mojo = new OutdatedMavenPluginMojo(client);

    @Test
    void should_throw_exception_when_shouldFailBuild_and_outdatedDependencies() throws Exception {
        final MavenProject projectWithDependency = createProjectWithDependencyOfAge(LocalDate.now().minusYears(10));
        mojo.setProject(projectWithDependency);

        mojo.setShouldFailBuild(true);
        assertThatThrownBy(() -> mojo.execute())
                .isInstanceOf(MojoFailureException.class)
                .hasMessage("There are dependencies that are outdated.");
    }

    @Test
    void should_now_throw_exception_by_default_when_outdatedDependencies() throws Exception {
        final MavenProject projectWithDependency = createProjectWithDependencyOfAge(LocalDate.now().minusYears(10));
        mojo.setProject(projectWithDependency);

        assertThatCode(() -> mojo.execute()).doesNotThrowAnyException();
    }

    private MavenProject createProjectWithDependencyOfAge(final LocalDate timestamp) throws MojoExecutionException {
        final Dependency dependency = new Dependency();
        dependency.setGroupId("com.giovds");
        dependency.setArtifactId("test-example");
        dependency.setVersion("1.0.0");
        when(client.search(any())).thenReturn(Set.of(
                new QueryClient.FoundDependency("id", dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion(), timestamp)
        ));

        final MavenProject project = new MavenProject();
        project.setDependencies(List.of(dependency));
        return project;
    }
}