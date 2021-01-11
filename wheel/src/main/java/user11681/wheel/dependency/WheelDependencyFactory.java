package user11681.wheel.dependency;

import java.util.Map;

import org.gradle.api.artifacts.ClientModule;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencyConstraint;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.ArtifactRepository;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.capabilities.Capability;
import org.gradle.api.internal.artifacts.DefaultDependencyFactory;
import org.gradle.api.internal.attributes.ImmutableAttributesFactory;
import org.gradle.api.internal.notations.ProjectDependencyFactory;
import org.gradle.internal.typeconversion.NotationParser;
import user11681.reflect.Classes;

import user11681.wheel.WheelExtension;
import user11681.wheel.ProjectHandler;

public class WheelDependencyFactory extends DefaultDependencyFactory {
    public static final long classPointer = Classes.getClassPointer(WheelDependencyFactory.class);

    public WheelDependencyFactory(
        NotationParser<Object, Dependency> dependencyNotationParser,
        NotationParser<Object, DependencyConstraint> dependencyConstraintNotationParser,
        NotationParser<Object, ClientModule> clientModuleNotationParser,
        NotationParser<Object, Capability> capabilityNotationParser,
        ProjectDependencyFactory projectDependencyFactory,
        ImmutableAttributesFactory attributesFactory) {
        super(dependencyNotationParser, dependencyConstraintNotationParser, clientModuleNotationParser, capabilityNotationParser, projectDependencyFactory, attributesFactory);
    }

    private static String changeVersion(String artifact, String version) {
        String[] segments = artifact.split(":");

        segments[2] = version;

        return String.join(":", segments);
    }

    @Override
    public Dependency createDependency(Object dependencyNotation) {
        if (dependencyNotation instanceof String) {
            return super.createDependency(resolve((String) dependencyNotation));
        }

        return super.createDependency(dependencyNotation);
    }

    private static Object resolve(String dependency) {
        String[] components = dependency.split(":");

        if (components.length == 2) {
            DependencyEntry info = WheelExtension.dependency(components[0]);

            if (info != null) {
                return changeVersion(info.artifact, components[1]);
            }
        }

        DependencyEntry value = WheelExtension.dependency(dependency);

        if (value != null) {
            repository:
            if (ProjectHandler.currentProject != null && value.repository() != null) {
                RepositoryHandler repositories = ProjectHandler.currentProject.getRepositories();

                for (ArtifactRepository repository : repositories) {
                    if (value.artifact.equals(repository.getName())
                        || repository instanceof MavenArtifactRepository
                        && value.resolveRepository().equals(((MavenArtifactRepository) repository).getUrl().toString())) {
                        break repository;
                    }
                }

                repositories.maven((MavenArtifactRepository repository) -> repository.setUrl(value.resolveRepository()));
            }

            return value.artifact;
        }

        if (ProjectHandler.currentProject != null && ProjectHandler.currentProject.findProject(dependency) != null) {
            return ProjectHandler.currentProject.getDependencies().project(Map.of("path", dependency));
        }

        return dependency;
    }
}
