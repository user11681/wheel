package user11681.wheel;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jfrog.bintray.gradle.BintrayExtension;
import com.jfrog.bintray.gradle.BintrayPlugin;
import net.gudenau.lib.unsafe.Unsafe;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.ConfigurablePublishArtifact;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.ArtifactHandler;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.file.FileCopyDetails;
import org.gradle.api.internal.artifacts.dsl.dependencies.DependencyFactory;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.publish.PublicationContainer;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.jvm.tasks.Jar;
import org.gradle.language.jvm.tasks.ProcessResources;
import user11681.reflect.Accessor;
import user11681.reflect.Classes;
import user11681.wheel.dependency.WheelDependencyFactory;
import user11681.wheel.dependency.configuration.BloatedDependencySet;
import user11681.wheel.dependency.configuration.IntransitiveDependencySet;
import user11681.wheel.repository.WheelRepositoryFactory;

import net.fabricmc.loom.LoomGradleExtension;
import net.fabricmc.loom.LoomGradlePlugin;
import net.fabricmc.loom.task.RemapJarTask;
import net.fabricmc.loom.task.RunClientTask;
import net.fabricmc.loom.task.RunServerTask;

public class ProjectHandler {
    public static String latestMinecraftVersion;
    public static Map<String, String> latestYarnBuilds = new HashMap<>();
    public static Map<DependencyFactory, Project> projectsByFactory = new HashMap<>();

    private static HttpClient httpClient;

    protected final Project project;
    protected final TaskContainer tasks;
    protected final WheelExtension extension;
    protected final RepositoryHandler repositories;
    protected final PluginContainer plugins;
    protected final ConfigurationContainer configurations;
    protected final DependencyHandler dependencies;

    public ProjectHandler(Project project) {
        this.project = project;

        this.tasks = project.getTasks();
        this.extension = project.getExtensions().create("wheel", WheelExtension.class);
        this.repositories = project.getRepositories();
        this.plugins = project.getPlugins();
        this.configurations = project.getConfigurations();
        this.dependencies = project.getDependencies();
    }

