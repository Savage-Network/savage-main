plugins {
    id("java")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":core-bukkit"))

    compileOnly(files("../libs/spigot.jar", "../libs/slime.jar", "../libs/protocol.jar"))
}

tasks.register<Copy>("copyJar") {
    dependsOn("jar")

    from("build/libs/bedwars-0.0.1-SNAPSHOT.jar")
    into("../server/bedwars-list/server-1/plugins")
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}