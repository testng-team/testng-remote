import org.testng.remote.RemoteTestNG;
//import org.testng.annotations.BeforeClass;
//import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

class SimpleTest {

//    @BeforeClass
//    def setUp() {
//        println "BeforeClass: setUp"
//    }
//
//    @AfterClass
//    def tearDown() {
//        println "AfterClass: tearDown"
//    }

    @Test
    void testFoo() {
        println "testFoo"
    }
}

def DEBUG = false

if (DEBUG) {
    println "root loader:"
    this.class.classLoader.rootLoader.URLs.each { println it }

    println "\nsystem loader:"
    ClassLoader.systemClassLoader.URLs.each { println it }

    println "\nsub loader"
    this.class.classLoader.URLs.each { println it }

    println "\n"
}

def args = ["-serport", "61497", "-protocol", "stdout", "-d", "./testng-output", "./src/test/resources/testng-remote.xml"] as String[]
RemoteTestNG.main(args)