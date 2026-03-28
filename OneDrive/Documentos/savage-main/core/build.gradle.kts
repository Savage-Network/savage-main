plugins {
    id("java")
}

dependencies {
    implementation("redis.clients:jedis:5.1.2")
    implementation("org.postgresql:postgresql:42.7.3")
    implementation("com.zaxxer:HikariCP:5.1.0")

    compileOnly(files("../libs/spigot.jar", "../libs/bungee.jar", "../libs/slime.jar"))
}