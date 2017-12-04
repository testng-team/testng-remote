@Grab(group = 'org.osgi', module = 'osgi.core', version = '6.0.0')
@Grab(group = 'org.testng.testng-remote', module = 'testng-remote', version = '1.4.0-SNAPSHOT')

import org.osgi.framework.Version
import org.testng.remote.strprotocol.JsonMessageSender

//
// global var
//

// need to download the classifier jar for versions <= 5.11
classifierVer = new Version("5.11")

groovyVer = System.getProperty("GROOVY_VERSION") ?: "2.3.11"
ivyVer = System.getProperty("IVY_VERSION") ?: "2.3.0"
testngRemoteVer = System.getProperty("PROJECT_VERSION") ?: "1.4.0-SNAPSHOT"

workingDir = new File(System.getProperty("user.dir"))
if (System.getProperty("PROJECT_BASEDIR")) {
    workingDir = new File(System.getProperty("PROJECT_BASEDIR"))
}

println "\t:::"
println "\t::: workingDir: ${workingDir}"
println "\t::: testng remote version: ${testngRemoteVer}"
println "\t::: groovy version: ${groovyVer}"
println "\t::: ivy version: ${ivyVer}"
println "\t:::"

scriptDir = new File(workingDir.absolutePath + "/src/test/groovy")

mvnRepoDir = System.getenv("HOME") + "/.m2/repository"

groovyJar = "${mvnRepoDir}/org/codehaus/groovy/groovy-all/${groovyVer}/groovy-all-${groovyVer}.jar"
// ivy is required for groovy @Grab annotation
ivyJar = "${mvnRepoDir}/org/apache/ivy/ivy/${ivyVer}/ivy-${ivyVer}.jar"

grapeRepoDir = System.getenv("HOME") + "/.groovy/grapes"

remoteTestngJar = "${grapeRepoDir}/org.testng.testng-remote/testng-remote-dist/jars/testng-remote-dist-${testngRemoteVer}-shaded.jar"
jcmdJar = "${grapeRepoDir}/com.beust/jcommander/jars/jcommander-1.48.jar"

resultSet = new HashMap<Integer, Set>()

//~~


def startTime = System.currentTimeMillis()
def metadata = new XmlSlurper().parse("https://bintray.com/cbeust/maven/download_file?file_path=org%2Ftestng%2Ftestng%2Fmaven-metadata.xml")

metadata.versioning.versions.version.each { version ->
    println ">>>>>"
    println ">>>>> Testing ${version}"
    println ">>>>>"

    def exitValue = runTestNGTest(version)
    def rset = resultSet[exitValue]
    if (rset == null) {
        rset = new ArrayList()
        resultSet[exitValue] = rset
    }
    rset << version

    println "<<<<<"
    println ">>>>> Tested ${version}: result=${exitValue}"
    println "<<<<<\n"
}

println "\nCompleted in " + (System.currentTimeMillis() - startTime) + " (ms)"


println "\nSummary report:"
resultSet.each {
    switch (it.key) {
        case 0:
            println it.key + " - PASSED:"
            break;
        case 1:
            println it.key + " - unsupported version detected:"
            break;
        case 2:
            println it.key + " - NoClassDefFoundError:"
            break;
        default:
            println it.key + " - OTHERS:"
    }

    println "\t" + it.value
}
println ""

// failed if there's any OTHER failures
assert resultSet[-1] == null

def minVer = new Version("6.0")
resultSet[1].each {
    // no version >= 6.0 will get error 'unsupported version detected'
    assert (toVersion(it.toString()).compareTo(minVer) < 0)
}
resultSet[2].each {
    // no version >= 6.5.1 will get error 'NoClassDefFoundError'
    assert (toVersion(it.toString()).compareTo(minVer) < 0)
}

/**
 * run the testng test with groovy in a separate process
 *
 * @param ver the testng version
 * @return 0 - success; 1 - unsupported version detected; 2 - NoClassDefFoundError; -1 - others;
 */
