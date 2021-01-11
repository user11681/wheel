package user11681.wheel.dependency;

import java.util.Set;

import user11681.wheel.WheelExtension;

public class DependencyEntry {
    public final String artifact;

    private String repository;
    private Set<String> keys;

    public DependencyEntry(String artifact) {
        this.artifact = artifact;
    }

    public String resolveRepository() {
        return WheelExtension.repository(this.repository);
    }

    public String repository() {
        return this.repository;
    }

    public DependencyEntry repository(String repository) {
        this.repository = repository;

        return this;
    }

    public Set<String> key() {
        return keys;
    }

    public DependencyEntry key(String... keys) {
        this.keys = Set.of(keys);

        return this;
    }
}
