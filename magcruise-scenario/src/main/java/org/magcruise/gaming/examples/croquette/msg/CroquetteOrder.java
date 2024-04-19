package org.magcruise.gaming.examples.croquette.msg;

import org.magcruise.gaming.model.game.ActorName;
import org.magcruise.gaming.model.game.message.GameMessage;

@SuppressWarnings("serial")
public class CroquetteOrder extends GameMessage {

  public final int num;

  public CroquetteOrder(ActorName from, ActorName to, int num) {
    super(from, to);
    this.num = num;
  }

  @Override
  public Object[] getConstractorArgs() {
    return new Object[] {from, to, num};
  }

}
