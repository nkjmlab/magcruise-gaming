package org.magcruise.gaming.examples.croquette.actor;

import org.magcruise.gaming.examples.croquette.msg.CroquetteDelivery;
import org.magcruise.gaming.examples.croquette.msg.CroquetteOrder;
import org.magcruise.gaming.model.game.HistoricalField;
import org.magcruise.gaming.model.game.Player;
import org.magcruise.gaming.model.game.PlayerParameter;
import org.magcruise.gaming.model.game.message.Alert;
import org.nkjmlab.util.java.concurrent.CallOnceTask;
import org.nkjmlab.util.java.lang.ResourceUtils;

@SuppressWarnings("serial")
public class Shop extends Player {

  @HistoricalField(name = "在庫個数")
  public volatile int stock;

  @HistoricalField(name = "発注個数")
  public volatile int numOfOrder;
  @HistoricalField(name = "納品個数")
  public volatile int delivery;
  @HistoricalField(name = "販売価格")
  public volatile int price;
  @HistoricalField(name = "在庫費")
  public volatile int inventoryCost;
  @HistoricalField(name = "仕入費")
  public volatile int materialCost;
  @HistoricalField(name = "売上個数")
  public volatile int sales;
  @HistoricalField(name = "売上高")
  public volatile int earnings;
  @HistoricalField(name = "利益")
  public volatile int profit;
  @HistoricalField(name = "来店者数")
  public volatile int demand;

  public Shop(PlayerParameter playerParameter) {
    super(playerParameter);
    this.stock = 600;
  }

  public void init(Market ctx) {
    String msg = (String) ctx.applyProcedure("shop:init-msg", ctx, this);
    syncRequestToConfirm(msg);
    showMessage(msg);
  }

  private CallOnceTask appendHeader = new CallOnceTask(() -> {
    appendHtml("#row-bottom",
        String.join(" ", ResourceUtils.readAllLines(getClass(), "header.html")));
  });
  private static final int FINAL_ROUND_NUM = 10;

  public void refresh(Market ctx) {
    appendHeader.run();

    String msg = (ctx.createMessage("shop:sale-msg", this, ctx.getOtherShop(this)) + "<br>"
        + ctx.createMessage("shop:order-msg", this));
    setHtml("#div-history", ctx.createMessage("shop:refresh-msg", ctx, this, ctx.getOtherShop(this))
        .replace("\"", "'"));
    syncRequestToConfirm(msg + "<br>" + ctx.createMessage("end-day-msg", ctx));
    showMessage(ctx.createMessage("start-day-msg", ctx));
    if (ctx.roundnum == FINAL_ROUND_NUM) {
      showAlertMessage(Alert.DANGER, "ゲームは終了です．お疲れ様でした．");
    }
    refresh();
  }

  public void refresh() {
    this.numOfOrder = 0;
    this.delivery = 0;
    this.price = 0;
    this.inventoryCost = 0;
    this.materialCost = 0;
    this.sales = 0;
    this.earnings = 0;
    this.profit = 0;
    this.demand = 0;
  }

  public void order(Market ctx) {
    // ctx.showMessageToAll("{}が{}日目の注文を入力しています．", name, ctx.getRoundnum());
    syncRequestToInput(ctx.createForm("shop:order-form", ctx, this), param -> {
      this.numOfOrder = param.getArgAsInt(0);
      showMessage(ctx.createMessage("shop:after-order-msg", ctx, this));
      sendMessage(new CroquetteOrder(name, toActorName("Factory"), this.numOfOrder));
      ctx.showMessage(ctx.getOthersNames(this), "{}が{}日目の注文を入力しました．", name, ctx.getRoundnum());
    }, e -> {
      showAlertMessage(Alert.DANGER, e.getMessage());
      order(ctx);
    });
  }

  public void price(Market ctx) {
    // ctx.showMessageToAll("{}が{}日目の販売価格を入力しています．", name, ctx.getRoundnum());
    syncRequestToInput(ctx.createForm("shop:price-form", ctx, this), param -> {
      this.price = param.getArgAsInt(0);
      showMessage(ctx.createMessage("shop:after-price-msg", ctx, this));
      ctx.showMessage(ctx.getOthersNames(this), "{}が{}日目の販売価格を入力しました．", name, ctx.getRoundnum());
    }, e -> {
      showAlertMessage(Alert.DANGER, e.getMessage());
      price(ctx);
    });

  }

  public void receiveDelivery(Market ctx) {
    this.delivery = takeMessage(CroquetteDelivery.class).num;
    this.stock += delivery;
    String msg = ctx.createMessage("shop:receive-delivery-msg", ctx, this).replaceAll("\"", "'");
    syncRequestToConfirm(msg);
    showMessage(msg);
  }

  public void closing(Market ctx) {
    this.demand = ctx.distributeDemand(this);

    this.sales = (stock >= demand) ? demand : stock; // 需要だけ売れる．上限は在庫量．

    this.stock -= sales;
    this.earnings = sales * price; // 収入は売った個数*単価
    this.inventoryCost = stock * 10; // 在庫費は売った後に計算．1個10円
    this.materialCost = delivery * CroquetteFactory.PRICE; // 材料費．冷凍コロッケの購入費は1個60円
    this.profit = earnings - (materialCost + inventoryCost); // 利益は収入から，材料費，在庫費を引いたもの．
  }
}
