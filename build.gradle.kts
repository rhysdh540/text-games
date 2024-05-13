import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.Deflater

plugins {
    java; idea
}

allprojects {
    apply {
        plugin("java")
        plugin("idea")
    }
    group = "dev.rdh"
    version = "0.1"

    repositories {
        mavenCentral()
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    dependencies {
        compileOnly("com.google.code.findbugs:jsr305:3.0.2")
        "org.projectlombok:lombok:1.18.20".also {
            compileOnly(it); annotationProcessor(it)
        }
    }

    tasks.jar {
        doLast {
            compressJar(archiveFile.get().asFile)
        }
    }

    afterEvaluate {
        tasks.register<JavaExec>("run") {
            classpath(sourceSets.main.get().runtimeClasspath, rootProject.configurations.getByName("runtimeClasspath"))
            mainClass = tasks.jar.get().manifest.attributes["Main-Class"] as String
        }
    }
}

subprojects {
    if(name != "common") {
        dependencies {
            implementation(project(":common"))
        }

        tasks.jar {
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
            from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
        }
    }

    base.archivesName = project.name.lowercase()

    rootProject.dependencies {
        implementation(project)
    }
}

base.archivesName = "allgames"

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.WARN
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    manifest {
        attributes["Main-Class"] = "dev.rdh.games.Main"
    }
}

fun compressJar(jar: File) {
    val contents = linkedMapOf<String, ByteArray>()
    JarFile(jar).use {
        it.entries().asSequence().forEach { entry ->
            if (!entry.isDirectory) {
                contents[entry.name] = it.getInputStream(entry).readAllBytes()
            }
        }
    }
    jar.delete()

    JarOutputStream(jar.outputStream()).use {
        it.setLevel(Deflater.BEST_COMPRESSION)
        it.setMethod(JarOutputStream.DEFLATED)
        contents.forEach { (name, bytes) ->
            it.putNextEntry(JarEntry(name))
            it.write(bytes)
            it.closeEntry()
        }
    }
}
