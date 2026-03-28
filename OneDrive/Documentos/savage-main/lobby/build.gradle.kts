plugins {
    id("java")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":core-bukkit"))

    compileOnly(files("../libs/spigot.jar"))
    compileOnly(files("../libs/ProtocolLib.jar"))
    
    // ZXing para geração de QR Code
    implementation("com.google.zxing:core:3.5.1")
    implementation("com.google.zxing:javase:3.5.1")
}

tasks.register<Copy>("copyJar") {
    dependsOn("jar")

    from("build/libs/lobby-1.0.0-SNAPSHOT.jar")
    into("../server/hubs/main/plugins")
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}