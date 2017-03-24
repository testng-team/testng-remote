
import com.beust.kobalt.api.Project
import com.beust.kobalt.buildScript
import com.beust.kobalt.plugin.packaging.assemble
import com.beust.kobalt.plugin.publish.bintray
import com.beust.kobalt.project
import com.beust.kobalt.test
import org.apache.maven.model.License
import org.apache.maven.model.Model
import org.apache.maven.model.Scm

val bs = buildScript {
    // All the subprojects launch servers on similar ports, so they can't run
    // in parallel
    kobaltOptions("--sequential")
}

val autoServiceVersion = "1.0-rc3"
val gsonVersion = "2.7"
val mainTestNgVersion = "6.10"
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
        compile("org.testng:testng:$mainTestNgVersion")
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

fun Project.defineProject(v: String, testNgVersion: String, exclude: Boolean = true) {
    name = "testng-remote$v"
    group = "org.testng.testng-remote"
    version = projectVersion
    directory = "remote$v"
    testsDependOnProjects(remote)

    dependencies {
        compile("com.google.guava:guava:19.0",
                "com.google.code.gson:gson:$gsonVersion")
        if (exclude) exclude("org.testng:testng:$mainTestNgVersion")
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
    defineProject("6_10", "6.10", exclude = false)
}

val remote6_9_10 = project {
    defineProject("6_9_10", "6.9.10")
}

val remote6_9_7 = project {
    defineProject("6_9_7", "6.9.7")
}

val remote6_5 = project {
    defineProject("6_5", "6.5.1")
}

val remote6_0 = project {
    defineProject("6_0", "6.0")
}
