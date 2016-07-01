@Grab(group = 'org.osgi', module = 'osgi.core', version = '6.0.0')

import org.osgi.framework.Version

def metadata = new XmlSlurper().parse("https://bintray.com/cbeust/maven/download_file?file_path=org%2Ftestng%2Ftestng%2Fmaven-metadata.xml")

def startTime = System.currentTimeMillis()

metadata.versioning.versions.version.each { version ->
    println ">>>>>"
    println ">>>>> Testing ${version}"
    println ">>>>>"

    def exitValue = runTestNGTest(version)

    println "<<<<<"
    if (exitValue != 0) {
        def ver = toVersion(version.toString())
        def minVer = new Version("6.5.1")

        if (ver.compareTo(minVer) >= 0) {
            println "!!!!! Test FAILED with version: ${version}, BUT IT SHOULD BE PASSED !!!!!"
        } else {
            println "!!!!! Test FAILED with version: ${version}"
        }
    } else {
        println "!!!!! Test PASSED with version: ${version}"
    }
    println "<<<<<\n"
}

println "\nCompleted in " + (System.currentTimeMillis() - startTime) + " (ms)"

def runTestNGTest(ver) {

    def version = toVersion(ver.toString())
    // need to download the classifier jar for versions <= 5.11
    def classifierVer = new Version("5.11")

    def workingDir = new File(System.getProperty("user.dir"))
    def scriptDir = new File(workingDir.absolutePath + "/src/main/groovy")

    def grabScriptFile = new File(scriptDir, "grabJar_${ver}.groovy")

    // start a new process to download the dep jars
    grabScriptFile.withWriter { w ->
        new File(scriptDir, "grabJar.groovy").eachLine { line ->
            if (line.contains("@Grab(group = 'org.testng', module = 'testng', version = '6.9.11')")) {
                if (version.compareTo(classifierVer) > 0) {
                    w << "@Grab(group = 'org.testng', module = 'testng', version = '${ver}')" + "\n"
                } else {
                    w << "@Grab(group = 'org.testng', module = 'testng', version = '${ver}', classifier = 'jdk15')" + "\n"
                }
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

    def downloaded = 1  // default as false
    try {
        def grapeProc = new ProcessBuilder(
                "java",
                "-classpath", "${groovyJar}:${ivyJar}",
                "groovy.ui.GroovyMain",
                grabScriptFile.absolutePath)
                .directory(workingDir).redirectErrorStream(true).start()
        grapeProc.inputStream.eachLine { println it }
        grapeProc.waitFor();
        downloaded = grapeProc.exitValue()
    } finally {
        grabScriptFile.delete()
    }

    if (downloaded != 0) {
        println "failed to download jars for ${ver}, skip testing"
        return -1
    }

    def grapeRepoDir = System.getenv("HOME") + "/.groovy/grapes"
    def remoteTestngJar = "${grapeRepoDir}/org.testng/testng-remote-dist/jars/testng-remote-dist-1.0.0-SNAPSHOT-shaded.jar"
    def testngJar = "${grapeRepoDir}/org.testng/testng/jars/testng-${ver}.jar"
    def jcmdJar = "${grapeRepoDir}/com.beust/jcommander/jars/jcommander-1.48.jar"


    def scriptFile = new File(scriptDir, "TestNGTest.groovy")
    // run the groovy script via Java executable rather than groovy executable, because:
    //      1) groovy has RootLoader loads groovy distributed testng jar, which is in prior to our own jar;
    //      2) groovy @Grad can't specify the order of the jar on the classpath,
    //          while testng-remote need to be on front of testng
    //          (since some older version of testng jar contains older version of RemoteTestNG)
    def process = new ProcessBuilder(
            "java",
            "-classpath", "${groovyJar}:${ivyJar}:${remoteTestngJar}:${testngJar}:${jcmdJar}",
//            "-Dtestng.eclipse.verbose",
//            "-Dtestng.eclipse.debug",
            "groovy.ui.GroovyMain",
            scriptFile.absolutePath)
            .directory(workingDir).redirectErrorStream(true).start()
    process.inputStream.eachLine { println it }
    process.waitFor();
    return process.exitValue()
}

def toVersion(ver) {
    if ("5.5.m".equals(ver)) {
        ver = "5.5"
    }
    return new Version(ver)
}
