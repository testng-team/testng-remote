@GrabResolver(name = 'jcenter', root = 'http://jcenter.bintray.com/')
@Grab(group = 'org.testng', module = 'testng', version = '6.9.11')
@Grab(group = 'com.beust', module = 'jcommander', version = '1.48')
@Grab(group = 'org.testng', module = 'testng-remote-dist', version = '1.0.0-SNAPSHOT', classifier = 'shaded')

import org.testng.annotations.Test;