    private static String sendGET(String uri) {
        if (httpClient == null) {
            httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
        }

        HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(uri)).build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return response.body();
        } catch (InterruptedException | IOException exception) {
            throw Unsafe.throwException(exception);
        }
    }

    private void checkMinecraftVersion() {
        if (this.extension.minecraftVersion == null) {
            if (latestMinecraftVersion == null) {
                String body = sendGET("https://meta.fabricmc.net/v2/versions/game");
                Matcher matcher = Pattern.compile("(?<=\").*(?=\")").matcher(body.substring(body.indexOf(":")));
                matcher.find();

                latestMinecraftVersion = matcher.group();
            }

            this.extension.minecraftVersion = latestMinecraftVersion;
        }
    }

    private void checkYarnBuild() {
        if (this.extension.yarnBuild == null) {
            if (latestYarnBuilds.get(extension.minecraftVersion) == null) {
                String body = sendGET("https://meta.fabricmc.net/v2/versions/yarn/" + extension.minecraftVersion);
                Matcher matcher = Pattern.compile("\\d+").matcher(body.substring(body.indexOf("separator")));
                matcher.find();

                latestYarnBuilds.put(extension.minecraftVersion, matcher.group());
            }

            this.extension.yarnBuild = latestYarnBuilds.get(extension.minecraftVersion);
        }
    }

    public void handle() {
        projectsByFactory.put(Classes.staticCast(Accessor.getObject(dependencies, "dependencyFactory"), WheelDependencyFactory.dummyInstance), project);
        Classes.staticCast(Accessor.getObject(repositories, "repositoryFactory"), WheelRepositoryFactory.dummyInstance);

        plugins.apply(JavaLibraryPlugin.class);
        plugins.apply(MavenPublishPlugin.class);

        configurations.create("dev");

        project.afterEvaluate(ignored -> {
            plugins.apply(BintrayPlugin.class);

            checkMinecraftVersion();
            checkYarnBuild();

            dependencies.add("minecraft", "com.mojang:minecraft:" + extension.minecraftVersion);
            dependencies.add("mappings", String.format("net.fabricmc:yarn:%s+build.%s:v2", extension.minecraftVersion, extension.yarnBuild));
            dependencies.add("modApi", "net.fabricmc:fabric-loader:+");

            dependencies.add("testImplementation", WheelExtension.artifact("junit"));

            if (extension.noSpam) {
                repositories.maven((MavenArtifactRepository repository) -> repository.setUrl(WheelExtension.repository("user11681")));

                dependencies.add("modApi", "user11681:narratoroff:+");
                dependencies.add("modApi", "user11681:noauth:+");
            }

            for (JavaCompile task : tasks.withType(JavaCompile.class)) {
                CompileOptions options = task.getOptions();

                options.getRelease().set(extension.javaVersion.ordinal());
                options.setEncoding("UTF-8");

                task.setSourceCompatibility(extension.javaVersion.toString());
                task.setTargetCompatibility(extension.javaVersion.toString());
            }

            JavaPluginConvention convention = project.getConvention().getPlugin(JavaPluginConvention.class);
            SourceSetContainer sourceSets = convention.getSourceSets();
            SourceSet mainSet = sourceSets.getByName("main");
            SourceSet testSet = sourceSets.getByName("test");
            JavaPluginExtension javaExtension = project.getExtensions().getByType(JavaPluginExtension.class);

            javaExtension.withSourcesJar();

            for (Jar task : tasks.withType(Jar.class)) {
                task.getArchiveClassifier().set("dev");
                task.from("LICENSE");
            }

            ProcessResources processResources = (ProcessResources) tasks.getByName("processResources");

            processResources.getInputs().property("version", project.getVersion());
            processResources.filesMatching("fabric.mod.json", (FileCopyDetails details) -> processResources.expand(Map.of("version", project.getVersion())));

            ArtifactHandler artifacts = project.getArtifacts();
            File devJar = project.file(String.format("%s/libs/%s-%s-dev.jar", project.getBuildDir(), project.getName(), project.getVersion()));

            artifacts.add("dev", devJar, (ConfigurablePublishArtifact artifact) -> artifact.builtBy(tasks.getByName("jar")).setType("jar"));

            if (devJar.exists()) {
                RemapJarTask task = (RemapJarTask) tasks.getByName("remapJar");

                task.getInput().set(devJar);
                task.getArchiveFileName().set(String.format("%s-%s.jar", project.getName(), project.getVersion()));
            }

            PublishingExtension publishing = project.getExtensions().getByType(PublishingExtension.class);
            PublicationContainer publications = publishing.getPublications();

            publications.create("maven", MavenPublication.class, (MavenPublication publication) -> {
                publication.setGroupId((String) project.getGroup());
                publication.setArtifactId(project.getName());
                publication.setVersion((String) project.getVersion());

                Task remapJar = tasks.getByName("remapJar");

                publication.artifact(remapJar).builtBy(remapJar);
                publication.artifact(tasks.getByName("sourcesJar")).builtBy(tasks.getByName("remapSourcesJar"));
            });

            publishing.getRepositories().mavenLocal();

            tasks.create("runTestClient", RunClientTask.class).classpath(testSet.getRuntimeClasspath());
            tasks.create("runTestServer", RunServerTask.class).classpath(testSet.getRuntimeClasspath());

            BintrayExtension bintray = project.getExtensions().getByType(BintrayExtension.class);

            bintray.setUser(System.getenv("BINTRAY_USER"));
            bintray.setKey(System.getenv("BINTRAY_API_KEY"));
            bintray.setPublications("maven");
            bintray.setPublish(extension.bintray);

            BintrayExtension.PackageConfig pkg = bintray.getPkg();

            pkg.setRepo("maven");
            pkg.setName(project.getName());
            pkg.setLicenses("LGPL-3.0");

            BintrayExtension.VersionConfig version = pkg.getVersion();

            version.setName((String) project.getVersion());
            version.setReleased(new Date().toString());
        });

        plugins.apply(LoomGradlePlugin.class);

        project.afterEvaluate(ignored -> {
            LoomGradleExtension loom = project.getExtensions().getByType(LoomGradleExtension.class);

            loom.autoGenIDERuns = true;
            loom.shareCaches = true;
        });

        Configuration intransitiveInclude = configurations.create("intransitiveInclude");
        Configuration intransitive = configurations.create("intransitive").extendsFrom(intransitiveInclude);
        Configuration bloatedInclude = configurations.create("bloatedInclude");
        Configuration bloated = configurations.create("bloated").extendsFrom(bloatedInclude);
        Configuration modInclude = configurations.create("modInclude").extendsFrom(bloatedInclude, intransitiveInclude);
        Configuration mod = configurations.create("mod").extendsFrom(modInclude, bloated, intransitive);
        Configuration apiInclude = configurations.create("apiInclude");

        Classes.staticCast(Accessor.getObject(bloated, "dependencies"), BloatedDependencySet.dummyInstance);
        Classes.staticCast(Accessor.getObject(bloatedInclude, "dependencies"), BloatedDependencySet.dummyInstance);
        Classes.staticCast(Accessor.getObject(intransitive, "dependencies"), IntransitiveDependencySet.dummyInstance);
        Classes.staticCast(Accessor.getObject(intransitiveInclude, "dependencies"), IntransitiveDependencySet.dummyInstance);

        configurations.getByName("api").extendsFrom(apiInclude);
        configurations.getByName("modApi").extendsFrom(mod);
        configurations.getByName("include").extendsFrom(apiInclude, modInclude);
    }
}
