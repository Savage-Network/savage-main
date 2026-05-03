plugins {
    id("java")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":core-bukkit"))

    // PostgreSQL e HikariCP (necess�rio para runtime)
    implementation("org.postgresql:postgresql:42.7.1")
    implementation("com.zaxxer:HikariCP:5.1.0")

    compileOnly(files("../libs/slime.jar"))
    compileOnly(files("../libs/zartema-api.jar"))
    compileOnly(files("../libs/protocol.jar", "../libs/panda-server.jar",))
    compileOnly("com.hpfxd.pandaspigot:pandaspigot-api:1.8.8-R0.1-SNAPSHOT")

}

repositories {
    mavenLocal()  // ← Isso é importante!
    mavenCentral()
}

tasks.register<Copy>("copyJar") {
    dependsOn("jar")

    from("build/libs/bedwars-1.0.0-SNAPSHOT.jar")
    into("../server/bedwars/plugins/")
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    finalizedBy("copyJar")
}