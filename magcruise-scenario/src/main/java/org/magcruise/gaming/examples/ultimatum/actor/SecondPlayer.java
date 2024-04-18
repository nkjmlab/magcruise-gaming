package org.magcruise.gaming.examples.ultimatum.actor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.jsoup.nodes.Element;
import org.magcruise.gaming.examples.ultimatum.msg.FinalNote;
import org.magcruise.gaming.examples.util.TagUtils;
import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.model.game.ActorName;
import org.magcruise.gaming.model.game.Player;
import org.magcruise.gaming.model.game.PlayerParameter;
import org.magcruise.gaming.model.game.message.Alert;
import org.magcruise.gaming.ui.model.Form;
import org.magcruise.gaming.ui.model.attr.Required;
import org.magcruise.gaming.ui.model.input.Input;
import org.magcruise.gaming.ui.model.input.RadioInput;

@SuppressWarnings("serial")
public class SecondPlayer extends UltimatumPlayer {

  public List<Response> defaultYesOrNos;
  public static final ActorName SECOND_PLAYER = ActorName.of("SecondPlayer");

  public SecondPlayer(PlayerParameter playerParameter) {
    this(playerParameter, Arrays.asList(Response.YES, Response.YES, Response.YES, Response.YES,
        Response.YES, Response.YES, Response.YES, Response.YES, Response.YES, Response.YES));
  }

  public SecondPlayer(PlayerParameter playerParameter, List<Response> yesOrNos) {
    super(playerParameter);
    this.defaultYesOrNos = new ArrayList<>(yesOrNos);
  }

  @Override
  public SConstructor<? extends Player> toConstructor(ToExpressionStyle style) {
    return SConstructor.toConstructor(style, getClass(), getPlayerParameter(), defaultYesOrNos);
  }

  public void beforeNegotiation(UltimatumGameContext ctx) {
    showAlertMessage(Alert.INFO, "相手からの通牒を待っています．");
  }

  public Response getDefaultYesOrNo(int roundnum) {
    return defaultYesOrNos.get(roundnum);
  }

  public void judge(UltimatumGameContext ctx) {
    if (isAgent()) {
      judgeOfAgent(ctx);
      return;
    }

    FinalNote msg = takeMessage(FinalNote.class);
    Element label = TagUtils.append(TagUtils.create("p"), TagUtils.ruby("第", "だい"), ctx.getRoundnum(),
        "ラウンドです．<br>", "おおぐま君は", UltimatumGameContext.providedVal, "円を", TagUtils.ruby("受", "う"),
        "けとり，あなたに", msg.proposition, "円を", TagUtils.ruby("分", "わ"), "けると言いました．", "受けとりますか？",
        TagUtils.create("div").attr("class", "pull-right").append(TagUtils.create("img")
            .attr("src", "https://i.gyazo.com/d4cf1336d68f315fc9a88ba446f69488.jpg").toString()));
    Input input = new RadioInput(TagUtils.ruby("受", "う").append("けとる？").toString(), "yes-or-no",
        getDefaultYesOrNo(ctx.getRoundnum()).toString(),
        new String[] {Response.YES.toString(), Response.NO.toString()},
        new String[] {Response.YES.toString(), Response.NO.toString()}, new Required());

    syncRequestToInput(new Form(label.toString(), input), params -> {
      this.yesOrNo = Response.valueOf(params.getArgAsString(0).toUpperCase());
      showAlertMessage(Alert.INFO, this.yesOrNo + "と答えました．");
    }, e -> {
      showAlertMessage(Alert.DANGER, e.getMessage());
      judge(ctx);
    });
  }

  private void judgeOfAgent(UltimatumGameContext ctx) {
    this.yesOrNo = ThreadLocalRandom.current().nextInt(2) % 2 == 0 ? Response.YES : Response.NO;
  }
}
