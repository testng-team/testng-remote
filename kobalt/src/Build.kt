import com.beust.kobalt.*
import com.beust.kobalt.api.*
import com.beust.kobalt.plugin.packaging.*
import com.beust.kobalt.plugin.application.*
import com.beust.kobalt.plugin.publish.*
import com.beust.kobalt.plugin.java.*
import org.apache.maven.model.*

val bs = buildScript {
    // All the subprojects launch servers on similar ports, so they can't run
    // in parallel
    kobaltOptions("--sequential")
}

val autoServiceVersion = "1.0-rc3"
val bintrayPackage = "testng-remote"
val bintrayRepo = "testng"
val gsonVersion = "2.7"
val mavenCompilerSource = "1.7"
val mavenCompilerTarget = "1.7"
val projectBuildSourceEncoding = "UTF-8"

val projectVersion = "1.3.0"

val remote = project {

    name = "testng-remote"
    group = "org.testng.testng-remote"
    artifactId = name
    version = projectVersion
    directory = "remote"

    dependencies {
        compile("com.google.code.gson:gson:$gsonVersion",
                "com.google.auto.service:auto-service:$autoServiceVersion"
                )
    }

    dependenciesTest {
        compile("org.testng:testng:6.10")
    }

    assemble {
        jar {
        }
    }

    bintray {
        publish = true
    }

    pom = Model().apply {
        name = project.name
        description = "TestNG remote"
        url = "http://testng.org"
        licenses = listOf(License().apply {
            name = "Apache-2.0"
            url = "http://www.apache.org/licenses/LICENSE-2.0"
        })
        scm = Scm().apply {
            url = "http://github.com/cbeust/testng"
            connection = "https://github.com/cbeust/testng.git"
            developerConnection = "git@github.com:cbeust/testng.git"
        }
    }
}

fun Project.defineProject(n: String, d: String, testNgVersion: String, exclude: Boolean = true) {
    this.name = n
    group = "org.testng.testng-remote"
    version = projectVersion
    directory = d
    testsDependOnProjects(remote)

    dependencies {
        compile("com.google.guava:guava:19.0",
                "com.google.code.gson:gson:$gsonVersion")
        if (exclude) exclude("org.testng:testng:6.10")
    }

    dependenciesTest {
        compile("org.testng:testng:$testNgVersion")
    }

    assemble {
        jar {
        }
    }

    bintray {
        publish = true
    }

    test {
        jvmArgs("-Dtest.resources.dir=../remote/src/test/resources")
    }
}

val remote6_10 = project {
    defineProject("testng-remote6_10", "remote6_10", "6.10", exclude = false)
}

val remote6_9_10 = project {
    defineProject("testng-remote6_9_10", "remote6_9_10", "6.9.10")
}

val remote6_9_7 = project {
    defineProject("testng-remote6_9_7", "remote6_9_7", "6.9.7")
}

val remote6_5 = project {
    defineProject("testng-remote6_5", "remote6_5", "6.5.1")
}

val remote6_0 = project {
    defineProject("testng-remote6_0", "remote6_0", "6.0")
}
