/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 MAGCruise
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.magcruise.broker.test;

import java.util.Map;
import gnu.lists.LList;
import gnu.mapping.Procedure;
import kawa.standard.Scheme;

public class ConvertUtils {
  public static String toString(Object value) {
    if (value instanceof LList) {
      return listToString((LList) value);
    } else {
      return value.toString();
    }
  }

  public static String listToString(LList value) {
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < value.size(); i++) {
      if (i != 0) b.append(", ");
      b.append(value.get(i).toString() + ":" + value.get(i).getClass().getName());
    }
    return b.toString();
  }

  public static String[] listToStrings(LList value) {
    String[] ret = new String[value.size()];
    for (int i = 0; i < ret.length; i++) {
      if (value.get(i) == null) {
        ret[i] = null;
      } else {
        ret[i] = value.get(i).toString();
      }
    }
    return ret;
  }

  public static String[] namesToValues(LList names, Map<String, String> nameToValues) {
    int n = names.size();
    String[] ret = new String[n];
    for (int i = 0; i < n; i++) {
      ret[i] = nameToValues.get(names.get(i));
    }
    return ret;
  }

  public static Number stringToNumber(String value) {
    try {
      Scheme scm = new Scheme();

      Number result = (Number) ((Procedure) scm.eval("string->number")).apply1(value);
      return result;
    } catch (Throwable e) {
      e.printStackTrace();
    }
    return null;
  }
}
