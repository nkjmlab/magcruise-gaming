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

import java.util.Arrays;
import gnu.lists.LList;
import gnu.mapping.Procedure1;
import gnu.mapping.ProcedureN;
import kawa.standard.Scheme;

public class GetDecision {
  public static void main(String[] args) throws Throwable {
    Scheme s = Scheme.getInstance();
    s.defineFunction(
        "require-decisions-of-player",
        new ProcedureN() {
          @Override
          public Object applyN(Object[] args) throws Throwable {
            System.out.println(Arrays.toString(args));
            System.out.println(args[0].getClass() + ":" + args[0].toString());
            System.out.println(args[1].getClass() + ":" + args[1].toString());
            System.out.println(args[2].getClass() + ":" + args[2].toString());
            System.out.println(args[3].getClass() + ":" + args[3].toString());
            System.out.println(args[4].getClass() + ":" + args[4].toString());
            if (args[5] instanceof LList) {

            } else if (args[5] instanceof LList) {
              System.out.println(args[5].getClass() + ": count:" + ((LList) args[5]).size());
            }
            return null;
          }
        });
    s.defineFunction(
        "get-game-round",
        new Procedure1() {
          @Override
          public Object apply1(Object arg0) throws Throwable {
            return 1;
          }
        });
    s.defineFunction(
        "get-agent-name",
        new Procedure1() {
          @Override
          public Object apply1(Object arg0) throws Throwable {
            return "Player1";
          }
        });
    s.define("simenv", null);
    s.define("self", null);
    // id roundNumber class playerName label decisions)
    s.eval(
        "(require-decisions-of-player \"decision1\" (get-game-round simenv) \"fishers\""
            + "(get-agent-name self) \"decision required:\""
            + "'() )"
        /*
         * "(require-decisions-of-player \"decision1\" (get-game-round simenv) \"fishers\""
         * + "(get-agent-name self) \"decision required:\"" +
         * "'((\"名前\" \"name\" \"input\")" +
         * "(\"性別\" \"sex\" \"choice\" (\"男性\" \"女性\") (\"male\" \"female\") ))"
         * + ")"
         */
        /*
         * "(decision \"意思決定1\" 1 \"fishers\" \"player1\" \"fishers decision\""
         * +
         * " '((\"名前\" \"name\" \"input\")(\"性別\" \"sex\" \"choice\" (\"男性\" \"女性\") (\"0\" \"1\")))"
         * + ")"
         */
        );
  }
}
