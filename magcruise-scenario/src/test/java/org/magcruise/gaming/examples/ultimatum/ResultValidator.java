package org.magcruise.gaming.examples.ultimatum;

import java.util.ArrayList;
import java.util.List;
import org.magcruise.gaming.examples.ValidateUtils;
import org.magcruise.gaming.manager.process.ProcessId;
import org.magcruise.gaming.util.SystemEnvironmentUtils;
import org.nkjmlab.sorm4j.Sorm;

class ResultValidator {

  private static final Integer[] firstPlayerAccounts =
      new Integer[] {
        90000, 170000, 170000, 230000, 280000, 280000, 370000, 450000, 450000, 510000, 510000
      };
  private static final Integer[] secondPlayerAccounts =
      new Integer[] {
        10000, 30000, 30000, 70000, 120000, 120000, 130000, 150000, 150000, 190000, 190000
      };

  private static Sorm util = Sorm.create(SystemEnvironmentUtils.getDefaultDataSource());

  static void validate(ProcessId pid) {
    validate(pid, 0, firstPlayerAccounts.length);
  }

  static void validate(ProcessId pid, int from, int to) {
    checkFirstPlayerResult(pid, firstPlayerAccounts, from, to);
    checkSecondPlayerResult(pid, secondPlayerAccounts, from, to);
  }

  private static void checkFirstPlayerResult(
      ProcessId pid, Object[] firstPlayerAccounts, int fromIndex, int toIndex) {
    List<Integer> db =
        util.readList(
            int.class,
            "SELECT "
                + "ACCOUNT"
                + " FROM "
                + "ORG_MAGCRUISE_GAMING_EXAMPLES_ULTIMATUM_ACTOR_FIRSTPLAYER"
                + " WHERE PROCESS_ID=? ORDER BY ROUNDNUM",
            pid.toString());
    List<Object> actual = new ArrayList<>();
    for (int i = 0; i < fromIndex; i++) {
      actual.add("");
    }
    actual.addAll(db);

    ValidateUtils.validate(firstPlayerAccounts, actual.toArray(), fromIndex, toIndex);
  }

  private static void checkSecondPlayerResult(
      ProcessId pid, Object[] secondPlayerAccounts, int fromIndex, int toIndex) {

    List<Integer> db =
        util.readList(
            int.class,
            "SELECT "
                + "ACCOUNT"
                + " FROM "
                + "ORG_MAGCRUISE_GAMING_EXAMPLES_ULTIMATUM_ACTOR_SECONDPLAYER"
                + " WHERE PROCESS_ID=? ORDER BY ROUNDNUM",
            pid.toString());
    List<Object> actual = new ArrayList<>();
    for (int i = 0; i < fromIndex; i++) {
      actual.add("");
    }
    actual.addAll(db);

    ValidateUtils.validate(secondPlayerAccounts, actual.toArray(), fromIndex, toIndex);
  }
}
