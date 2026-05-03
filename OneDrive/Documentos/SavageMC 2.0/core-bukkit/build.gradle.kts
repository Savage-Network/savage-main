plugins {
    id("java")
}

dependencies {
    implementation(project(":core"))
    implementation("xyz.xenondevs:particle:1.8.4")

    implementation("org.postgresql:postgresql:42.7.1")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("redis.clients:jedis:5.1.2")
    compileOnly("com.hpfxd.pandaspigot:pandaspigot-api:1.8.8-R0.1-SNAPSHOT")

    compileOnly(files("../libs/protocol.jar", "../libs/panda-server.jar", "../libs/panda-api.jar"))
}

repositories {
    mavenLocal()  // ← Isso é importante!
    mavenCentral()
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