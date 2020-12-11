package user11681.spinningwheel;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.gradle.api.JavaVersion;

public class SpinningWheelExtension {
    public String minecraftVersion;
    public String yarnBuild;

    public JavaVersion javaVersion = JavaVersion.current();

    public boolean noSpam = true;

    private static final Map<String, String> repositoryMap = new HashMap<>(Map.ofEntries(
        Map.entry("boundarybreaker", "https://server.bbkr.space/artifactory/libs-release"),
        Map.entry("blamejared", "https://maven.blamejared.com"),
        Map.entry("buildcraft", "https://mod-buildcraft.com/maven"),
        Map.entry("dblsaiko", "https://maven.dblsaiko.net/"),
        Map.entry("earthcomputer", "https://dl.bintray.com/earthcomputer/mods"),
        Map.entry("halfof2", "https://raw.githubusercontent.com/Devan-Kerman/Devan-Repo/master"),
        Map.entry("jamieswhiteshirt", "https://maven.jamieswhiteshirt.com/libs-release"),
        Map.entry("jitpack", "https://jitpack.io"),
        Map.entry("ladysnake", "https://dl.bintray.com/ladysnake/libs"),
        Map.entry("user11681", "https://dl.bintray.com/user11681/maven"),
        Map.entry("wrenchable", "https://dl.bintray.com/zundrel/wrenchable")
    ));

    private static final Map<String, String> artifactMap = new HashMap<>(Map.ofEntries(
        Map.entry("api", "net.fabricmc.fabric-api:fabric-api:+"),
        Map.entry("apibase", "net.fabricmc.fabric-api:fabric-api-base:+"),
        Map.entry("apiblockrenderlayer", "net.fabricmc.fabric-api:fabric-blockrenderlayer-v1:+"),
        Map.entry("apicommand", "net.fabricmc.fabric-api:fabric-command-api-v1:+"),
        Map.entry("apiscreenhandler", "net.fabricmc.fabric-api:fabric-screen-handler-api-v1:+"),
        Map.entry("apieventsinteraction", "net.fabricmc.fabric-api:fabric-events-interaction-v0:+"),
        Map.entry("apikeybindings", "net.fabricmc.fabric-api:fabric-key-binding-api-v1:+"),
        Map.entry("apilifecycleevents", "net.fabricmc.fabric-api:fabric-lifecycle-events-v1:+"),
        Map.entry("apinetworking", "net.fabricmc.fabric-api:fabric-networking-v0:+"),
        Map.entry("apirendererapi", "net.fabricmc.fabric-api:fabric-renderer-api-v1:+"),
        Map.entry("apirendererindigo", "net.fabricmc.fabric-api:fabric-renderer-indigo:+"),
        Map.entry("apiresourceloader", "net.fabricmc.fabric-api:fabric-resource-loader-v0:+"),
        Map.entry("apitagextensions", "net.fabricmc.fabric-api:fabric-tag-extensions-v0:+"),
        Map.entry("arrp", "net.devtech:arrp:+"),
        Map.entry("astromine", "com.github.Chainmail-Studios:Astromine:1.8.1"),
        Map.entry("autoconfig", "me.sargunvohra.mcmods:autoconfig1u:+"),
        Map.entry("basicmath", "me.shedaniel.cloth:basic-math:+"),
        Map.entry("bason", "user11681:bason:+"),
        Map.entry("cardinalcomponents", "io.github.onyxstudios.Cardinal-Components-API:Cardinal-Components-API:+"),
        Map.entry("cardinalcomponentsbase", "io.github.onyxstudios.Cardinal-Components-API:cardinal-components-base:+"),
        Map.entry("cardinalcomponentsentity", "io.github.onyxstudios.Cardinal-Components-API:cardinal-components-entity:+"),
        Map.entry("cardinalcomponentsitem", "io.github.onyxstudios.Cardinal-Components-API:cardinal-components-item:+"),
        Map.entry("cell", "user11681:cell:+"),
        Map.entry("clothconfig", "me.shedaniel.cloth:config-2:+"),
        Map.entry("cottonresources", "io.github.cottonmc:cotton-resources:+"),
        Map.entry("commonformatting", "user11681:commonformatting:+"),
        Map.entry("dynamicentry", "user11681:dynamicentry:+"),
        Map.entry("fabricasm", "com.github.Chocohead:Fabric-ASM:master-SNAPSHOT"),
        Map.entry("fabricasmtools", "user11681:fabricasmtools:+"),
        Map.entry("gfh", "net.devtech:grossfabrichacks:0.+"),
        Map.entry("invisiblelivingentities", "user11681:invisiblelivingentities:+"),
        Map.entry("javaparser", "com.github.javaparser:javaparser-symbol-solver-core:+"),
        Map.entry("joor", "org.jooq:joor-java-8:+"),
        Map.entry("junit", "org.junit.jupiter:junit-jupiter:+"),
        Map.entry("liltaterreloaded", "com.github.Yoghurt4C:LilTaterReloaded:fabric-1.16-SNAPSHOT"),
        Map.entry("limitless", "user11681:limitless:+"),
        Map.entry("modmenu", "io.github.prospector:modmenu:1.14.6+"),
        Map.entry("multiconnect", "net.earthcomputer:multiconnect:+:api"),
        Map.entry("narratoroff", "user11681:narratoroff:+"),
        Map.entry("noauth", "user11681:noauth:+"),
        Map.entry("optional", "user11681:optional:+"),
        Map.entry("phormat", "user11681:phormat:+"),
        Map.entry("projectfabrok", "user11681:projectfabrok:+"),
        Map.entry("prone", "user11681:prone:+"),
        Map.entry("reachentityattributes", "com.jamieswhiteshirt:reach-entity-attributes:+"),
        Map.entry("reflect", "user11681:reflect:+"),
        Map.entry("rei", "me.shedaniel:RoughlyEnoughItems:+"),
        Map.entry("shortcode", "user11681:shortcode:+"),
        Map.entry("toml4j", "com.moandjiezana.toml:toml4j:+"),
        Map.entry("unsafe", "net.gudenau.lib:unsafe:+")
    ));

    public static String repository(Object key) {
        return repositoryMap.get(key.toString().replace("-", "").toLowerCase(Locale.ROOT));
    }

    public static void repository(String key, String value) {
        repositoryMap.put(key, value);
    }

    public static String artifact(Object key) {
        return artifactMap.get(key.toString().replace("-", "").toLowerCase(Locale.ROOT));
    }

    public static void artifact(String key, String value) {
        artifactMap.put(key, value);
    }

    public void setJavaVersion(Object version) {
        this.javaVersion = JavaVersion.toVersion(version);
    }
}
