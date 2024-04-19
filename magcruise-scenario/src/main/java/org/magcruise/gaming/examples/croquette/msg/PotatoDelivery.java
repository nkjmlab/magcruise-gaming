package org.magcruise.gaming.examples.croquette.msg;

import org.magcruise.gaming.model.game.ActorName;
import org.magcruise.gaming.model.game.message.GameMessage;

@SuppressWarnings("serial")
public class PotatoDelivery extends GameMessage {

  public final int num;

  public PotatoDelivery(ActorName from, ActorName to, int orderOfPotato) {
    super(from, to);
    this.num = orderOfPotato;
  }

  @Override
  public Object[] getConstractorArgs() {
    return new Object[] {from, to, num};
  }

}
