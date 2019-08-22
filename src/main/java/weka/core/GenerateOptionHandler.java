/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * GenerateOptionHandler.java
 * Copyright (C) 2019 University of Waikato, Hamilton, NZ
 */

package weka.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper class for generating the option handling code for classes that
 * implement Weka's OptionHandler interface. The generator outputs an abstract
 * class with all the option handling code that the user then subclasses to
 * fill in the actual code. With this approach, additional options can be
 * added and the abstract regenerated without affecting the actual code.
 * The name of the abstract class can be tweaked by providing a prefix
 * and/or suffix.
 * <br>
 * The following JSON configuration will generate a base class for a support
 * vector machine: weka.classifiers.functions.AbstractMySVM
 * <pre>
 * {
 *   "name": "MySVM",
 *   "package": "weka.classifiers.functions",
 *   "prefix": "Abstract",
 *   "suffix": "",
 *   "superclass": "weka.classifiers.AbstractClassifier",
 *   "implement": [],
 *   "author": "FracPete",
 *   "organization": "University of Waikato, Hamilton, NZ",
 *   "options": [
 *     {
 *       "property": "capacity",
 *       "type": "double",
 *       "flag": "capacity",
 *       "default": "1.0",
 *       "help": "The capacity parameter."
 *     },
 *     {
 *       "property": "kernel",
 *       "type": "weka.classifiers.smile.math.kernel.AbstractSmileKernel",
 *       "flag": "kernel",
 *       "default": "new weka.classifiers.smile.math.kernel.SmileGaussianKernel()",
 *       "help": "The kernel to use."
 *     },
 *     {
 *       "property": "multiClassStrategy",
 *       "type": "smile.classification.SVM.Multiclass",
 *       "flag": "multiclass-strategy",
 *       "default": "smile.classification.SVM.Multiclass.ONE_VS_ALL",
 *       "help": "The strategy to use in case of non-binary class attribute."
 *     }
 *   ]
 * }
 * </pre>
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class GenerateOptionHandler
  implements Serializable {

  /** the GPL preamble. */
  public final static String GPL = "/*\n" +
    " *   This program is free software: you can redistribute it and/or modify\n" +
    " *   it under the terms of the GNU General Public License as published by\n" +
    " *   the Free Software Foundation, either version 3 of the License, or\n" +
    " *   (at your option) any later version.\n" +
    " *\n" +
    " *   This program is distributed in the hope that it will be useful,\n" +
    " *   but WITHOUT ANY WARRANTY; without even the implied warranty of\n" +
    " *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n" +
    " *   GNU General Public License for more details.\n" +
    " *\n" +
    " *   You should have received a copy of the GNU General Public License\n" +
    " *   along with this program.  If not, see <http://www.gnu.org/licenses/>.\n" +
    " */\n";

  /**
   * Single option definition.
   */
  public static class Option
    implements Serializable {

    /** the property name. */
    public String property;

    /** the type. */
    public String type;

    /** the flag. */
    public String flag;

    /** the default value. */
    public String defaultValue = "";

    /** the help string. */
    public String help = "";

    /**
     * Outputs the option a string.
     *
     * @return		the string representation
     */
    @Override
    public String toString() {
      return "property: " + property + "\n"
	+ "type: " + type + "\n"
	+ "flag: " + flag + "\n"
	+ "default: " + defaultValue + "\n"
	+ "help: " + help;
    }
  }

  /**
   * The class definition.
   */
  public static class Definition
    implements Serializable {

    /** the name of the class. */
    public String name;

    /** the package of the class. */
    public String pkg = "";

    /** the prefix. */
    public String prefix = "";

    /** the suffix. */
    public String suffix = "";

    /** the superclass. */
    public String superclass = "";

    /** the list of interfaces that the class should implement. */
    public List<String> implement = new ArrayList<>();

    /** the author. */
    public String author;

    /** the organization. */
    public String organization;

    /** the options. */
    public List<Option> options = new ArrayList<>();

    /**
     * Returns a string representation of the definition.
     *
     * @return		the string representation
     */
    @Override
    public String toString() {
      StringBuilder	result;
      int		i;

      result = new StringBuilder();
      result.append("name: " + name + "\n");
      result.append("package: " + pkg + "\n");
      result.append("prefix: " + prefix + "\n");
      result.append("suffix: " + suffix + "\n");
      result.append("superclass: " + superclass + "\n");
      result.append("implement: " + implement + "\n");
      result.append("author: " + author + "\n");
      result.append("organization: " + organization + "\n");
      result.append("options:\n");
      if (options.size() == 0) {
        result.append("-none-");
      }
      else {
	for (i = 0; i < options.size(); i++)
	  result.append((i+1) + ". " + options.get(i) + "\n");
      }

      return result.toString();
    }
  }

  /** for logging. */
  protected Logger m_Logger;

  /** the JSON configuration file. */
  protected List<File> m_Configurations;

  /** the output directory. */
  protected File m_OutputDir;

  /** whether to add the package structure to the output filename. */
  protected boolean m_AddPackageStructure;

  /** whether to generate the directories. */
  protected boolean m_GenerateDirs;

  /** whether to be verbose. */
  protected boolean m_Verbose;

  /** the parsed definition. */
  protected Definition m_Definition;

  /**
   * Initializes the generator.
   */
  public GenerateOptionHandler() {
    m_Definition = null;
    setConfigurations(new ArrayList<>());
    setOutputDir(new File("."));
    setAddPackageStructure(false);
    setGenerateDirs(false);
    setVerbose(false);
  }

  /**
   * Returns the logger to use.
   *
   * @return		the logger
   */
  protected Logger getLogger() {
    if (m_Logger == null)
      m_Logger = Logger.getLogger(getClass().getName());
    return m_Logger;
  }

  /**
   * Sets the configuration files.
   *
   * @param value	the files
   */
  public void setConfigurations(List<File> value) {
    m_Configurations = value;
  }

  /**
   * Returns the configuration files.
   *
   * @return		the files
   */
  public List<File> getConfigurations() {
    return m_Configurations;
  }

  /**
   * Sets the output dir.
   *
   * @param value	the dir
   */
  public void setOutputDir(File value) {
    m_OutputDir = value;
  }

  /**
   * Returns the output dir.
   *
   * @return		the dir
   */
  public File getOutputDir() {
    return m_OutputDir;
  }

  /**
   * Sets whether to add the package structure to the output filename.
   *
   * @param value	true if to add
   */
  public void setAddPackageStructure(boolean value) {
    m_AddPackageStructure = value;
  }

  /**
   * Returns whether to the package structure to the output filename.
   *
   * @return		true if to add
   */
  public boolean getAddPackageStructure() {
    return m_AddPackageStructure;
  }

  /**
   * Sets whether to be generate output dirs.
   *
   * @param value	true if generate output dirs
   */
  public void setGenerateDirs(boolean value) {
    m_GenerateDirs = value;
  }

  /**
   * Returns whether to be generate output dirs.
   *
   * @return		true if generate output dirs
   */
  public boolean getGenerateDirs() {
    return m_GenerateDirs;
  }

  /**
   * Sets whether to be verbose in the processing.
   *
   * @param value	true if verbose
   */
  public void setVerbose(boolean value) {
    m_Verbose = value;
  }

  /**
   * Returns whether to be verbose in the processing.
   *
   * @return		true if verbose
   */
  public boolean getVerbose() {
    return m_Verbose;
  }

  /**
   * Sets the commandline options.
   *
   * @param options	the options to use
   * @return		true if successful
   * @throws Exception	in case of an invalid option
   */
  public boolean setOptions(String[] options) throws Exception {
    ArgumentParser parser;
    Namespace ns;

    parser = ArgumentParsers.newArgumentParser(getClass().getName());
    parser.addArgument("--configuration")
      .metavar("JSON")
      .type(Arguments.fileType().verifyExists())
      .setDefault(new File("."))
      .required(true)
      .nargs("+")
      .dest("configuration")
      .help("The JSON file with the class/option specifications.");
    parser.addArgument("--output-dir")
      .metavar("DIR")
      .type(Arguments.fileType().verifyExists().verifyIsDirectory())
      .dest("outputdir")
      .required(true)
      .help("The output directory for the generated class, above the top-level package.");
    parser.addArgument("--add-package-structure")
      .type(Boolean.class)
      .dest("addpackagestructure")
      .required(false)
      .action(Arguments.storeTrue())
      .help("If enabled, the package structure gets added to the output filename.");
    parser.addArgument("--generate-dirs")
      .type(Boolean.class)
      .dest("generatedirs")
      .required(false)
      .action(Arguments.storeTrue())
      .help("If enabled, any missing output directories get generated.");
    parser.addArgument("--verbose")
      .type(Boolean.class)
      .dest("verbose")
      .required(false)
      .action(Arguments.storeTrue())
      .help("If enabled, outputs verbose debugging output.");

    try {
      ns = parser.parseArgs(options);
    }
    catch (ArgumentParserException e) {
      parser.handleError(e);
      return false;
    }

    setConfigurations(ns.getList("configuration"));
    setOutputDir(ns.get("outputdir"));
    setAddPackageStructure(ns.getBoolean("addpackagestructure"));
    setGenerateDirs(ns.getBoolean("generatedirs"));
    setVerbose(ns.getBoolean("verbose"));

    return true;
  }

  /**
   * Closes the reader.
   *
   * @param r		the reader to close
   */
  protected void closeQuietly(Reader r) {
    if (r == null)
      return;
    try {
      r.close();
    }
    catch (Exception e) {
      // ignored
    }
  }

  /**
   * Closes the writer.
   *
   * @param w		the writer to close
   */
  protected void closeQuietly(Writer w) {
    if (w == null)
      return;
    try {
      w.flush();
    }
    catch (Exception e) {
      // ignored
    }
    try {
      w.close();
    }
    catch (Exception e) {
      // ignored
    }
  }

  /**
   * Loads the JSON configuration and checks it.
   *
   * @param config 	the configuration file to load
   * @return		null if successful, otherwise error message
   */
  protected String loadJSON(File config) {
    String		result;
    String		msg;
    JsonParser		parser;
    JsonElement		json;
    JsonObject		obj;
    JsonArray 		array;
    JsonObject		option;
    FileReader		freader;
    BufferedReader	breader;
    Definition		def;
    Option		opt;
    int			i;

    getLogger().info("Reading configuration: " + config);

    result  = null;
    freader = null;
    breader = null;
    obj     = null;
    try {
      freader = new FileReader(config);
      breader = new BufferedReader(freader);
      parser  = new JsonParser();
      json    = parser.parse(breader);
      if (!(json instanceof JsonObject))
        throw new IllegalStateException("JSON file is not an object, instead: " + json.getClass().getName());
      obj = (JsonObject) json;
    }
    catch (Exception e) {
      msg    = "Failed to parse configuration: " + m_Configurations;
      result = msg + "\n" + e;
      getLogger().log(Level.SEVERE, msg, e);
    }
    finally {
      closeQuietly(breader);
      closeQuietly(freader);
    }

    if (obj != null) {
      def = new Definition();
      def.name         = obj.get("name").getAsString();
      def.author       = obj.get("author").getAsString();
      def.organization = obj.get("organization").getAsString();
      if (obj.has("package"))
	def.pkg = obj.get("package").getAsString();
      if (obj.has("prefix"))
        def.prefix = obj.get("prefix").getAsString();
      if (obj.has("suffix"))
        def.suffix = obj.get("suffix").getAsString();
      if (obj.has("superclass"))
        def.superclass = obj.get("superclass").getAsString();
      if (obj.has("implement")) {
        array = obj.getAsJsonArray("implement");
        for (i = 0; i < array.size(); i++)
          def.implement.add(array.get(i).getAsString());
      }
      if (obj.has("options")) {
        array = obj.getAsJsonArray("options");
        for (i = 0; i < array.size(); i++) {
          option           = (JsonObject) array.get(i);
          opt              = new Option();
          opt.property     = option.get("property").getAsString();
          opt.type         = option.get("type").getAsString();
          opt.flag         = option.get("flag").getAsString();
          opt.defaultValue = option.get("default").getAsString();
          if (option.has("help"))
            opt.help = option.get("help").getAsString();
          def.options.add(opt);
	}
      }
      m_Definition = def;
      if (getVerbose())
        getLogger().fine("Parsed definition:\n" + m_Definition);
    }

    return result;
  }

  /**
   * Extracts the last part of the classname.
   *
   * @param clsname	the classname to trim
   * @return		the updated classname
   */
  protected String trimClass(String clsname) {
    if (clsname.contains("."))
      return clsname.substring(clsname.lastIndexOf('.') + 1);
    else
      return clsname;
  }

  /**
   * Turns the first character of the property to uppercase.
   *
   * @param property	the property to process
   * @return		the updated name
   */
  protected String upFirst(String property) {
    return property.substring(0, 1).toUpperCase() + property.substring(1);
  }

  /**
   * Generates the class.
   *
   * @return		null if successful, otherwise error message
   */
  protected String generate() {
    String		result;
    StringBuilder	code;
    Definition		d;
    FileWriter		fwriter;
    BufferedWriter	bwriter;
    String		msg;
    File 		outFile;
    String		outStr;
    int			i;
    boolean 		fromSuper;

    result = null;
    code   = new StringBuilder();
    d      = m_Definition;
    fromSuper = true;

    // preamble
    code.append(GPL);
    code.append("\n");

    // copyright
    code.append("/*\n");
    code.append(" * " + d.name + ".java\n");
    code.append(" * Copyright (C) " + new GregorianCalendar().get(GregorianCalendar.YEAR) + " " + d.organization + "\n");
    code.append(" */\n");
    code.append("\n");

    // package
    if (!d.pkg.isEmpty()) {
      code.append("package " + d.pkg + ";\n");
      code.append("\n");
    }

    // imports
    code.append("import weka.core.Utils;\n");
    code.append("import weka.core.WekaOptionUtils;\n");
    if (!d.superclass.isEmpty())
      code.append("import " + d.superclass + ";\n");
    for (String intf: d.implement) {
      if (intf.equals("weka.core.OptionHandler"))
        fromSuper = false;
      code.append("import " + intf + ";\n");
    }
    code.append("import java.util.ArrayList;\n");
    code.append("import java.util.Enumeration;\n");
    code.append("import java.util.List;\n");
    code.append("import java.util.Vector;\n");
    for (Option o : d.options) {
      if (o.type.contains("."))
	code.append("import " + o.type + ";\n");
    }
    code.append("\n");

    // class javadoc
    code.append("/**\n");
    code.append(" * Superclass for " + d.name + " containing the option handling.\n");
    code.append(" *\n");
    code.append(" * @author " + d.author + "\n");
    code.append(" */\n");

    // class
    code.append("public abstract class " + d.prefix + d.name + d.suffix);
    if (!d.superclass.isEmpty())
      code.append("\n  extends " + trimClass(d.superclass));
    if (!d.implement.isEmpty()) {
      code.append("\n  implements ");
      for (i = 0; i < d.implement.size(); i++) {
        if (i > 0)
          code.append(", ");
        code.append(trimClass(d.implement.get(i)));
      }
    }
    code.append(" {\n");

    // flags
    for (Option o: d.options) {
      code.append("\n");
      code.append("  /** the flag for " + o.property + ". */\n");
      code.append("  public final static String " + o.property.toUpperCase() + " = \"" + o.flag + "\";\n");
    }

    // members
    for (Option o: d.options) {
      code.append("\n");
      code.append("  /** " + (o.help.isEmpty() ? o.property : o.help + " */\n"));
      code.append("  protected " + trimClass(o.type) + " m_" + upFirst(o.property) + " = getDefault" + upFirst(o.property) + "();\n");
    }

    // globalinfo
    code.append("\n");
    code.append("  /**\n");
    code.append("   * Returns a desription of the class.\n");
    code.append("   *\n");
    code.append("   * @return the description\n");
    code.append("   */\n");
    code.append("  public abstract String globalInfo();\n");

    // listOptions
    code.append("\n");
    code.append("  /**\n");
    code.append("   * Returns an enumeration describing the available options.\n");
    code.append("   *\n");
    code.append("   * @return an enumeration of all the available options.\n");
    code.append("   */\n");
    code.append("  @Override\n");
    code.append("  public Enumeration listOptions() {\n");
    code.append("    Vector result = new Vector();\n");
    for (Option o: d.options)
      code.append("    WekaOptionUtils.addOption(result, " + o.property + "TipText(), \"\" + getDefault" + upFirst(o.property) + "(), " + o.property.toUpperCase() + ");\n");
    if (fromSuper)
      code.append("    WekaOptionUtils.add(result, super.listOptions());\n");
    code.append("    return WekaOptionUtils.toEnumeration(result);\n");
    code.append("  }\n");

    // setOptions
    code.append("\n");
    code.append("  /**\n");
    code.append("   * Parses a given list of options.\n");
    code.append("   *\n");
    code.append("   * @param options the list of options as an array of strings\n");
    code.append("   * @throws Exception if an option is not supported\n");
    code.append("   */\n");
    code.append("  @Override\n");
    code.append("  public void setOptions(String[] options) throws Exception {\n");
    for (Option o: d.options) {
      if (o.type.contains("."))
        code.append("    set" + upFirst(o.property) + "((" + trimClass(o.type) + ") WekaOptionUtils.parse(options, " + o.property.toUpperCase() + ", getDefault" + upFirst(o.property) + "()));\n");
      else
        code.append("    set" + upFirst(o.property) + "(WekaOptionUtils.parse(options, " + o.property.toUpperCase() + ", getDefault" + upFirst(o.property) + "()));\n");
    }
    if (fromSuper)
      code.append("    super.setOptions(options);\n");
    code.append("  }\n");

    // getOptions
    code.append("\n");
    code.append("  /**\n");
    code.append("   * Gets the current settings.\n");
    code.append("   *\n");
    code.append("   * @return an array of strings suitable for passing to setOptions\n");
    code.append("   */\n");
    code.append("  @Override\n");
    code.append("  public String[] getOptions() {\n");
    code.append("    List<String> result = new ArrayList<String>();\n");
    for (Option o: d.options)
      code.append("    WekaOptionUtils.add(result, " + o.property.toUpperCase() + ", get" + upFirst(o.property) + "());\n");
    if (fromSuper)
      code.append("    WekaOptionUtils.add(result, super.getOptions());\n");
    code.append("    return WekaOptionUtils.toArray(result);\n");
    code.append("  }\n");

    // default/get/set/tipText methods
    for (Option o: d.options) {
      // default
      code.append("\n");
      code.append("  /**\n");
      code.append("   * The default value for " + o.property + ".\n");
      code.append("   *\n");
      code.append("   * @return the default value\n");
      code.append("   */\n");
      code.append("  protected " + trimClass(o.type) + " getDefault" + upFirst(o.property) + "() {\n");
      code.append("    return " + o.defaultValue + ";\n");
      code.append("  }\n");

      // get
      code.append("\n");
      code.append("  /**\n");
      code.append("   * Returns the current value for " + o.property + ".\n");
      code.append("   *\n");
      code.append("   * @return the current value\n");
      code.append("   */\n");
      code.append("  public " + trimClass(o.type) + " get" + upFirst(o.property) + "() {\n");
      code.append("    return m_" + upFirst(o.property) + ";\n");
      code.append("  }\n");

      // set
      code.append("\n");
      code.append("  /**\n");
      code.append("   * Sets the new value for " + o.property + ".\n");
      code.append("   *\n");
      code.append("   * @param value the new value\n");
      code.append("   */\n");
      code.append("  public void set" + upFirst(o.property) + "(" + trimClass(o.type) + " value) {\n");
      code.append("    m_" + upFirst(o.property) + " = value;\n");
      code.append("  }\n");

      // tiptext
      code.append("\n");
      code.append("  /**\n");
      code.append("   * Returns the help string for " + o.property + ".\n");
      code.append("   *\n");
      code.append("   * @return the help string\n");
      code.append("   */\n");
      code.append("  public String " + o.property + "TipText() {\n");
      code.append("    return \"" + upFirst(o.help) + "\";\n");
      code.append("  }\n");
    }

    // close class
    code.append("}\n");

    // output code
    fwriter = null;
    bwriter = null;
    outStr  = m_OutputDir.getAbsolutePath();
    if (m_AddPackageStructure)
      outStr += File.separator + d.pkg.replace(".", File.separator);
    outStr += File.separator + d.prefix + d.name + d.suffix + ".java";
    outFile = new File(outStr);
    if (m_GenerateDirs && !outFile.getParentFile().exists()) {
      getLogger().info("Generating output directory: " + outFile.getParentFile());
      if (!outFile.getParentFile().mkdirs())
        getLogger().severe("Failed to generate output directory: " + outFile.getParentFile());
    }
    getLogger().info("Writing generated code to: " + outFile);
    try {
      fwriter = new FileWriter(outFile);
      bwriter = new BufferedWriter(fwriter);
      bwriter.write(code.toString());
    }
    catch (Exception e) {
      msg    = "Failed to output generated code to: " + outFile;
      result = msg + "\n" + e;
      getLogger().log(Level.SEVERE, msg, e);
    }
    finally {
      closeQuietly(bwriter);
      closeQuietly(fwriter);
    }

    return result;
  }

  /**
   * Generates the code.
   *
   * @return		null if successful, otherwise error message
   */
  public String execute() {
    String	result;

    result = null;

    for (File config: m_Configurations) {
      m_Definition = null;
      result       = loadJSON(config);
      if (result == null)
	result = generate();
      if (result != null)
        break;
    }

    return result;
  }

  /**
   * Starts the generation from the commandline.
   *
   * @param args	the parameters to use
   * @throws Exception	if providing invalid parameters
   */
  public static void main(String[] args) throws Exception {
    GenerateOptionHandler 	generator;
    String			error;

    generator = new GenerateOptionHandler();
    if (generator.setOptions(args)) {
      error = generator.execute();
      if (error != null) {
	System.err.println(error);
	System.exit(2);
      }
    }
    else {
      System.exit(1);
    }
  }
}
