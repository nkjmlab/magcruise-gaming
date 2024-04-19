package org.magcruise.gaming.executor;

import java.awt.image.BufferedImage;
import org.magcruise.gaming.model.game.ActorName;
import org.magcruise.gaming.ui.api.message.RequestToUI;
import org.magcruise.gaming.ui.model.message.MessageToUI;
import org.nkjmlab.util.java.util.Base64Utils;

@SuppressWarnings("serial")
public class RequestToShowImage extends RequestToUI {

  private String playerName;
  private int roundnum;
  private String encodedImage;
  private String formatName;

  public RequestToShowImage(long id, ActorName name, int roundnum, String encodedImage,
      String formatName) {
    super(id);
    this.playerName = name.toString();
    this.roundnum = roundnum;
    this.encodedImage = encodedImage;
    this.formatName = formatName;
  }

  public RequestToShowImage(ActorName name, int roundnum, String encodedImage, String formatName) {
    this(generateId(), name, roundnum, encodedImage, formatName);
  }

  public String getPlayerName() {
    return playerName;
  }

  public MessageToUI getMessage() {
    return new MessageToUI(encodeImageStringToImgElement(encodedImage, formatName));
  }

  public int getRoundnum() {
    return roundnum;
  }

  public BufferedImage getImage() {
    return Base64Utils.decodeToImage(encodedImage, formatName);
  }

  private static String encodeImageStringToImgElement(String encodedImage, String formatName) {
    return String.format("<img src='%s'/>", encodedImage);
  }

}
