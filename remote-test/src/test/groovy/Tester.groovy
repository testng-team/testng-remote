@Grab(group = 'org.osgi', module = 'osgi.core', version = '6.0.0')

import org.osgi.framework.Version

//
// global var
//

// need to download the classifier jar for versions <= 5.11
classifierVer = new Version("5.11")

// TODO get the version properties passed by maven via system properties to be consistent with pom.xml
groovyVer = "2.3.11"
ivyVer = "2.3.0"

workingDir = new File(System.getProperty("user.dir"))

// workaround for running mvn from project root
def f = new File(workingDir, "remote-test")
if (f.exists()) {
    workingDir = f
}

scriptDir = new File(workingDir.absolutePath + "/src/test/groovy")

mvnRepoDir = System.getenv("HOME") + "/.m2/repository"

groovyJar = "${mvnRepoDir}/org/codehaus/groovy/groovy-all/${groovyVer}/groovy-all-${groovyVer}.jar"
// ivy is required for groovy @Grab annotation
ivyJar = "${mvnRepoDir}/org/apache/ivy/ivy/${ivyVer}/ivy-${ivyVer}.jar"

grapeRepoDir = System.getenv("HOME") + "/.groovy/grapes"

remoteTestngJar = "${grapeRepoDir}/org.testng/testng-remote-dist/jars/testng-remote-dist-1.0.0-SNAPSHOT-shaded.jar"
jcmdJar = "${grapeRepoDir}/com.beust/jcommander/jars/jcommander-1.48.jar"

resultSet = new HashMap<Integer, Set>()

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

// print the summary report
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


// failed if there's any OTHER failures
assert resultSet[-1] == null

def minVer = new Version("6.5.1")
resultSet[1].each {
    // no version >= 6.5.1 will get error 'unsupported version detected'
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
//            "-Dtestng.eclipse.verbose",
//            "-Dtestng.eclipse.debug",
            "groovy.ui.GroovyMain",
            scriptFile.absolutePath)
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
        new File(scriptDir, "grabJar.groovy").eachLine { line ->
            if (line.contains("@Grab(group = 'org.testng', module = 'testng', version = '6.9.11')")) {
                if (toVersion(ver.toString()).compareTo(classifierVer) > 0) {
                    w << "@Grab(group = 'org.testng', module = 'testng', version = '${ver}')" + "\n"
                } else {
                    w << "@Grab(group = 'org.testng', module = 'testng', version = '${ver}', classifier = 'jdk15')" + "\n"
                }
            } else {
                w << line + "\n"
            }
        }
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
    return new Version(ver)
}
