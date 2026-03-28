plugins {
    id("java")
}

dependencies {
    implementation(project(":core"))
    implementation("org.javacord:javacord:3.8.0")
    implementation("org.postgresql:postgresql:42.7.3")
    implementation("com.zaxxer:HikariCP:5.1.0")

    compileOnly("redis.clients:jedis:5.1.2")

    compileOnly(files("../libs/bungee.jar"))
}

tasks.register<Copy>("copyJar") {
    dependsOn("jar")

    from("build/libs/core-bungee-1.0.0-SNAPSHOT.jar")
    into("../server/bungee/plugins")
}

tasks.jar {
    exclude("META-INF/*.RSA", "META-INF/*.SF'", "META-INF/*.DSA")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}