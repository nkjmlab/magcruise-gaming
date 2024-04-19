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
import gnu.mapping.Procedure2;
import kawa.standard.Scheme;

public class RunScheme_hash_table {
  public static void main(String[] args) throws Throwable {
    Scheme s = Scheme.getInstance();
    s.defineFunction(
        "f",
        new Procedure2() {
          @Override
          public Object apply2(Object arg0, Object arg1) throws Throwable {
            String[] names = {"hello", "world"};
            @SuppressWarnings("unchecked")
            String[] values = ConvertUtils.namesToValues((LList) arg0, (Map<String, String>) arg1);
            for (int i = 0; i < 2; i++) {
              System.out.println(names[i] + " => " + values[i]);
            }
            return null;
          }
        });
    s.eval(
        String.format(
            "(require 'hash-table)(let ((h (make-hash-table)))"
                + "(hash-table-set! h \"hello\" \"world\")"
                + "(hash-table-set! h \"hello2\" \"world2\")"
                + "(f '(\"hello\" \"world\") h))"));
  }
}
