plugins { java }

group = "pl.airr"
version = "0.1.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-releases/")
}

dependencies {
    compileOnly("dev.folia:folia-api:26.1.2.build.8-stable")
    compileOnly("com.github.retrooper:packetevents-spigot:2.13.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.14.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// Java 8 bytecode (1.8.8+ servers), but the Folia API is published for Java 25 - without this Gradle refuses the variant
java { disableAutoTargetJvm() }

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(8)
    options.compilerArgs.add("-Xlint:-options")
}

tasks.test { useJUnitPlatform() }

tasks.processResources {
    filesMatching("plugin.yml") { expand("version" to project.version) }
}

tasks.jar {
    archiveFileName.set("CinderAC-${project.version}.jar")
}
