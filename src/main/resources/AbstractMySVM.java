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
 * MySVM.java
 * Copyright (C) 2019 University of Waikato, Hamilton, NZ
 */

package weka.classifiers.functions;

import weka.core.Utils;
import weka.core.WekaOptionUtils;
import weka.classifiers.AbstractClassifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import weka.classifiers.smile.math.kernel.AbstractSmileKernel;
import smile.classification.SVM.Multiclass;

/**
 * Superclass for MySVM containing the option handling.
 *
 * @author FracPete
 */
public abstract class AbstractMySVM
  extends AbstractClassifier {

  /** the flag for capacity. */
  public final static String CAPACITY = "capacity";

  /** the flag for kernel. */
  public final static String KERNEL = "kernel";

  /** the flag for multiClassStrategy. */
  public final static String MULTICLASSSTRATEGY = "multiclass-strategy";

  /** The capacity parameter. */
  protected double m_Capacity = getDefaultCapacity();

  /** The kernel to use. */
  protected AbstractSmileKernel m_Kernel = getDefaultKernel();

  /** The strategy to use in case of non-binary class attribute. */
  protected Multiclass m_MultiClassStrategy = getDefaultMultiClassStrategy();

  /**
   * Returns a desription of the class.
   *
   * @return the description
   */
  public abstract String globalInfo();

  /**
   * Returns an enumeration describing the available options.
   *
   * @return an enumeration of all the available options.
   */
  @Override
  public Enumeration listOptions() {
    Vector result = new Vector();
    WekaOptionUtils.addOption(result, capacityTipText(), "" + getDefaultCapacity(), CAPACITY);
    WekaOptionUtils.addOption(result, kernelTipText(), "" + getDefaultKernel(), KERNEL);
    WekaOptionUtils.addOption(result, multiClassStrategyTipText(), "" + getDefaultMultiClassStrategy(), MULTICLASSSTRATEGY);
    WekaOptionUtils.add(result, super.listOptions());
    return WekaOptionUtils.toEnumeration(result);
  }

  /**
   * Parses a given list of options.
   *
   * @param options the list of options as an array of strings
   * @throws Exception if an option is not supported
   */
  @Override
  public void setOptions(String[] options) throws Exception {
    setCapacity(WekaOptionUtils.parse(options, CAPACITY, getDefaultCapacity()));
    setKernel((AbstractSmileKernel) WekaOptionUtils.parse(options, KERNEL, getDefaultKernel()));
    setMultiClassStrategy((Multiclass) WekaOptionUtils.parse(options, MULTICLASSSTRATEGY, getDefaultMultiClassStrategy()));
    super.setOptions(options);
  }

  /**
   * Gets the current settings.
   *
   * @return an array of strings suitable for passing to setOptions
   */
  @Override
  public String[] getOptions() {
    List<String> result = new ArrayList<String>();
    WekaOptionUtils.add(result, CAPACITY, getCapacity());
    WekaOptionUtils.add(result, KERNEL, getKernel());
    WekaOptionUtils.add(result, MULTICLASSSTRATEGY, getMultiClassStrategy());
    WekaOptionUtils.add(result, super.getOptions());
    return WekaOptionUtils.toArray(result);
  }

  /**
   * The default value for capacity.
   *
   * @return the default value
   */
  protected double getDefaultCapacity() {
    return 1.0;
  }

  /**
   * Returns the current value for capacity.
   *
   * @return the current value
   */
  public double getCapacity() {
    return m_Capacity;
  }

  /**
   * Sets the new value for capacity.
   *
   * @param value the new value
   */
  public void setCapacity(double value) {
    m_Capacity = value;
  }

  /**
   * Returns the help string for capacity.
   *
   * @return the help string
   */
  public String capacityTipText() {
    return "The capacity parameter.";
  }

  /**
   * The default value for kernel.
   *
   * @return the default value
   */
  protected AbstractSmileKernel getDefaultKernel() {
    return new weka.classifiers.smile.math.kernel.SmileGaussianKernel();
  }

  /**
   * Returns the current value for kernel.
   *
   * @return the current value
   */
  public AbstractSmileKernel getKernel() {
    return m_Kernel;
  }

  /**
   * Sets the new value for kernel.
   *
   * @param value the new value
   */
  public void setKernel(AbstractSmileKernel value) {
    m_Kernel = value;
  }

  /**
   * Returns the help string for kernel.
   *
   * @return the help string
   */
  public String kernelTipText() {
    return "The kernel to use.";
  }

  /**
   * The default value for multiClassStrategy.
   *
   * @return the default value
   */
  protected Multiclass getDefaultMultiClassStrategy() {
    return smile.classification.SVM.Multiclass.ONE_VS_ALL;
  }

  /**
   * Returns the current value for multiClassStrategy.
   *
   * @return the current value
   */
  public Multiclass getMultiClassStrategy() {
    return m_MultiClassStrategy;
  }

  /**
   * Sets the new value for multiClassStrategy.
   *
   * @param value the new value
   */
  public void setMultiClassStrategy(Multiclass value) {
    m_MultiClassStrategy = value;
  }

  /**
   * Returns the help string for multiClassStrategy.
   *
   * @return the help string
   */
  public String multiClassStrategyTipText() {
    return "The strategy to use in case of non-binary class attribute.";
  }
}
