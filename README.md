weka-option-utils
=================

Library for making life easier when dealing with Weka option handling.

Example
-------

Below an example for a `java.io.File` option called `model`, with the `-model`
command-line flag. The tip text from the GUI is also used in the `listOptions`
method which generates the help on the command-line.

```java
  public static final String MODEL = "model";

  @Override
  public Enumeration listOptions() {
    Vector result = new Vector();
    WekaOptionUtils.addOption(result, modelTipText(), "" + getDefaultModel(), MODEL);
    WekaOptionUtils.add(result, super.listOptions());
    return WekaOptionUtils.toEnumeration(result);
  }

  @Override
  public void setOptions(String[] options) throws Exception {
    setModel(WekaOptionUtils.parse(options, MODEL, getDefaultModel()));
    super.setOptions(options);
  }

  @Override
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
    reset();
  }

  public File getModel() {
    return m_Model;
  }

  public String modelTipText() {
    return "The model file to load and use.";
  }
```


Releases
--------

Click on one of the following links to get to the corresponding release page:

* [2016.6.9](https://github.com/fracpete/weka-option-utils/releases/v2016.6.9)


Maven
-----

Add the following dependency in your `pom.xml` to include the package:

```xml
    <dependency>
      <groupId>com.github.fracpete</groupId>
      <artifactId>weka-option-utils</artifactId>
      <version>2016.8.22</version>
      <type>jar</type>
      <exclusions>
        <exclusion>
          <groupId>nz.ac.waikato.cms.weka</groupId>
          <artifactId>weka-dev</artifactId>
        </exclusion>
      </exclusions>
    </dependency>
```

