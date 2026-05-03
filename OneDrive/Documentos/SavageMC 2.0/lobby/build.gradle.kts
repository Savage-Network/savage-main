plugins {
    id("java")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":core-bukkit"))

    implementation("org.postgresql:postgresql:42.7.1")
    implementation("com.zaxxer:HikariCP:5.1.0")

    compileOnly("com.hpfxd.pandaspigot:pandaspigot-api:1.8.8-R0.1-SNAPSHOT")
    compileOnly(files("../libs/zartema-api.jar", "../libs/panda-server.jar", "../libs/panda-api.jar"))
}

repositories {
    mavenLocal()  // ← Isso é importante!
    mavenCentral()
}

val copyJarDestinations = mapOf(
    "lobby-main" to "C:/Users/Douglas/OneDrive/Área de Trabalho/server/hub-principal/plugins/",
    "hub-bedwars" to "C:/Users/Douglas/OneDrive/Área de Trabalho/server/hub-bedwars/plugins/",
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