(define (def:setup-game-builder builder ::GameBuilder)
  (builder:addDefContext (def:context org.magcruise.gaming.model.game.SimpleContext))

  (builder:addDefPlayers
   (def:player 'HumanPlayer1 'human org.magcruise.gaming.model.game.SimplePlayer)
   (def:player 'HumanPlayer2 'human org.magcruise.gaming.model.game.SimplePlayer)
   (def:player 'HumanPlayer3 'human org.magcruise.gaming.model.game.SimplePlayer)
   (def:player 'HumanPlayer4 'human org.magcruise.gaming.model.game.SimplePlayer))


  ;; r1: (H1 → H2)  → (H1, H3, H2) → (H4)
  ;; r2: (H1 → H2)  → (H1) → (H4)
  ;; r3: (H1 → H2)  → (H2) → (H4)
  (builder:addDefRounds
  (def:rounds 3
    (def:stage 'test
      (def:task 'HumanPlayer1 'vote)
      (def:task 'HumanPlayer2 'vote))
    (def:cond-stage 'vote
      (list 'c1 'c2)
      (def:parallel-stage 'h1_2
        (def:parallel-stage 'h1_3
          (def:task 'HumanPlayer1 'vote)
          (def:task 'HumanPlayer3 'vote))
        (def:task 'HumanPlayer2 'vote))
      (def:task 'HumanPlayer1 'vote)
      (def:task 'HumanPlayer2 'vote))
    (def:stage 'dist
      (def:task 'distribution)))))

(define (c1 ctx ::Context) (eqv? ctx:roundnum 0))
(define (c2 ctx ::Context) (eqv? ctx:roundnum 1))

(define items '("アイテムX" "アイテムY"))

(define (vote ctx ::Context self ::Player)
  (self:syncRequestToInput
    (ui:form
      "1000円のアイテムXと100円のアイテムYがあります．
      あなたの他に2人プレーヤがいて，他のプレーヤと異なるアイテムを選べば，そのアイテムを貰うことができます．
      もし，一緒のアイテムを選んだ場合は，何も貰うことは出来ません．どちらのアイテムを選びますか？"
      (ui:radio "アイテムの選択" 'item (car items) items items))
    (lambda (item)
      (self:set 'item item))))

(define (distribution ctx ::Context)
  (define tmp-player (ctx:players:get 'HumanPlayer4))
  (tmp-player:sync-request-to-input
    (ui:form
      "フロー確認のために表示しています．"
      (ui:radio "アイテムの選択" 'item (car items) items items))
    (lambda (item)
      #t))

  (define select-first  (filter (lambda (p ::Player) (eqv? (p:get 'item) (items 0))) (ctx:players:asLList)))
  (define select-second (filter (lambda (p ::Player) (eqv? (p:get 'item) (items 1))) (ctx:players:asLList)))

  (define minority
    (if (< (length select-first) (length select-second)) select-first select-second))
  (ctx:showMessageToAll
    (to-string
      (html:p "結果は以下です"
      (apply html:ul
        (map
          (lambda (p ::Player) (to-string p:name " → " (p:get 'item)))
          (ctx:players:values))))))
  (log:debug (ln) minority)
  (for-each
    (lambda (p ::Player)
      (ctx:showMessageToAll (to-string p:name "が" (p:get 'item) "を手に入れました．")))
    minority)
  (ctx:showMessageToAll (to-string "ラウンド" ctx:roundnum "が終了しました")))
