package user11681.wheel.dependency;

import java.util.Map;

import net.gudenau.lib.unsafe.Unsafe;
import org.gradle.api.artifacts.ClientModule;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencyConstraint;
import org.gradle.api.capabilities.Capability;
import org.gradle.api.internal.artifacts.DefaultDependencyFactory;
import org.gradle.api.internal.attributes.ImmutableAttributesFactory;
import org.gradle.api.internal.notations.ProjectDependencyFactory;
import org.gradle.internal.typeconversion.NotationParser;
import user11681.wheel.ProjectHandler;
import user11681.wheel.WheelExtension;

public class WheelDependencyFactory extends DefaultDependencyFactory {
    public static final WheelDependencyFactory dummyInstance = Unsafe.allocateInstance(WheelDependencyFactory.class);

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
        return super.createDependency(this.resolve(dependencyNotation));
    }

    private Object resolve(String[] components) {
        if (components.length == 2) {
            Object first = this.resolve(components[0]);
            Object second = this.resolve(components[1]);

            if ((first instanceof String && second instanceof String) && ((String) second).indexOf(':') < 0) {
                return changeVersion((String) first, (String) second);
            }

            return new Object[]{first, second};
        }

        Object[] resolved = new Object[components.length];

        for (int i = 0; i < components.length; i++) {
            resolved[i] = this.resolve(components[i]);
        }

        return resolved;
    }

    private Object resolve(String dependency) {
        Object value = WheelExtension.artifact(dependency);

        if (value != null) {
            return value;
        }

        try {
            return ProjectHandler.projectsByFactory.get(this).getDependencies().project(Map.of("path", dependency));
        } catch (Throwable ignored) {}

        return dependency;
    }

    private Object resolve(Object dependencyNotation) {
        if (dependencyNotation instanceof String) {
            return this.resolve((String) dependencyNotation);
        }

        if (dependencyNotation instanceof String[]) {
            return this.resolve((String[]) dependencyNotation);
        }

        return dependencyNotation;
    }
}
