Remote TestNG
====

[![Build Status](http://img.shields.io/travis/testng-team/testng-remote.svg)](https://travis-ci.org/testng-team/testng-remote)
[![Coverage Status](https://coveralls.io/repos/github/testng-team/testng-remote/badge.svg)](https://coveralls.io/github/testng-team/testng-remote)

TestNG Remote - the modules for running TestNG remotely. This is normally used by IDE to communicate with TestNG runtime, e.g. receive the Test Result from runtime so that can display them on IDE views.

### Current Release Version

```xml
<repositories>
  <repository>
    <id>jcenter</id>
    <name>bintray</name>
    <url>http://jcenter.bintray.com</url>
  </repository>
</repositories>

<dependency>
  <groupId>org.testng.testng-remote</groupId>
  <artifactId>testng-remote-dist</artifactId>
  <version>1.3.0</version>
  <classifier>shaded</classifier>
</dependency>
```
