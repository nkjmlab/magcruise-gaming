(define-alias CroquetteDelivery org.magcruise.gaming.examples.croquette.msg.CroquetteDelivery)
(define-alias CroquetteOrder org.magcruise.gaming.examples.croquette.msg.CroquetteOrder)
(define-alias PotatoOrder org.magcruise.gaming.examples.croquette.msg.PotatoOrder)
(define-alias PotatoDelivery org.magcruise.gaming.examples.croquette.msg.PotatoDelivery)
(define-alias Shop org.magcruise.gaming.examples.croquette.actor.Shop)
(define-alias Farmer org.magcruise.gaming.examples.croquette.actor.Farmer)
(define-alias Factory org.magcruise.gaming.examples.croquette.actor.CroquetteFactory)
(define-alias Market org.magcruise.gaming.examples.croquette.actor.Market)

(define-namespace croquette "croquette")
(define-namespace shop "shop")
(define-namespace factory "factory")
(define-namespace farmer "farmer")

(define *shops* '(Shop1 Shop2))

(define (def:setup-game-builder builder ::GameBuilder)
  (builder:addDefContext
   (def:context name: 'Context class: Market))

  (setup-players builder)

  (builder:addDefRounds
    (def:round
      (def:stage type: 'parallel name: 'init
        (def:task actor: 'Factory action: 'init)
        (def:task actor: *shops* action: 'init))
      (def:stage type: 'sequential name: 'factory-receive-order
        (def:task actor: 'Factory action: 'receiveOrder))
      (def:stage type: 'parallel name: 'shop-order-and-pricing-factory-order
        (def:task actor: *shops* action: 'order)
        (def:task actor: *shops* action: 'price)
        (def:stage name: 'factory-order
          (def:task actor: 'Factory action: 'order)
          (def:task actor: 'Farmer action: 'receiveOrder)))
      (def:stage name: 'factory-receive-order
        (def:task  actor: 'Factory action: 'receiveOrder))
      (def:stage type: 'parallel name: 'closing
        (def:task actor: 'Factory action: 'closing)
        (def:task actor: *shops* action: 'closing))))

  (builder:addDefRounds
    (def:round
      (def:stage type: 'parallel name: 'refresh
        (def:task actor: (append *shops* (list 'Factory 'Farmer)) action: 'refresh))
      (def:stage name: 'farmer-delivery
        (def:task actor: 'Farmer action: 'delivery)
        (def:task actor: 'Factory action: 'receiveDelivery))
      (def:restage 'shop-order-and-pricing-factory-order)
      (def:restage 'factory-receive-order)
      (def:restage 'closing)))

  (builder:addDefRounds
    (def:round repeat: 6
      (def:restage 'refresh)
      (def:stage name: 'factory-delivery
        (def:task actor: 'Factory action: 'delivery)
        (def:task actor: *shops* action: 'receiveDelivery))
      (def:restage 'farmer-delivery)
      (def:restage 'shop-order-and-pricing-factory-order)
      (def:restage 'factory-receive-order)
      (def:restage 'closing)))

  (builder:addDefRounds
    (def:round
      (def:restage 'refresh)
      (def:restage 'factory-delivery)
      (def:restage 'farmer-delivery)
      (def:stage type: 'parallel name: 'shop-pricing
        (def:task actor: *shops* action: 'price))
      (def:restage 'closing)))

  (builder:addDefRounds
    (def:round
      (def:restage 'refresh)
      (def:restage 'factory-delivery)
      (def:restage 'shop-pricing)
      (def:restage 'closing))
    (def:round
      (def:restage 'refresh))))


(define (shop:sale-msg self ::Shop other ::Shop)
  (to-string (html:img width: 48 src: "https://i.gyazo.com/066c0903bb551da48a2d284af3cc61b1.png") "<br>"
   "コロッケを1個" self:price "円で販売しました．" "お店にはお客さんが" self:demand "人来て，"
             self:sales "個のコロッケが売れました．" "売り上げは" self:earnings "円です．" "<br>"
             "競合店はコロッケを1個" other:price "円で販売し，"  other:demand "人が来店したそうです．"))
(define (shop:order-msg self ::Shop)
  (to-string (html:img width: 64 src: "https://i.gyazo.com/ced22de43fcabec52f836c3ec614bb4a.png") "<br>"
   "冷凍コロッケを" self:numOfOrder "個発注しました．この冷凍コロッケは翌々日に納品予定です．"))

(define (shop:refresh-msg ctx ::Market self ::Shop other ::Shop)
  (<div> class: "alert"
      (to-string
        (<h3> (- ctx:roundnum 1) "日目のまとめ")
        (<br>)
        (<ul>
          (<li> "発注：" (shop:order-msg self))
          (<li> "販売：" (shop:sale-msg self other))
          (<li> "在庫：" self:delivery "個の冷凍コロッケが納品されました．"
                         self:sales "個のコロッケを売りました．"
                         "在庫は" self:stock "個です．")
          (<li> "収支：" "仕入費は" self:materialCost "円，"
                         "在庫費は" self:inventoryCost "円，"
                         "売上高は" self:earnings "円，"
                         "利益は" self:profit "円です．"))
        (<br>)
        (<h4> "在庫表")
        (self:tabulateHistory 'delivery 'sales 'stock 'order )
        (<h4> "販売表")
        (self:tabulateHistory 'price 'sales 'earnings 'demand)
        (<h4> "収支表")
        (self:tabulateHistory 'materialCost 'inventoryCost 'earnings 'profit))))



(define (factory:order-msg self ::Factory)
  (<div> class: "alert alert-success"
  (to-string (html:img width: 64 src: "https://i.gyazo.com/c0ef8fe2bfd1a453d3a473fddb9c0519.png") "<br>"
             self:orderOfPotato "個のじゃがいもを発注しました．翌日に納品されます．")))

(define (factory:ordered-msg self ::Factory)
  (<div> class: "alert alert-info"
   (to-string (html:img width: 64 src: "https://i.gyazo.com/1311a2a61ad40b976662bcf186c21c58.png") "<br>"
"各ショップから" self:orders "の注文を受けました．翌々日開始時点に納品する必要があります．")))

(define (factory:production-msg self ::Factory)
  (<div> class: "alert alert-info"
  (to-string (html:img width: 180 src: "https://i.gyazo.com/71045141e00ede32b2d9aced1c36545e.png") "<br>"
             self:deliveredPotato "個のじゃがいもが納品されました．支払額は" self:materialCost "円です．"
                         self:production "個の冷凍コロッケを作成しました．" self:machiningCost "円の生産費がかかりました．")))

(define (factory:refresh-msg ctx ::Market self ::Factory) ::String
    (<div> class: "alert"
      (to-string
       (<h3> (- ctx:roundnum 1) "日目のまとめ")
        (<ul>
         (<li> "発注：" (factory:order-msg self))
         (<li> "受注：" (factory:ordered-msg self))
         (<li> "生産：" (factory:production-msg self))
         (<li> "販売："
          (<div> class: "alert alert-info"
          (html:img width: 64 src: "https://i.gyazo.com/ced22de43fcabec52f836c3ec614bb4a.png") "<br>"
          self:demand "個の冷凍コロッケの納品が必要でした．" "冷凍コロッケを1個" self:PRICE "円で"
           self:sales "個納品しました．" "売り上げは" self:earnings "円です．"))
          (<li> "在庫：" "冷凍コロッケの在庫は" self:stock "個になりました．")
          (<li> "収支：" "仕入費は" self:materialCost "円，"
                         "在庫費は" self:inventoryCost "円，"
                         "売上高は" self:earnings "円，"
                         "利益は" self:profit "円です．"))
        (<br>)
        (<h4> "在庫表")
        (self:tabulateHistory 'deliveredPotato 'production 'stock 'orderOfPotato 'orders)
        (<h4> "販売表")
        (self:tabulateHistory 'price 'sales 'earnings 'demand)
        (<h4> "収支表")
        (self:tabulateHistory 'inventoryCost 'materialCost 'machiningCost 'earnings 'profit))))


(define (end-day-msg ctx ::Market) ::String
  (to-string  (<p> (- ctx:roundnum 1) "日目が終了しました．次の日に進みます．")))

(define (start-day-msg ctx ::Market) ::String
  (<div> class: "alert alert-warning" (to-string ctx:roundnum "日目のはじまりです．")))

(define (shop:init-msg ctx ::Market self ::Shop) ::String
    (<div> class: "alert alert-warning"
        (to-string (<h4> ctx:roundnum "日目がはじまりました．")
                   "初日(0日目)に発注した冷凍コロッケは翌々日(2日目)に納品され，その日から販売できます．<br>"
                   "現在の冷凍コロッケの在庫は" self:stock "個です．")))

(define (factory:init-msg ctx ::Market self ::Factory) ::String
    (<div> class: "alert alert-warning"
        (to-string (<h4> ctx:roundnum "日目がはじまりました．")
                   "初日(0日目)に受ける注文は翌々日(2日目)開始時に納品しなくてはなりません．<br>"
                   "初日(0日目)にじゃがいもを発注すると，翌日(1日目)に農家から納品されて冷凍コロッケを生産し，翌々日(2日目)にへショップへ納品できます．<br>"
                   "現在の冷凍コロッケの在庫は" self:stock "個です．")))


(define (shop:order-form ctx ::Market self ::Shop) ::Form
  (ui:form
    (to-string  (<h4> ctx:roundnum "日目の発注")
                self:name "さん，コロッケ工場へ発注する冷凍コロッケの個数(0個～1000個)を入力して下さい．発注したものは，翌々日の販売前に納品される予定です．" "<br>"
                (html:img width: 100 src: "https://i.gyazo.com/ced22de43fcabec52f836c3ec614bb4a.png"))
    (ui:number-blank "個数(冷凍コロッケ)" 'num-of-croquette (Min 0) (Max 1000) (Required))))

(define (shop:after-order-msg ctx ::Market self ::Shop) ::String
    (<div> class: "alert alert-success" (to-string "冷凍コロッケを" self:numOfOrder "個発注しました．翌々日に納品予定です．")))

(define (shop:price-form ctx ::Market self ::Shop) ::Form
  (ui:form
    (to-string (<h4> ctx:roundnum "日目の販売価格") self:name
               "さん，今日のコロッケの販売価格(50円～200円)を決定して下さい．" "<br>"
               (html:img width: 100 src: "https://i.gyazo.com/066c0903bb551da48a2d284af3cc61b1.png"))
    (ui:number-blank "販売価格(コロッケ)" 'price (Min 50) (Max 200) (Required))))

(define (shop:after-price-msg ctx ::Market self ::Shop) ::String
    (<div> class: "alert alert-success" (to-string "コロッケ1個の販売価格を" self:price "円に決めました．")))

(define (shop:receive-delivery-msg ctx ::Market self ::Shop) ::String
  (<div> class: "alert alert-info"
   (to-string (html:img width: 64 src: "https://i.gyazo.com/ced22de43fcabec52f836c3ec614bb4a.png")
              "<br>" "冷凍コロッケが" self:delivery "個納品されました．在庫数は" self:stock "個になりました．")))


(define (factory:order-form ctx ::Market self ::Factory) ::Form
    (ui:form
      (to-string (<h4> ctx:roundnum "日目の発注") self:name
                 "さん，農場へ発注するじゃがいもの個数(0個～1000個)を入力して下さい．発注したものは，翌日に納品されます．" "<br>"
                 (html:img width: 100 src: "https://i.gyazo.com/c0ef8fe2bfd1a453d3a473fddb9c0519.png"))
      (ui:number-blank "個数(ジャガイモ)" 'potato (Min 0) (Max 1000)(Required))))

;;(define (factory:after-order-msg ctx ::Market self ::Factory) ::String
;;  (<div> class: "alert alert-success" (to-string "じゃがいもを" self:orderOfPotato "個発注しました．翌日に納品されます．")))

(define (factory:delivery-msg shop-name delivery) ::String
  (to-string shop-name "に冷凍コロッケ" delivery "個を納品しました．"))

(define (factory:after-delivery-msg msg self ::Factory) ::String
  (<div> class: "alert alert-info"
   (html:img width: 64 src: "https://i.gyazo.com/ced22de43fcabec52f836c3ec614bb4a.png") "<br>"
   msg "在庫は" self:stock "個になりました．"))


(define (factory:receive-delivery-msg num production stock) ::String
  (<div> class: "alert alert-info"
    (to-string
     (html:img width: 180 src: "https://i.gyazo.com/71045141e00ede32b2d9aced1c36545e.png")
     "<br>" "じゃがいも" num "個が納品され，" production "個の冷凍コロッケを生産しました．在庫は" stock "個になりました．")))

