package org.magcruise.gaming.examples.ultimatum.actor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
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
import org.magcruise.gaming.ui.model.attr.Max;
import org.magcruise.gaming.ui.model.attr.Min;
import org.magcruise.gaming.ui.model.attr.Required;
import org.magcruise.gaming.ui.model.input.NumberInput;

@SuppressWarnings("serial")
public class FirstPlayer extends UltimatumPlayer {

  public List<Integer> defaultPropositions;
  public static final ActorName FIRST_PLAYER = ActorName.of("FirstPlayer");

  public FirstPlayer(PlayerParameter playerParameter) {
    this(playerParameter,
        Arrays.asList(null, null, null, null, null, null, null, null, null, null));
  }

  public FirstPlayer(PlayerParameter playerParameter, List<Integer> propositions) {
    super(playerParameter);
    this.defaultPropositions = new ArrayList<>(propositions);
  }

  @Override
  public SConstructor<? extends Player> toConstructor(ToExpressionStyle style) {
    return SConstructor.toConstructor(style, getClass(), getPlayerParameter(), defaultPropositions);
  }

  public void note(UltimatumGameContext ctx) {
    if (isAgent()) {
      noteOfAgent(ctx);
      return;
    }

    Element label = TagUtils.append(TagUtils.create("p"), TagUtils.ruby("第", "だい"), ctx.getRoundnum(),
        "ラウンドです．<br>", "あなたは", UltimatumGameContext.providedVal, "円を", TagUtils.ruby("受", "う"),
        "けとりました．こぐま君にいくらを", TagUtils.ruby("分", "わ"), "けますか？",
        TagUtils.create("div").attr("class", "pull-right").append(TagUtils.create("img")
            .attr("src", "https://i.gyazo.com/eddcf71e96234685cbaa412c463b9c7a.jpg").toString()));
    NumberInput input = new NumberInput(TagUtils.ruby("金額", "きんがく").toString(), "proposition",
        defaultPropositions.get(ctx.getRoundnum()), new Min(0),
        new Max(UltimatumGameContext.providedVal), new Required());

    syncRequestToInput(new Form(label.toString(), input), params -> {
      this.proposition = params.getArgAsInt(0);
      sendMessage(new FinalNote(name, SecondPlayer.SECOND_PLAYER, this.proposition));
      showAlertMessage(Alert.INFO, this.proposition + "円を分けると伝えました．" + "返事を待っています．");
    }, e -> {
      showAlertMessage(Alert.DANGER, e.getMessage());
      note(ctx);
    });
  }

  private final Random random = ThreadLocalRandom.current();

  private void noteOfAgent(UltimatumGameContext ctx) {
    this.proposition = random.nextInt(10) * (UltimatumGameContext.providedVal) / 10;
    sendMessage(new FinalNote(name, SecondPlayer.SECOND_PLAYER, this.proposition));
  }

}
