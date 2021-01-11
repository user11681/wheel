package user11681.wheel;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.gradle.api.JavaVersion;
import org.gradle.api.Project;

import user11681.wheel.dependency.DependencyContainer;
import user11681.wheel.dependency.DependencyEntry;

public class WheelExtension {
    private boolean publish = true;
    private boolean bintray = true;

    public boolean noSpam = true;
    public boolean shareCaches = true;

    public String minecraftVersion;
    public String yarnBuild;

    public JavaVersion javaVersion = JavaVersion.VERSION_1_8;

    private static final Map<String, String> repositoryMap = new HashMap<>(Map.ofEntries(
        Map.entry("blamejared", "https://maven.blamejared.com"),
        Map.entry("boundarybreaker", "https://server.bbkr.space/artifactory/libs-release"),
        Map.entry("buildcraft", "https://mod-buildcraft.com/maven"),
        Map.entry("cursemaven", "https://www.cursemaven.com"),
        Map.entry("dblsaiko", "https://maven.dblsaiko.net/"),
        Map.entry("earthcomputer", "https://dl.bintray.com/earthcomputer/mods"),
        Map.entry("grossfabrichackers", "https://raw.githubusercontent.com/GrossFabricHackers/maven/master"),
        Map.entry("halfof2", "https://raw.githubusercontent.com/Devan-Kerman/Devan-Repo/master"),
        Map.entry("jamieswhiteshirt", "https://maven.jamieswhiteshirt.com/libs-release"),
        Map.entry("jitpack", "https://jitpack.io"),
        Map.entry("ladysnake", "https://dl.bintray.com/ladysnake/libs"),
        Map.entry("user11681", "https://dl.bintray.com/user11681/maven"),
        Map.entry("wrenchable", "https://dl.bintray.com/zundrel/wrenchable")
    ));

