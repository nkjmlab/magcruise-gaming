<script type="text/javascript" src="http://res.nkjmlab.org/wplab/gist-embed.js"></script>

MAGCruiseCore
=============
http://www.magcruise.org/jp/developer にも開発者向け情報を掲載しています．

# A. チュートリアル
## 1. はじめに
MAGCruiseでは，ゲームスクリプトを記述することで独自のゲームを作成し，実施することができます．

ゲームスクリプトは，二つのパートからなります．

1. ゲームシナリオの定義
2. シナリオから呼び出される関数定義

以下では，具体的なゲームとして，単純なマイノリティゲームを取りあげて説明します．

## 2. ゲームシナリオの定義

<code data-gist-id="9886620"></code>

* 1行目：def:game-scenario関数の中でシナリオを定義します．
* 2行目～4行目：プレーヤーの定義をします．名前とタイプ(ソフトウェアが操作するか人間が操作するか)を定めます．
* 6行目：コンテキストの定義をします．contex-round関数により，コンテキストのラウンドオブジェクトにdesisionsという変数が作られ空リストに束縛されます．
* 8行目：def:round関数によりラウンドを定義します．
* 9行目：def:stage関数によりステージを定義します．def:stage関数の内部のturnは上から順に実行されます．
* 10行目-12行目：def:turn関数により，ターン実行時に呼び出されるプレーヤ名と関数名を定義します．この例では，voteがHumanPlayer1，HumanPlayer2，HumanPlayer3を引数として与えられて実行されます．
* 13行目：def:filter関数により，実行のタイミングとフィルタとして呼び出される関数名を定義します．afterが指定されているfilterはstageの全ターンが終了した後に呼び出されます．
* 15行目：Schemeの通常の大域変数です．

## 2. シナリオから呼び出される関数の定義
17行目以降では，ゲームシナリオから呼び出される関数を定義しています．

* 17行目：ui:request-input関数により，操作者に入力を求めます．self:nameによりselfが指し示すPlayerオブジェクトのname属性の値を取得できます．
* 18行目～23行目：formを作成し，アイテムを選択させるラジオボタン式の入力欄を組み込みます．
* 24行目：操作者が入力をするとこの関数が呼び出されます．form内のinputの値が可変長引数として渡されます．今回は入力欄が一つなので，引数は一つです．
* 25行目：selfのラウンドオブジェクトのitem属性に値を設定します．
* 26行目：contextのラウンドオブジェクトのdecisions属性にプレーヤ名と選択したアイテムの組を格納します．
* 28行目以降：選択した人数が少ないアイテムを求め，そのアイテムを選んだプレーヤに与えます．

## 補足
```
<scinario> = {<player>|<context>|<round>}
<player> = <name> <player-type>
<context> = <key> <val>
<context-round> =  <key> <val>
<rounds> = <repeat-times> {<stage>}
<round> = {<stage>|<stages>}
<event-driven-stage> = {<filter>|<event-driven-turn>}
<stage> = {<filter>|<turn>}
<filter> = <when> <proc-name>
<turn> = <agent-name> <proc-name>
<event-driven-turn> = <agent-name> <scenario-name>
<when> = "before"|"after"
<agent-name> = <text>
<proc-name> = <text>
```

