def metadata = new XmlSlurper().parse("https://bintray.com/cbeust/maven/download_file?file_path=org%2Ftestng%2Ftestng%2Fmaven-metadata.xml")

metadata.versioning.versions.version.each { version ->
    println "Testing ${version}"
    def exitValue = runTestNGTest(version)
    if (exitValue != 0) {
        println "!!!!!"
        println "!!!!! Test FAILED with version: ${version}"
        println "!!!!!\n"
    }
    else {
        println "!!!!! Test PASSED with version: ${version}"
    }
}

println "\nCompleted!"

def runTestNGTest(ver) {

    def workingDir = new File(getClass().protectionDomain.codeSource.location.path).parent
    def baseDir = new File(workingDir + "/../../../")

    def scriptFile = new File(workingDir, "TestNGTest_${ver}.groovy")

    scriptFile.withWriter { w ->
        new File(workingDir, "TestNGTest.groovy").eachLine { line ->
            if (line.contains("@Grab(group = 'org.testng', module = 'testng', version = '6.9.11')")) {
                w << "@Grab(group = 'org.testng', module = 'testng', version = '${ver}')" + "\n"
            } else {
                w << line + "\n"
            }
        }
    }

    // TODO get the version properties passed by maven via system properties
    def groovyVer = "2.4.7"
    def ivyVer = "2.4.0"
    def mvnRepoDir = System.getenv("HOME") + "/.m2/repository"

    def groovyJar = "${mvnRepoDir}/org/codehaus/groovy/groovy-all/${groovyVer}/groovy-all-${groovyVer}.jar"
    // ivy is used for groovy @Grab annotation
    def ivyJar = "${mvnRepoDir}/org/apache/ivy/ivy/${ivyVer}/ivy-${ivyVer}.jar"

    try {
        def process = new ProcessBuilder(
                "java",
                "-classpath", "${groovyJar}:${ivyJar}",
                "-Dtestng.eclipse.verbose",
                "groovy.ui.GroovyMain",
                scriptFile.absolutePath)
                .directory(baseDir).redirectErrorStream(true).start()
        process.inputStream.eachLine { println it }
        process.waitFor();
        return process.exitValue()
    } finally {
        scriptFile.delete()
    }
}
