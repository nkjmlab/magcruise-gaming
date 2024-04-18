package org.magcruise.gaming.scm;

import org.nkjmlab.util.java.lang.MethodInvokerInfoUtils;

/**
 * Schemeからこのクラスをインスタンス化して，toStringするとSchemeの行数が表示できる．
 *
 * @author nkjm
 *
 */
public class LineNumber {

  @Override
  public String toString() {
    return getMethodInvokationInfoElement(6);
  }

  public String getMethodInvokationInfoElement(int depth) {
    return MethodInvokerInfoUtils.getInvokerFileNameAndLineNumber(depth,
        new Throwable().getStackTrace());
  }
}