---------------------------------------------------------------------------------
# B. リファレンス
<hr>
## シナリオ定義関数
|関数の概要|
|:---------|
|def:player<br>(def:player player-name ::symbol playerType ::symbol) ::#!void <br> プレイヤーの定義をします．|
|(def:context key ::symbol val) ::#!void <br> contextに属性を定義します．|
|(def:turn player ::symbol proc-name ::symbol) ::StageElement <br> 一人のプレーヤのターンの呼び出し関数を定義します．|
|(def:turn-players player-list ::list proc-name ::symbol) ::StageElement <br> 複数のプレーヤのターンの呼び出し関数を定義します．|
|(def:turn-all-players proc-name ::symbol) ::StageElement <br> 全てのプレーヤのターンの呼び出し関数を定義します．|
|(def:stage name ::string . stage-elements) ::RoundElement <br> ステージを定義します．ステージ内のターンは定義された順に実行されます．stage-elementsはStageElement型の可変長引数です．|
|(def:stages times ::integer name ::string . stage-elements) ::RoundElement <br> timesの回数だけ同じ内容のステージを定義します．|
|(def:concurrent-stage name ::string . elements) ::RoundElement <br> 並行実行ターンを持つステージを定義します．ステージ内のターンは出来るだけ並行して実行されます．stage-elementsはStageElement型の可変長引数です．|
|(def:concurrent-stages times ::integer name ::string . stage-elements) ::RoundElement <br> 並行実行ターンを持つステージをtimesの回数だけ定義します．|
|(def:round . round-elements) ::#!void <br> ラウンドを定義します．round-elementsは，RoundElement型の要素の可変長引数です．|
|(def:rounds times ::integer . round-elements) <br> timesの回数だけ同じ内容のラウンドを定義します．|
<hr>
## インタラクション関数
|関数の概要|
|:---------|
|(ui:show-message	playerName ::symbol . msgs) ::#!void <br> 操作者にメッセージを表示します．|
|(ui:request-input context ::Context self ::Player form ::Form callback ::procedure) ::#!void <br> 操作者に入力を求めます．|
|(ui:form label ::string . inputs) ::Form <br> 表示用のlabelをつけたフォームを定義します．inputsはInputの可変長引数です．|
|(ui:val-input label ::string name ::symbol init-val ::number) ::Input <br> 表示用のlabelと識別用の名前をつけた，数字用の入力欄を定義します．|
|(ui:text-input label ::string name ::symbol init-val ::string) ::Input <br> 表示用のlabelと識別用の名前をつけた，文字列用の入力欄を定義します．|
|(ui:radio-input label ::string name ::symbol init-val option-labels options) ::Input <br> 表示用のlabelと識別用の名前をつけた，選択欄を定義します．|
<hr>
### ui:show-message
* ```(ui:show-message	playerName ::symbol	. msgs)```
メッセージを表示します．
#### パラメータ
* playerName ::symbol
* . msgs ::Objectの可変長引数
#### 返り値
* なし
#### 呼び出し例
```scheme
(ui:show-message player:name "こんにちは" player:name "さん")
```
<hr>
### ui:request-input
* ```(ui:request-input context ::Context self ::Player form ::Form callback ::procedure)```

ヒューマンプレーヤに入力を求めます．操作者はformを埋めて送り返します．操作者から入力を受けとるとコールバック関数が呼び出されます．コールバック関数の引数にはフォーム内の入力値が順に渡されます．

#### パラメータ
* player-name ::symbol
* form ::form - formはui:formにより作成できる．
* callback ::procedure

#### 呼び出し例
```scheme
  (ui:request-input self:name
    (ui:form "どの魚を何匹取りますか？"
      (ui:val-input "魚の種類" 'species 'fishA '("タイ" "ヒラメ") '('fishA 'fishB))
      (ui:val-input "目標漁獲量" 'number 10))
    (lambda (species number)
      (ui:show-message self:name species "を" number "匹取りました")))
```
<hr>
## HTMLの生成関数
### 関数の概要
|関数|概要|
|:---------|:------|
|(html:h level . contents)||
|(html:p . contents)|contentsをpタグで囲んだ文字列を取得します．|
|(html:ul . lists)|listsが含む要素をliタグで囲み，全体をulタグで囲んだ文字列を取得します．|
|(html:img url)||
<hr>
## その他の関数
### 関数の概要
|関数|概要|
|:---------|:------|
|(ln) ::string|この関数が書かれたファイル名と行番号を示す文字列を取得します．|
|(log:debug . objs)|デバッグログを出力します．objsはObject型の可変長引数です．|
|(util:load-game scenario ::string)|シナリオディレクトリからゲームシナリオをロードします．|

### Context
#### メンバの概要
|メンバ|概要|
|:---------|:------|
|```roundnum ::integer```|現在のラウンド番号|
|```players ::Players```| プレーヤのリスト|
|```history ::History```|ラウンドオブジェクトの履歴|

#### メソッドの概要
|メソッド|概要|
|:---------|:------|
|```(get key ::symbol) ::Object```|登録されたkeyに対応する値を返します．|
|```(set key ::symbol val) ::void```|keyとそれに対応する値を設定します．|
|```(setAll . pairs) ::void```|pairsを設定します．pairは(cons key val)という形式を持ちます．|
|```(getAt roundnum ::integer key ::symbol)```|roundnumで指定したラウンドが終了した時のkeyの値を返します．|
|```(prev roundnum ::integer key ::symbol)```|現在のラウンドからprevだけ前のラウンドが終了した時のkeyの値を返します．|

<hr>
### Player
#### メンバの概要
|メンバ|概要|
|:---------|:------|
|```name ::symbol```|プレイヤー名|
|```history ::History```|ラウンドオブジェクトの履歴|
|```msgbox ::MessageBox```|メッセージボックス|

