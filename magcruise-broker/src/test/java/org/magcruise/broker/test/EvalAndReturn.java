/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 MAGCruise
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.magcruise.broker.test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.nkjmlab.util.jackson.JacksonMapper;
import gnu.lists.PairWithPosition;
import gnu.mapping.Environment;
import gnu.mapping.Procedure3;
import kawa.standard.Scheme;

public class EvalAndReturn {
  public static class UserInputData {
    public UserInputData() {}

    public UserInputData(String name, String type, String[] values) {
      this.name = name;
      this.type = type;
      this.values = values;
    }

    public String getName() {
      return name;
    }

    public String getType() {
      return type;
    }

    public String[] getValues() {
      return values;
    }

    private String name;
    private String type;
    private String[] values;
  }

  public static void main(String[] args) throws Throwable {
    Scheme s = Scheme.getInstance();
    Environment.setCurrent(s.getEnvironment());
    final List<UserInputData> inputs = new ArrayList<>();
    s.defineFunction(
        "user-input-data",
        new Procedure3() {
          @Override
          public Object apply3(Object arg0, Object arg1, Object arg2) throws Throwable {
            inputs.add(
                new UserInputData(
                    arg0.toString(),
                    arg1.toString(),
                    pairListToStringArray((PairWithPosition) arg2)));
            return null;
          }
        });

    try (InputStream is = EvalAndReturn.class.getResourceAsStream("user-input-data.q")) {
      s.eval(new InputStreamReader(is, "UTF-8"));
      System.out.println(JacksonMapper.getDefaultMapper().toJson(inputs, true));
    }
  }

  private static String[] pairListToStringArray(PairWithPosition pair) {
    List<String> strs = new ArrayList<String>();
    for (Object v : pair) {
      strs.add(v.toString());
    }
    return strs.toArray(new String[] {});
  }
}
