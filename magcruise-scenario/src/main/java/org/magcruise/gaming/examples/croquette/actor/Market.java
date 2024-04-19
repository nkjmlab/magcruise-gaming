package org.magcruise.gaming.examples.croquette.actor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.magcruise.gaming.model.game.ActorName;
import org.magcruise.gaming.model.game.Context;
import org.magcruise.gaming.model.game.ContextParameter;
import org.magcruise.gaming.model.game.Player;

@SuppressWarnings("serial")
public class Market extends Context {

  public Market(ContextParameter contextParameter) {
    super(contextParameter);
  }

  public int distributeDemand(Shop shop) {
    Shop other = getOtherShop(shop);

    int demand = 0;

    if (shop.price < 105) {
      demand = 260;
    } else if (shop.price < 115) {
      demand = 250;
    } else if (shop.price < 130) {
      demand = 220;
    } else if (shop.price < 135) {
      demand = 200;
    } else if (shop.price < 155) {
      demand = 150;
    } else if (shop.price < 175) {
      demand = 120;
    } else if (shop.price <= 200) {
      demand = 70;
    } else {
      demand = 0;
    }

    if (shop.price < other.price) {
      demand += 100;// 安い金額を付けた方にボーナス
    } else if (shop.price == other.price) {
      demand += 50; // 同じ金額だったら等分
    }
    return demand;
  }

  public Shop getOtherShop(Shop shop) {
    if (shop.getName().compareToString("Shop1")) {
      return (Shop) getPlayer(toActorName("Shop2"));
    } else {
      return (Shop) getPlayer(toActorName("Shop1"));
    }
  }

  public List<ActorName> getOthersNames(Player player) {
    List<ActorName> r = new ArrayList<>(
        Arrays.asList(toActorName("Shop1"), toActorName("Shop2"), toActorName("Factory")));
    r.remove(player.getName());
    return r;
  }
}
