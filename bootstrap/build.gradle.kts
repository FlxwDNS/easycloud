plugins {
    id("dev.vankka.dependencydownload.plugin") version ("1.3.1")
    kotlin("jvm")
}

repositories {
    mavenCentral()
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation("dev.vankka:dependencydownload-runtime:1.3.1")

    // jline
    runtimeDownload("org.jline:jline:3.30.4")

    // toml
    runtimeDownload("com.akuleshov7:ktoml-core:0.7.0")
    runtimeDownload("com.akuleshov7:ktoml-file:0.7.0")
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    configurations["compileClasspath"].forEach { file: File ->
        if(!file.name.startsWith("dependencydownload-") && !file.name.startsWith("kotlin-")) {
            return@forEach
        }
        from(zipTree(file.absoluteFile))
    }

    from(project(":grpc").tasks.jar)
    from(project(":cluster").tasks.jar)

    manifest {
        attributes["Main-Class"] = "dev.easycloud.BootstrapBootKt"
        attributes["project-version"] = version
    }

    archiveFileName.set("bootstrap.jar")
    dependsOn(
        tasks.named("generateRuntimeDownloadResourceForRuntimeDownloadOnly"),
        tasks.named("generateRuntimeDownloadResourceForRuntimeDownload")
    )
}

tasks.register("runBootstrap") {
    group = "build"
    description = "Builds the jar and runs the bootstrap in a separate terminal."

    dependsOn("jar")

    doLast {
        val jarTask = tasks.named("jar").get() as Jar
        val jarFile = jarTask.archiveFile.get().asFile
        val outputDir = File("C:\\Users\\Radik\\Desktop\\EasyCloudV3")

        copy {
            from(jarFile)
            from("start.bat")
            into(outputDir)
        }

        val batPath = File(outputDir, "start.bat").absolutePath

        ProcessBuilder("cmd", "/c", "start", "", "cmd", "/c", batPath)
            .directory(outputDir)
            .start()

        println("Bootstrap launched. Check the terminal for output.")
    }
}
