package org.magcruise.gaming.examples.croquette;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.magcruise.gaming.examples.ValidateUtils;
import org.magcruise.gaming.examples.croquette.actor.Market;
import org.magcruise.gaming.manager.process.ProcessId;
import org.magcruise.gaming.manager.session.GameSessionOnServer;
import org.magcruise.gaming.model.game.ActorName;
import org.magcruise.gaming.model.game.Player;
import org.magcruise.gaming.util.SystemEnvironmentUtils;
import org.nkjmlab.sorm4j.Sorm;
import gnu.mapping.Symbol;

class ResultValidator {
  private static org.apache.logging.log4j.Logger log =
      org.apache.logging.log4j.LogManager.getLogger();
  static Integer[] factoryProfits =
      new Integer[] {0, -6000, -70, 5990, 29990, -29940, -4000, 20000, 11000, 38000, 0};

  static Integer[] shop1Profits =
      new Integer[] {22600, 27200, 7980, 9600, 3890, 7980, 12000, 4400, 14200, 12900, 0};
  static Integer[] shop2Profits =
      new Integer[] {26400, 24000, 9240, 12800, 5940, 5360, 7700, 9700, 10600, 5900, 0};

  private static Sorm util = Sorm.create(SystemEnvironmentUtils.getDefaultDataSource());

  static void validate(ProcessId pid) {
    log.debug("pid={}", pid);
    checkFactoryResult(pid, factoryProfits, 0, factoryProfits.length);
    checkShopResult(pid, shop1Profits, "Shop1", 0, shop1Profits.length);
    checkShopResult(pid, shop2Profits, "Shop2", 0, shop2Profits.length);
  }

  private static void checkShopResult(
      ProcessId pid, Object[] expected, String name, int fromRound, int toRound) {
    List<Integer> db =
        util.readList(
            int.class,
            "SELECT PROFIT FROM "
                + " ORG_MAGCRUISE_GAMING_EXAMPLES_CROQUETTE_ACTOR_SHOP "
                + " WHERE PROCESS_ID=? AND PLAYER_NAME=? AND ROUNDNUM BETWEEN ? AND ? ORDER BY ROUNDNUM",
            pid.toString(),
            name,
            fromRound,
            toRound);
    List<Object> actual = new ArrayList<>();
    for (int i = 0; i < fromRound; i++) {
      actual.add("");
    }
    actual.addAll(db);

    ValidateUtils.validate(expected, actual.toArray(), fromRound, toRound);
  }

  private static void checkFactoryResult(
      ProcessId pid, Object[] expected, int fromRound, int toRound) {
    List<Integer> db =
        util.readList(
            int.class,
            "SELECT PROFIT FROM "
                + " ORG_MAGCRUISE_GAMING_EXAMPLES_CROQUETTE_ACTOR_CROQUETTEFACTORY "
                + " WHERE PROCESS_ID=? AND ROUNDNUM BETWEEN ? AND ? ORDER BY ROUNDNUM",
            pid.toString(),
            fromRound,
            toRound);
    List<Object> actual = new ArrayList<>();
    for (int i = 0; i < fromRound; i++) {
      actual.add("");
    }
    actual.addAll(db);

    ValidateUtils.validate(expected, actual.toArray(), fromRound, toRound);
  }

  static void checkResult(ProcessId pid, int startRound) {
    checkFactoryResult(pid, factoryProfits, startRound, factoryProfits.length);
    checkShopResult(pid, shop1Profits, "Shop1", startRound, shop1Profits.length);
    checkShopResult(pid, shop2Profits, "Shop2", startRound, shop2Profits.length);
  }

  static void getLatestContextAndValidateResult(GameSessionOnServer session) {
    Market ctx = session.getLatestContext(Market.class);
    {
      Integer[] actual = new Integer[10];
      for (int i = 0; i < 10; i++) {
        Player p = ctx.getPlayer(ActorName.of("Factory"));
        Serializable v = p.getValue(Symbol.parse("profit"), i);
        actual[i] = Integer.valueOf(v.toString());
      }
      ValidateUtils.validate(factoryProfits, actual, 0, 10);
    }
    {
      Integer[] actual = new Integer[10];
      for (int i = 0; i < 10; i++) {
        Player p = ctx.getPlayer(ActorName.of("Shop1"));
        Serializable v = p.getValue(Symbol.parse("profit"), i);
        actual[i] = Integer.valueOf(v.toString());
      }
      ValidateUtils.validate(shop1Profits, actual, 0, 10);
    }
    {
      Integer[] actual = new Integer[10];
      for (int i = 0; i < 10; i++) {
        Player p = ctx.getPlayer(ActorName.of("Shop2"));
        Serializable v = p.getValue(Symbol.parse("profit"), i);
        actual[i] = Integer.valueOf(v.toString());
      }
      ValidateUtils.validate(shop2Profits, actual, 0, 10);
    }
  }
}
