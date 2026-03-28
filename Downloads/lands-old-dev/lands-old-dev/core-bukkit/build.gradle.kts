plugins {
    id("java")
}

dependencies {
    implementation(project(":core"))

    // Particle API
    implementation("xyz.xenondevs:particle:1.8.4")

    compileOnly("redis.clients:jedis:5.1.2")

    compileOnly(files("../libs/spigot.jar", "../libs/ProtocolLib.jar"))
}

tasks.register<Copy>("copyJar") {
    dependsOn("jar")

    from("build/libs/core-bukkit-0.0.1-SNAPSHOT.jar")
    into("../server/hub/plugins")
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}