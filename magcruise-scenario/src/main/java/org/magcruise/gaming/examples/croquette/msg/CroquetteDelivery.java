package org.magcruise.gaming.examples.croquette.msg;

import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.model.game.ActorName;
import org.magcruise.gaming.model.game.message.GameMessage;

@SuppressWarnings("serial")
public class CroquetteDelivery extends GameMessage {

  public final int num;

  public CroquetteDelivery(ActorName from, ActorName to, int orderOfCroquette) {
    super(from, to);
    this.num = orderOfCroquette;
  }

  @Override
  public SConstructor<? extends GameMessage> toConstructor(ToExpressionStyle style) {
    return SConstructor.toConstructor(style, this.getClass(), from, to, num);
  }

  @Override
  public Object[] getConstractorArgs() {
    return new Object[] {from, to, num};
  }

}