#### メソッドの概要
|メソッド|概要|
|:---------|:------|
|```(get key ::symbol) ::Object```|登録されたkeyに対応する値を返します．|
|```(set key ::symbol val) ::void```|keyとそれに対応する値を設定します．|
|```(setAll . pairs) ::void```|pairsを設定します．pairは(cons key val)という形式を持ちます．|
|```(getAt roundnum ::integer key ::symbol)```|roundnumで指定したラウンドが終了した時のkeyの値を返します．|
|```(prev roundnum ::integer key ::symbol)```|現在のラウンドからprevだけ前のラウンドが終了した時のkeyの値を返します．|
|```(makeMessage name) ::Message ```|nameが設定されたMessageオブジェクトを生成します．|
|```(makeMessage name . pairs) ::Message ```|nameとpairsが設定されたMessageオブジェクトを生成します．pairは(cons key val)という形式を持ちます．|

<hr>
### Message
#### メンバの概要
|メンバ|概要|
|:---------|:------|
|```name ::symbol```|名前|
|```from ::symbol```|送信元のPlayerの名前|
|```to ::symbol```|送信先のPlayerの名前|

#### メソッドの概要
|メソッド|概要|
|:---------|:------|
|```(get key ::symbol) ::Object```|登録されたkeyに対応する値を返します．|
|```(set key ::symbol val) ::void```|keyとそれに対応する値を設定します．|
|```(setAll . pairs) ::void```|pairsを設定します．pairは(cons key val)という形式を持ちます．|

<hr>
### Players
#### メソッドの概要
|シグネチャ|概要|
|:---------|:------|
|```(get player-name ::symbol) ::Player```|player-nameに対応するPlayerオブジェクトを取得します．|
|```(asLList) ::LList```|全Playerオブジェクトを含むリストをLList(Lisp形式のリスト)で取得します．|

----------------------------------------------------------------------------------------------------------------------------
# C. イベント駆動のゲーム開発
### ゲームシナリオの定義
```scheme
(define (def:game-scenario)
  (def:player 'Fisherman1 'human)
  (def:player 'Fisherman2 'human)

  (def:context 'ocean (make Ocean 100 1.2))

  (def:round 
    (def:event-driven-stage "fish-game" 
      (def:filter 'before 'start-fish-game)
      (def:event-driven-turn 'Fisherman1 'fisherman1-scenario)
      (def:event-driven-turn 'Fisherman2 'fisherman2-scenario)
      (def:filter 'after 'go-to-fishing)
      (def:filter 'after 'cleanup-and-recover))))
```
### プレイヤーシナリオの定義
ゲームを実行すると以下の順にゲームが進む．

1. filterによりstart-fish-gameがFisherman1に送られる．
2. Fisharman1のnegotiationメソッドが実行される．
3. Fisherman2にnegotiationのメッセージが送られる．
4. Fisharman2のnegotiationメソッドが実行される．
5. Fisherman1にfishingのメッセージが送られる．
6. Fisherman1のfishingのメソッドが実行される．Fisherman1がendする．
7. Fisherman2にfishingのメッセージが送られる．
8. Fisherman2のfishingのメソッドが実行される．Fisherman2がendする．

```scheme
(define (fisherman1-scenario)
  (define (negotiation context self msg)
    (let* ((form (fisher:input-comment context self))
           (msg (make Message 'negotiation)))
      (log:debug (self:getName) form)
      (msg:set 'text (form:get 'text))
      (manager:send 'Fisherman2 msg)))

  (define (fishing context self msg)
    (ui:show-message 'all msg)
    (fisher:decide-number-of-fish context self)
    (manager:send 'Fisherman2 (make Message 'fishing))
    (def:end-of-turn self))

  (def:agent-scenario 'first-scene
    (def:scene 'first-scene
      (def:behavior "negotiation"
        (lambda (context self msg) (msg:isNamed 'start-fish-game))
        (lambda (context self msg) (negotiation context self msg))
        'first-scene)
      (def:behavior "fishing"
        (lambda (context self msg) (msg:isNamed 'fishing))
        (lambda (context self msg) (fishing context self msg))
      'first-scene))))
```

```scheme
(define (fisherman2-scenario)
  (define (negotiation context self msg)
    (let* ((form (fisher:input-comment context self))
           (msg (make Message 'fishing)))
      (log:debug (self:getName) form)
      (msg:set 'text (form:get 'text))
      (manager:send 'Fisherman1 msg)))

  (define (fishing context self msg)
    (ui:show-message 'all msg)
    (fisher:decide-number-of-fish context self)
    (def:end-of-turn self))

  (def:agent-scenario 'first-scene
    (def:scene 'first-scene
      (def:behavior "negotiation"
        (lambda (context self msg) (msg:isNamed 'negotiation))
        (lambda (context self msg) (negotiation context self msg))
        'first-scene)
      (def:behavior "fishing"
        (lambda (context self msg) (msg:isNamed 'fishing))
        (lambda (context self msg) (fishing context self msg))
      'first-scene))))

(define (start-fish-game context ::Context)
  (manager:send 'Fisherman1 (make Message 'start-fish-game)))
```
