package user11681.wheel;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class WheelPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        new ProjectHandler(project).handle();
    }
}
