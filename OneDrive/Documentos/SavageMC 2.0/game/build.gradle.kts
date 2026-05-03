plugins {
    id("java")
}

group = "com.minecraft.game"
version = "unspecified"

dependencies {
    implementation(project(":core"))
    implementation(project(":core-bukkit"))

    implementation("org.postgresql:postgresql:42.7.1")
    implementation("com.zaxxer:HikariCP:5.1.0")

    compileOnly(files("../libs/zartema-api.jar", "../libs/panda-server.jar", "../libs/panda-api.jar"))
    compileOnly("com.hpfxd.pandaspigot:pandaspigot-api:1.8.8-R0.1-SNAPSHOT")
}

repositories {
    mavenLocal()  // ← Isso é importante!
    mavenCentral()
}

val copyJarDestinations = mapOf(
    "lobby-main" to "../server/lobby-main/plugins/",
)

copyJarDestinations.forEach { (name, dest) ->
    tasks.register<Copy>("copyJarTo$name") {
        dependsOn("jar")
        from("build/libs/lobby-1.0.0-SNAPSHOT.jar")
        into(dest)
    }
}

tasks.register("copyJar") {
    dependsOn("jar")
    copyJarDestinations.keys.forEach { name ->
        dependsOn("copyJarTo$name")
    }
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    finalizedBy("copyJar")
}