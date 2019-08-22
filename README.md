# weka-option-utils

Library for making life easier when dealing with Weka option handling.

There are two classes that help with Weka's option handling:
* `weka.core.WekaOptionUtils` -- simplifies the code in the `listOptions`, 
 `setOptions` and `getOptions` methods.
* `weka.core.GenerateOptionHandler` -- uses a JSON definition from a config file 
  to generate an abstract class that manages all the option handling and the user 
  only needs to subclass it and implement the actual functionality. 


## WekaOptionUtils

Below is an example for a `java.io.File` option called `model`, with the `-model`
command-line flag. The tip text from the GUI is also used in the `listOptions`
method which generates the help on the command-line.

```java
  public static final String MODEL = "model";

  protected File m_Model = getDefaultModel(); 

  public Enumeration listOptions() {
    Vector result = new Vector();
    WekaOptionUtils.addOption(result, modelTipText(), "" + getDefaultModel(), MODEL);
    WekaOptionUtils.add(result, super.listOptions());
    return WekaOptionUtils.toEnumeration(result);
  }

  public void setOptions(String[] options) throws Exception {
    setModel(WekaOptionUtils.parse(options, MODEL, getDefaultModel()));
    super.setOptions(options);
  }

  public String[] getOptions() {
    List<String> result = new ArrayList<>();
    WekaOptionUtils.add(result, MODEL, getModel());
    WekaOptionUtils.add(result, super.getOptions());
    return WekaOptionUtils.toArray(result);
  }

  protected File getDefaultModel() {
    return new File(".");
  }

  public void setModel(File value) {
    m_Model = value;
  }

  public File getModel() {
    return m_Model;
  }

  public String modelTipText() {
    return "The model file to load and use.";
  }
```

## GenerateOptionHandler

The `weka.core.GenerateOptionHandler` class allows you to process one or more
JSON config files to generate abstract classes implementing all the option
handling.

```
usage: weka.core.GenerateOptionHandler
       [-h] --configuration JSON [JSON ...] --output-dir DIR
       [--add-package-structure] [--generate-dirs] [--verbose]

optional arguments:
  -h, --help             show this help message and exit
  --configuration JSON [JSON ...]
                         The    JSON    file    with    the    class/option
                         specifications.
  --output-dir DIR       The output  directory  for  the  generated  class,
                         above the top-level package.
  --add-package-structure
                         If enabled, the  package  structure  gets added to
                         the output filename.
  --generate-dirs        If enabled,  any  missing  output  directories get
                         generated.
  --verbose              If enabled, outputs verbose debugging output.
```

The JSON file structure is very simple:
* `name`: the name of the class, eg `MySVM`
* `package` (optional): the Java package for the class, eg `weka.classifiers.functions`
* `prefix` (optional): prefix to use for the class, eg `Abstract`
* `suffix` (optional): prefix to use for the class, eg `Base`
* `superclass`: the superclass for this class, eg `weka.classifiers.AbstractClassifier`
* `implement` (optional): array of other interfaces to implement, eg `weka.core.OptionHandler`
* `author`: the author to be used in the Javadoc
* `organization`: the organization that owns the copyright
* `options` (optional): array of options

An `option` itself has the following properties:
* `property`: the Java Bean property name (starts with lower case letter), eg `capacity`
* `type`: the string denoting the type (primitive or class), eg `double`
* `flag` (optional): the string to use as command-line option (all lower case, no leading `-`), 
  eg `capacity`; if left empty, will get automatically generated from the property name
  (`multiClassStrategy` -> `multi-class-strategy`)
* `default`: the Java code snippet string with the default value, eg `1.0` or `new some.pkg.SomeClass()`
* `constraint` (optional): the Java code snippet representing a boolean evaluation to 
  guard accepting the value presented to the `set` method (the parameter name used by 
  the `set` method is always `value`), eg `value >= 0`
* `help`: the help string to display in the user interface and to list on the commandline

This [example configuration](src/main/resources/mysvm.json) generates this
[Java source file](src/main/resources/AbstractMySVM.java).

In case you include this artifact in your Maven project, you can add a build
configuration that will regenerate your abstract classes using `mvn exec:java`:

```xml
  ...
  <build>
    ...
    <plugins>
      ...
      <!-- for generating the base classes: mvn exec:java -->
      <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>exec-maven-plugin</artifactId>
        <version>1.2.1</version>
        <executions>
          <execution>
            <goals>
              <goal>java</goal>
            </goals>
          </execution>
        </executions>
        <configuration>
          <mainClass>weka.core.GenerateOptionHandler</mainClass>
          <workingDirectory>.</workingDirectory>
          <arguments>
            <argument>--output-dir</argument>
            <argument>${project.basedir}/src/main/java</argument>
            <argument>--configuration</argument>
            <argument>${project.basedir}/src/main/resources/mysvm.json</argument>
          </arguments>
        </configuration>
      </plugin>
    </plugins>
  </build>
  ...
```

## Releases

Click on one of the following links to get to the corresponding release page:

* [2019.8.22](https://github.com/fracpete/weka-option-utils/releases/v2019.8.22)
* [2016.6.9](https://github.com/fracpete/weka-option-utils/releases/v2016.6.9)


## Maven

Add the following dependency in your `pom.xml` to include the package:

```xml
    <dependency>
      <groupId>com.github.fracpete</groupId>
      <artifactId>weka-option-utils</artifactId>
      <version>2019.8.22</version>
      <type>jar</type>
      <exclusions>
        <exclusion>
          <groupId>nz.ac.waikato.cms.weka</groupId>
          <artifactId>weka-dev</artifactId>
        </exclusion>
      </exclusions>
    </dependency>
```

