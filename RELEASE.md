Release Remote TestNG
====

## Prerequisite

1. An Bintray account joined [TestNG Team](https://bintray.com/testng-team)
2. Have Maven repository and package created in Bintray
  This is defines where to store the release bits on Bintray.
  For example, below uses [test](https://bintray.com/testng-team/test) as the Maven repo, and [testng-remote](https://bintray.com/testng-team/test/testng-remote) as the Package.
  Note: once the repo and package available, we need update `bintray.repo` and `bintray.package` in pom.xml accordingly. 
3. Add your Bintray credentials to Maven settings.xml
  Edit the `$HOME/.m2/settings.xml` on the release server (normally it's your laptop)

  ```xml
  <server>
    <id>bintray-repo-testng-remote</id>
    <username>{bintray-user}</username>
    <password>{bintray-api-key}</password>
  </server>
  ```

  Note #1: the `id` should be same as the `repository` in pom.xml

  Note #2: Bintray REST API [uses API keys](https://bintray.com/docs/api/#_authentication) instead of passwords. (Use the [API Key section](https://bintray.com/docs/usermanual/interacting/interacting_editingyouruserprofile.html#anchorAPIKEY) of your profile settings to obtain your bintray-api-key)

### Prepare for each release

1. Add new Version for the repo package on Bintray
  Follow the doc to create Version: https://bintray.com/docs/usermanual/working/working_versions.html 
  for example, to release `1.0.0` 

### Perform release

One the release server, run the following maven command to release (tagging and upload to Bintray):
```bash
REL_VER=1.0.0
DEV_VER=1.0.1-SNAPSHOT
mvn -e --batch-mode release:prepare release:perform -DreleaseVersion=$REL_VER -DdevelopmentVersion=$DEV_VER
```

## References:

* [Publishing Your Maven Project to Bintray](http://blog.bintray.com/2015/09/17/publishing-your-maven-project-to-bintray/)
* [Publishing releases using Github, Bintray and maven-release-plugin](http://veithen.github.io/2013/05/26/github-bintray-maven-release-plugin.html)