def runTestNGTest(ver) {
    if (downloadTestNG(ver) != 0) {
        println "failed to download jars for ${ver}, skip testing"
        return -1
    }

    def port = 12345
    def msgHub = new JsonMessageSender("localhost", port)
    Thread.start {
        msgHub.initReceiver()
    }

    try {
        def testngJar = "${grapeRepoDir}/org.testng/testng/jars/testng-${ver}.jar"
        if (toVersion(ver.toString()).compareTo(classifierVer) <= 0) {
            testngJar = "${grapeRepoDir}/org.testng/testng/jars/testng-${ver}-jdk15.jar"
        }

        println "classpath: ${groovyJar}:${ivyJar}:${remoteTestngJar}:${testngJar}:${jcmdJar}\n"

        def scriptFile = new File(scriptDir, "TestNGTest.groovy")
        // run the groovy script via Java executable rather than groovy executable, because:
        //      1) groovy has RootLoader loads groovy distributed testng jar, which is in prior to our own jar;
        //      2) groovy @Grad can't specify the order of the jar on the classpath,
        //          while testng-remote need to be on front of testng
        //          (since some older version of testng jar contains older version of RemoteTestNG)
        def output = new StringBuilder()
        def process = new ProcessBuilder(
                "java",
                "-classpath", "${groovyJar}:${ivyJar}:${remoteTestngJar}:${testngJar}:${jcmdJar}",
                "-Dtestng.eclipse.verbose",
                "-Dtestng.eclipse.debug",
                "groovy.ui.GroovyMain",
                scriptFile.absolutePath,
                "json", "" + port)
                .directory(workingDir).redirectErrorStream(true).start()
        process.inputStream.eachLine { println it; output << it.toString() }
        process.waitFor();
        def exitValue = process.exitValue()
        if (exitValue == 0) {
            return 0
        } else {
            if (output.contains("is not a supported TestNG version")) {
                return 1
            } else if (output.contains("java.lang.NoClassDefFoundError")) {
                return 2;
            } else {
                return -1
            }
        }
    } finally {
        msgHub.stopReceiver()
    }
}

/**
 * start a new process to download the dep jars,
 * since @Grad can't specify the order of the jars on the classpath
 * while we do want to make testng-remote on front of testng jar
 * that's why we not put @Grab in the TestNGTest.groovy
 *
 * @param ver the testng version
 * @return 0 - success, otherwise - failure
 */
def downloadTestNG(ver) {
    def grabScriptFile = new File(scriptDir, "grabJar_${ver}.groovy")

    grabScriptFile.withWriter { w ->
        w << "@GrabResolver(name = 'jcenter', root = 'http://jcenter.bintray.com/')" + "\n"

        if (toVersion(ver.toString()).compareTo(classifierVer) > 0) {
            w << "@Grab(group = 'org.testng', module = 'testng', version = '${ver}')" + "\n"
        } else {
            w << "@Grab(group = 'org.testng', module = 'testng', version = '${ver}', classifier = 'jdk15')" + "\n"
        }

        w << "@GrabExclude('com.google.guava:guava')" + "\n"

        w << "@Grab(group = 'com.beust', module = 'jcommander', version = '1.48')" + "\n"
        w << "@Grab(group = 'org.testng.testng-remote', module = 'testng-remote-dist', version = '${testngRemoteVer}', classifier = 'shaded')" + "\n"

        w << "import org.testng.annotations.Test;" + "\n"
    }

    try {
        def grapeProc = new ProcessBuilder(
                "java",
                "-classpath", "${groovyJar}:${ivyJar}",
                "groovy.ui.GroovyMain",
                grabScriptFile.absolutePath)
                .directory(workingDir).redirectErrorStream(true).start()
        grapeProc.inputStream.eachLine { println it }
        grapeProc.waitFor();
        return grapeProc.exitValue()
    } finally {
        grabScriptFile.delete()
    }
}

def Version toVersion(String ver) {
    if ("5.5.m".equals(ver)) {
        ver = "5.5"
    }
    def idx = ver.indexOf("-RC")
    if (idx > 0) {
        ver = ver.substring(0, idx)
    }
    return new Version(ver)
}
