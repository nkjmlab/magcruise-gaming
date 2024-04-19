package org.magcruise.gaming.examples.ultimatum.actor;

import org.magcruise.gaming.model.game.HistoricalField;
import org.magcruise.gaming.model.game.Player;
import org.magcruise.gaming.model.game.PlayerParameter;

@SuppressWarnings("serial")
public abstract class UltimatumPlayer extends Player {

  @HistoricalField(name = "手に入れたお金の<ruby><rb>合計</rb><rp>(</rp><rt>ごうけい</rt><rp>)</rp>")
  public volatile int account = 0;

  @HistoricalField(name = "伝えた<ruby><rb>金額</rb><rp>(</rp><rt>きんがく</rt><rp>)</rp>")
  public volatile int proposition;

  @HistoricalField(name = "<ruby><rb>返事</rb><rp>(</rp><rt>へんじ</rt><rp>)</rp>")
  public volatile Response yesOrNo;

  @HistoricalField(name = "<ruby><rb>今回</rb><rp>(</rp><rt>こんかい</rt><rp>)</rp>手に入れたお金")
  public volatile int acquisition;

  public UltimatumPlayer(PlayerParameter playerParameter) {
    super(playerParameter);
  }

  public void status(UltimatumGameContext ctx) {
    showMessage(tabulateHistory());
    proposition = 0;
    yesOrNo = null;
    acquisition = 0;
  }

  public void paid(int money) {
    this.acquisition = money;
    this.account += acquisition;
  }
}
