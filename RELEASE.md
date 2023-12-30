Release Remote TestNG
====

### Perform release

1. Create a release branch like `release/1.6.0`
2. Go to https://github.com/testng-team/testng-remote/actions/workflows/release.yml
  * Select the `branch` you just created: `release/1.6.0`
  * Set `releaseVersion`: the new release version, for example, `1.6.0`
  * Set `developmentVersion`: the next development version, for example, `1.6.1-SNAPSHOT` 