    public static final DependencyContainer dependencies = new DependencyContainer(
        new DependencyEntry("net.fabricmc.fabric-api:fabric-api:latest.release").key("api"),
        new DependencyEntry("net.fabricmc.fabric-api:fabric-api-base:latest.release").key("apibase"),
        new DependencyEntry("net.fabricmc.fabric-api:fabric-blockrenderlayer-v1:latest.release").key("apiblockrenderlayer"),
        new DependencyEntry("net.fabricmc.fabric-api:fabric-command-api-v1:latest.release").key("apicommand"),
        new DependencyEntry("net.fabricmc.fabric-api:fabric-screen-handler-api-v1:latest.release").key("apiscreenhandler"),
        new DependencyEntry("net.fabricmc.fabric-api:fabric-events-interaction-v0:latest.release").key("apieventsinteraction"),
        new DependencyEntry("net.fabricmc.fabric-api:fabric-key-binding-api-v1:latest.release").key("apikeybindings"),
        new DependencyEntry("net.fabricmc.fabric-api:fabric-lifecycle-events-v1:latest.release").key("apilifecycleevents"),
        new DependencyEntry("net.fabricmc.fabric-api:fabric-networking-api-v1:latest.release").key("apinetworking"),
        new DependencyEntry("net.fabricmc.fabric-api:fabric-renderer-api-v1:latest.release").key("apirendererapi"),
        new DependencyEntry("net.fabricmc.fabric-api:fabric-renderer-indigo:latest.release").key("apirendererindigo"),
        new DependencyEntry("net.fabricmc.fabric-api:fabric-resource-loader-v0:latest.release").key("apiresourceloader"),
        new DependencyEntry("net.fabricmc.fabric-api:fabric-tag-extensions-v0:latest.release").key("apitagextensions"),
        new DependencyEntry("curse.maven:aquarius-301299:3132504").key("aquarius").repository("cursemaven"),
        new DependencyEntry("net.devtech:arrp:latest.release").key("arrp").repository("halfof2"),
        new DependencyEntry("com.github.Chainmail-Studios:Astromine:1.8.1").key("astromine").repository("jitpack"),
        new DependencyEntry("me.sargunvohra.mcmods:autoconfig1u:latest.release").key("autoconfig"),
        new DependencyEntry("me.shedaniel.cloth:basic-math:latest.release").key("basicmath"),
        new DependencyEntry("user11681:bason:latest.release").key("bason").repository("user11681"),
        new DependencyEntry("io.github.onyxstudios.Cardinal-Components-API:Cardinal-Components-API:latest.release").key("cardinalcomponents").repository("ladysnake"),
        new DependencyEntry("io.github.onyxstudios.Cardinal-Components-API:cardinal-components-base:latest.release").key("cardinalcomponentsbase").repository("ladysnake"),
        new DependencyEntry("io.github.onyxstudios.Cardinal-Components-API:cardinal-components-entity:latest.release").key("cardinalcomponentsentity").repository("ladysnake"),
        new DependencyEntry("io.github.onyxstudios.Cardinal-Components-API:cardinal-components-item:latest.release").key("cardinalcomponentsitem").repository("ladysnake"),
        new DependencyEntry("user11681:cell:latest.release").key("cell").repository("user11681"),
        new DependencyEntry("curse.maven:charm-318872:3140951").key("charm").repository("cursemaven"),
        new DependencyEntry("me.shedaniel.cloth:config-2:latest.release").key("clothconfig"),
        new DependencyEntry("io.github.cottonmc:cotton-resources:latest.release").key("cottonresources").repository("boundarybreaker"),
        new DependencyEntry("user11681:commonformatting:latest.release").key("commonformatting").repository("user11681"),
        new DependencyEntry("user11681:dynamicentry:latest.release").key("dynamicentry").repository("user11681"),
        new DependencyEntry("com.github.Chocohead:Fabric-ASM:master-SNAPSHOT").key("fabricasm").repository("jitpack"),
        new DependencyEntry("curse.maven:moenchantments-320806:3084973").key("moenchantments").repository("cursemaven"),
        new DependencyEntry("user11681:fabricasmtools:latest.release").key("huntinghamhills").repository("user11681"),
        new DependencyEntry("net.devtech:grossfabrichacks:latest.release").key("grossfabrichacks").repository("grossfabrichackers"),
        new DependencyEntry("user11681:invisiblelivingentities:latest.release").key("invisiblelivingentities").repository("user11681"),
        new DependencyEntry("com.github.javaparser:javaparser-symbol-solver-core:latest.release").key("javaparser"),
        new DependencyEntry("org.jooq:joor-java-8:latest.release").key("joor"),
        new DependencyEntry("org.junit.jupiter:junit-jupiter:latest.release").key("junit"),
        new DependencyEntry("com.github.Yoghurt4C:LilTaterReloaded:fabric-1.16-SNAPSHOT").key("liltaterreloaded").repository("jitpack"),
        new DependencyEntry("user11681:limitless:latest.release").key("limitless").repository("user11681"),
        new DependencyEntry("io.github.prospector:modmenu:latest.release").key("modmenu"),
        new DependencyEntry("net.earthcomputer:multiconnect:latest.release:api").key("multiconnect").repository("earthcomputer"),
        new DependencyEntry("user11681:narratoroff:latest.release").key("narratoroff").repository("user11681"),
        new DependencyEntry("user11681:noauth:latest.release").key("noauth").repository("user11681"),
        new DependencyEntry("user11681:optional:latest.release").key("optional").repository("user11681"),
        new DependencyEntry("user11681:phormat:latest.release").key("phormat").repository("user11681"),
        new DependencyEntry("user11681:projectfabrok:latest.release").key("projectfabrok").repository("user11681"),
        new DependencyEntry("user11681:prone:latest.release").key("prone").repository("user11681"),
        new DependencyEntry("com.jamieswhiteshirt:reach-entity-attributes:latest.release").key("reachentityattributes").repository("jamieswhiteshirt"),
        new DependencyEntry("user11681:reflect:latest.release").key("reflect").repository("user11681"),
        new DependencyEntry("me.shedaniel:RoughlyEnoughItems:latest.release").key("rei"),
        new DependencyEntry("user11681:shortcode:latest.release").key("shortcode").repository("user11681"),
        new DependencyEntry("com.moandjiezana.toml:toml4j:latest.release").key("toml4j"),
        new DependencyEntry("net.gudenau.lib:unsafe:latest.release").key("unsafe").repository("user11681")
    );

    private static String sanitize(String key) {
        return key.replace("-", "").toLowerCase(Locale.ROOT);
    }

    public static String repository(String key) {
        return key == null ? null : repositoryMap.get(sanitize(key));
    }

    public static DependencyEntry dependency(String key) {
        return dependencies.entry(sanitize(key));
    }

    public static void repository(String key, String value) {
        repositoryMap.put(key, value);
    }

    public boolean publish() {
        return this.publish;
    }

    public void publish(boolean publish) {
        this.publish = publish;
    }

    public boolean bintray() {
        return this.bintray;
    }

    public void bintray(boolean bintray) {
        this.bintray = bintray;
    }

    public void setJavaVersion(Object version) {
        this.javaVersion = JavaVersion.toVersion(version);
    }
}
