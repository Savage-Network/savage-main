plugins {
    id("java")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":core-bukkit"))

    // PostgreSQL e HikariCP (necess�rio para runtime)
    implementation("org.postgresql:postgresql:42.7.1")
    implementation("com.zaxxer:HikariCP:5.1.0")

    compileOnly("com.hpfxd.pandaspigot:pandaspigot-api:1.8.8-R0.1-SNAPSHOT")
    compileOnly(files("../libs/panda-server.jar",))

}

repositories {
    mavenLocal()  // ← Isso é importante!
    mavenCentral()
}

tasks.register<Copy>("copyJar") {
    dependsOn("jar")

    from("build/libs/auth-1.0.0-SNAPSHOT.jar")
    into("C:/Users/Douglas/OneDrive/Área de Trabalho/server/hub-principal/plugins/")
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}