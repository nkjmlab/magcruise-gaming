(define (def:setup-game-builder builder ::GameBuilder)
  (builder:addDefContext (def:context org.magcruise.gaming.model.game.SimpleContext))
  (builder:addDefPlayers
    (def:player 'Tester 'human org.magcruise.gaming.model.game.SimplePlayer))
  (builder:addDefRounds
   (def:round
     (def:stage
          (def:task 'Tester 'gui-test)))))

(define (gui-test ctx ::Context self ::Player)
  (self:showMessage
    (<div>
      (to-string (<h3> "プレーヤにメッセージを送れます．メッセージにはHTMLタグが使えます．")
                 (<p> "画像の挿入も出来ます．")
                 (<img> "http://www.magcruise.org/jp/wp-content/themes/magcruise/img/logo.png"))))

   (self:syncRequestToInput
     (ui:form "プレーヤには複数の項目を入力させることが出来ます．<br>あなたの食べ物の好みについて教えて下さい．"
       (ui:text "名前" 'name "MAGCruise 太郎")
       (ui:number "年齢" 'age 20)
       (ui:radio "好きなタイカレーは?" 'thai_curry "green" (list "赤" "黄" "緑") (list "red" "yellow" "green"))
       (ui:checkbox "好きな果物は?" 'fruits (list "lemmon" "melon") (list "リンゴ" "レモン" "メロン") (list "apple" "lemmon" "melon")))
     (lambda (name ::string age ::number thai_curry ::string fruits ::List)
       (self:showMessage
         (<div-class> "alert alert-success"
           (to-string "あなたの入力内容は以下です．" "name=" name ", age=" age ", thai_curry=" thai_curry ",fruits=" fruits)))))

  (self:showMessage
    (<div-class> "alert alert-warning" "入力結果を受けとるまで，このメッセージが表示されないことに注意して下さい．")))


