package org.magcruise.gaming.examples.ultimatum.msg;

import org.magcruise.gaming.model.game.ActorName;
import org.magcruise.gaming.model.game.message.GameMessage;

@SuppressWarnings("serial")
public class FinalNote extends GameMessage {

  public final int proposition;

  public FinalNote(ActorName from, ActorName to, int proposition) {
    super(from, to);
    this.proposition = proposition;
  }

  @Override
  public Object[] getConstractorArgs() {
    return new Object[] {from, to, proposition};
  }

}
