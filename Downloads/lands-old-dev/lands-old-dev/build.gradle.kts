subprojects {
    apply(plugin = "java")

    group = "com.minecraft.savagemc"
    version = "1.0.0-SNAPSHOT"

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        val lombok = "org.projectlombok:lombok:1.18.34"

        "implementation"(lombok)
        "annotationProcessor"(lombok)
    }
}

val exportDir = "C:/Users/kaust/OneDrive/Área de Trabalho/jars-savage"

val jarModules = mapOf(
    "core-bungee" to "core-bungee-1.0.0-SNAPSHOT.jar",
    "core-bukkit" to "core-bukkit-1.0.0-SNAPSHOT.jar",
    "lobby"       to "lobby-1.0.0-SNAPSHOT.jar",
    "auth"        to "auth-1.0.0-SNAPSHOT.jar",
    "aquatic"     to "aquatic-1.0.0-SNAPSHOT.jar",
    "duels"       to "duels-1.0.0-SNAPSHOT.jar",
    "pvp"         to "pvp-1.0.0-SNAPSHOT.jar"
)

tasks.register("exportAllJars") {
    dependsOn(jarModules.keys.map { ":$it:jar" })

    doLast {
        val dest = file(exportDir)
        dest.mkdirs()

        jarModules.forEach { (module, jarName) ->
            val jar = file("$module/build/libs/$jarName")
            if (jar.exists()) {
                jar.copyTo(file("$exportDir/$jarName"), overwrite = true)
                println("Copiado: $jarName -> $exportDir")
            } else {
                println("AVISO: $jarName não encontrado em $module/build/libs/")
            }
        }
    }
}