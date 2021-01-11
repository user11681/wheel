/*
 * This file is part of fabric-loom, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2016, 2017, 2018 FabricMC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package user11681.wheel;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jfrog.bintray.gradle.BintrayExtension;
import com.jfrog.bintray.gradle.BintrayPlugin;
import groovy.util.Node;
import net.gudenau.lib.unsafe.Unsafe;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.UnknownTaskException;
import org.gradle.api.XmlProvider;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ExcludeRule;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.dsl.ArtifactHandler;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.FlatDirectoryArtifactRepository;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.file.FileCopyDetails;
import org.gradle.api.initialization.dsl.ScriptHandler;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.Convention;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.plugins.PluginManager;
import org.gradle.api.publish.Publication;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.jvm.tasks.Jar;
import org.gradle.language.jvm.tasks.ProcessResources;
import org.gradle.plugins.ide.eclipse.EclipsePlugin;
import org.gradle.plugins.ide.idea.IdeaPlugin;
import org.gradle.plugins.ide.idea.model.IdeaModel;
import org.gradle.plugins.ide.idea.model.IdeaModule;
import user11681.reflect.Accessor;
import user11681.reflect.Classes;

import net.fabricmc.loom.api.decompilers.LoomDecompiler;
import net.fabricmc.loom.decompilers.cfr.FabricCFRDecompiler;
import net.fabricmc.loom.decompilers.fernflower.FabricFernFlowerDecompiler;
import user11681.wheel.dependency.WheelDependencyFactory;
import user11681.wheel.WheelExtension;
import user11681.wheel.dependency.configuration.BloatedDependencySet;
import user11681.wheel.dependency.configuration.IntransitiveDependencySet;
import net.fabricmc.loom.LoomGradleExtension;
import user11681.wheel.repository.WheelRepositoryFactory;

@SuppressWarnings({"ResultOfMethodCallIgnored", "UnstableApiUsage", "ConstantConditions", "unchecked"})
public class ProjectHandler {
    public static String latestMinecraftVersion = null;
    public static Map<String, String> latestYarnBuilds = new HashMap<>();
    public static Project currentProject = null;

    private static HttpClient httpClient = null;

    public final Project project;
    public final Project rootProject;
    public final PluginContainer plugins;
    public final PluginManager pluginManager;
    public final Convention convention;
    public final TaskContainer tasks;
    public final ExtensionContainer extensions;
    public final RepositoryHandler repositories;
    public final DependencyHandler dependencies;
    public final ConfigurationContainer configurations;
    public final ArtifactHandler artifacts;
    public final Logger logger;
    public final Gradle gradle;
    public final ScriptHandler buildScript;
    public final LoomGradleExtension loom;
    public final WheelExtension extension;

    private final List<LoomDecompiler> decompilers = new ArrayList<>();

    public ProjectHandler(Project project) {
        this.project = project;
        this.rootProject = project.getRootProject();
        this.plugins = project.getPlugins();
        this.pluginManager = project.getPluginManager();
        this.convention = project.getConvention();
        this.tasks = project.getTasks();
        this.extensions = project.getExtensions();
        this.repositories = project.getRepositories();
        this.dependencies = project.getDependencies();
        this.configurations = project.getConfigurations();
        this.artifacts = project.getArtifacts();
        this.logger = project.getLogger();
        this.gradle = project.getGradle();
        this.buildScript = project.getBuildscript();
        this.extension = this.extensions.create("wheel", WheelExtension.class);
    }

    public static boolean isRootProject(Project project) {
        return project.getRootProject() == project;
    }

    private static String sendGET(String uri) {
        if (httpClient == null) {
            httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
        }

        HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(uri)).build();

        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
        } catch (Throwable throwable) {
            throw Unsafe.throwException(throwable);
        }
    }

    private void checkMinecraftVersion() {
        if (this.extension.minecraftVersion == null) {
            if (latestMinecraftVersion == null) {
                String body = sendGET("https://meta.fabricmc.net/v2/versions/game");
                Matcher matcher = Pattern.compile("(?<=\").*(?=\")").matcher(body.substring(body.indexOf(":") + 1));

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
        currentProject = this.project;

        this.plugins.apply(MavenPublishPlugin.class);

        Classes.staticCast(Accessor.getObject(this.repositories, "repositoryFactory"), WheelRepositoryFactory.classPointer);
        Classes.staticCast(Accessor.getObject(this.dependencies, "dependencyFactory"), WheelDependencyFactory.classPointer);

//        this.configurations.create("dev");

        this.project.beforeEvaluate(ignored -> this.beforeEvaluate());
        this.project.afterEvaluate(ignored -> this.afterEvaluate());

        this.plugins.apply(BintrayPlugin.class);

        this.repositories.mavenLocal();

        this.tasks.getByName("remapJar").doLast((Task remapTask) -> remapTask.getInputs().getFiles().forEach(File::delete));
        this.tasks.getByName("remapSourcesJar").doLast((Task remapTask) -> remapTask.getInputs().getFiles().forEach(File::delete));

        this.configureConfigurations();
    }

    private void configureConfigurations() {
        Configuration intransitiveInclude = this.configurations.create("intransitiveInclude");
        Configuration intransitive = this.configurations.create("intransitive").extendsFrom(intransitiveInclude);
        Configuration bloatedInclude = this.configurations.create("bloatedInclude");
        Configuration bloated = this.configurations.create("bloated").extendsFrom(bloatedInclude);
        Configuration modInclude = this.configurations.create("modInclude").extendsFrom(bloatedInclude, intransitiveInclude);
        Configuration mod = this.configurations.create("mod").extendsFrom(modInclude, bloated, intransitive);
        Configuration apiInclude = this.configurations.create("apiInclude");

        Classes.staticCast(Accessor.getObject(bloated, "dependencies"), BloatedDependencySet.classPointer);
        Classes.staticCast(Accessor.getObject(bloatedInclude, "dependencies"), BloatedDependencySet.classPointer);
        Classes.staticCast(Accessor.getObject(intransitive, "dependencies"), IntransitiveDependencySet.classPointer);
        Classes.staticCast(Accessor.getObject(intransitiveInclude, "dependencies"), IntransitiveDependencySet.classPointer);

        this.configurations.getByName("api").extendsFrom(apiInclude);
        this.configurations.getByName("modApi").extendsFrom(mod);
        this.configurations.getByName("include").extendsFrom(apiInclude, modInclude);
    }

    private void beforeEvaluate() {
        currentProject = this.project;
    }

    private void afterEvaluate() {
        this.checkMinecraftVersion();
        this.checkYarnBuild();

        this.dependencies.add("minecraft", "com.mojang:minecraft:" + this.extension.minecraftVersion);
        this.dependencies.add("mappings", String.format("net.fabricmc:yarn:%s+build.%s:v2", this.extension.minecraftVersion, this.extension.yarnBuild));
        this.dependencies.add("modApi", "net.fabricmc:fabric-loader:+");
        this.dependencies.add("testImplementation", "org.junit.jupiter:junit-jupiter:+");

        if (this.extension.noSpam) {
            this.dependencies.add("modApi", "narratoroff");
            this.dependencies.add("modApi", "noauth");
        }

        for (JavaCompile task : this.tasks.withType(JavaCompile.class)) {
            task.getOptions().setEncoding("UTF-8");

            task.setSourceCompatibility(this.extension.javaVersion.toString());
            task.setTargetCompatibility(task.getSourceCompatibility());
        }

        this.extensions.getByType(JavaPluginExtension.class).withSourcesJar();

        for (Jar task : this.tasks.withType(Jar.class)) {
//            task.getArchiveClassifier().set("dev");

            task.from("LICENSE");
        }

        ProcessResources processResources = (ProcessResources) this.tasks.getByName("processResources");
        processResources.getInputs().property("version", this.project.getVersion());
        processResources.filesMatching("fabric.mod.json", (FileCopyDetails details) -> details.expand(new HashMap<>(Map.of("version", project.getVersion()))));

//        File devJar = project.file(String.format("%s/libs/%s-%s-dev.jar", this.project.getBuildDir(), this.project.getName(), this.project.getVersion()));

//        this.artifacts.add("dev", devJar, (ConfigurablePublishArtifact artifact) -> artifact.builtBy(tasks.getByName("jar")).setType("jar"));

//        if (devJar.exists()) {
//            RemapJarTask task = (RemapJarTask) tasks.getByName("remapJar");
//
//            task.getInput().set(devJar);
//            task.getArchiveFileName().set(String.format("%s-%s.jar", project.getName(), project.getVersion()));
//        }

        if (this.extension.publish()) {
            PublishingExtension publishing = this.extensions.getByType(PublishingExtension.class);

            publishing.getPublications().create("maven", MavenPublication.class, (MavenPublication publication) -> {
                publication.setGroupId((String) this.project.getGroup());
                publication.setArtifactId(this.project.getName());
                publication.setVersion((String) this.project.getVersion());

                Task remapJar = this.tasks.getByName("remapJar");

                publication.artifact(remapJar).builtBy(remapJar);
                publication.artifact(this.tasks.getByName("sourcesJar")).builtBy(this.tasks.getByName("remapSourcesJar"));
            });

            publishing.getRepositories().mavenLocal();

            SourceSet testSet = this.convention.getPlugin(JavaPluginConvention.class).getSourceSets().getByName("test");

            this.tasks.create("runTestClient", RunClientTask.class).classpath(testSet.getRuntimeClasspath());
            this.tasks.create("runTestServer", RunServerTask.class).classpath(testSet.getRuntimeClasspath());

            if (this.extension.bintray()) {
                BintrayExtension bintray = this.extensions.getByType(BintrayExtension.class);
                bintray.setUser(System.getenv("BINTRAY_USER"));
                bintray.setKey(System.getenv("BINTRAY_API_KEY"));
                bintray.setPublications("maven");
                bintray.setPublish(true);

                BintrayExtension.PackageConfig pkg = bintray.getPkg();
                pkg.setRepo("maven");
                pkg.setName(project.getName());
                pkg.setLicenses("LGPL-3.0");

                BintrayExtension.VersionConfig version = pkg.getVersion();
                version.setName(String.valueOf(project.getVersion()));
                version.setReleased(new Date().toString());
            }
        }
    }
}