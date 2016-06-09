How to make a release
=====================

Preparation
-----------

* Change the artifact ID in `pom.xml` to today's date, e.g.:

  ```
  2016.6.6-SNAPSHOT
  ```

* Commit/push all changes


Maven Central
-------------

* Run the following command to deploy the artifact:

  ```
  mvn release:clean release:prepare release:perform
  ```

* After successful deployment, push the changes out:

  ```
  git push
  ```

* After the artifacts show up on central, update the artifact version used
  in the dependency fragment of the `README.md` file

