Test for Remote TestNG
====

## Background

Since TestNG Eclipse [6.9.11](https://github.com/cbeust/testng-eclipse/blob/master/CHANGES.md#6911), 
the plugin does not append any testng.jar to the runtime testng process. Instead, the testng-remote jar is appended,
which is used for communicating between the runtime testng process and the plugin.

When start the testng process, the testng-remote checks the version of runtime testng 
and load the corresponding adapter (aka. version specific) to perform the real testing.

As we can see there are lots of releases in the past, it's sort of hard to let testng-remote support all the versions, 
that's why only testng version >= 6.5.1 are supported.

However, we still want to have a view of how will testng-remote behave against the older versions.

## Usage

```bash
cd testng-remote/remote-test
mvn -e -DskipIntTest=false test
```

## Trouble shooting

* General error during conversion: Error grabbing Grapes -- \[download failed: org.testng\#testng;6.8.3!testng.jar\]
  if we get the above error, we need to clean up the cache of grape and maven:
  ```
  rm -r ~/.groovy/grapes/org.testng/testng
  rm -r ~/.m2/repository/org/testng/testng
  ```
