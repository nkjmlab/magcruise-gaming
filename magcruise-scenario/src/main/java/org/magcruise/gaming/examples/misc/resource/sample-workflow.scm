(define-alias SimpleContext org.magcruise.gaming.model.game.SimpleContext)

(define (def:setup-game-builder builder ::GameBuilder)
  (builder:addDefContext (def:context org.magcruise.gaming.model.game.SimpleContext))

  (builder:addDefPlayers
   (def:player 'HumanPlayer1 'human org.magcruise.gaming.model.game.SimplePlayer)
   (def:player 'HumanPlayer2 'human org.magcruise.gaming.model.game.SimplePlayer)
   (def:player 'HumanPlayer3 'human org.magcruise.gaming.model.game.SimplePlayer))

  (builder:addDefRounds
   (def:rounds 2
    (def:stage 'test
      (def:task 'HumanPlayer1 'vote1))
    (def:exor-stage 'vote 'first-round?
      (def:parallel-stage 'h1_2
        (def:parallel-stage 'h1_3
          (def:task 'HumanPlayer1 'vote2)
          (def:task 'HumanPlayer2 'vote2))
        (def:task 'HumanPlayer3 'vote2))
      (def:stage 'h1_3
        (def:task 'HumanPlayer1 'vote3)
        (def:task 'HumanPlayer2 'vote3)))
    (def:stage 'dist
      (def:task 'distribution)))))

(define (first-round? ctx ::Context) (eqv? ctx:roundnum 0))

(define (vote1 ctx ::Context self ::Player)
  (self:showMessage 1))

(define (vote2 ctx ::Context self ::Player)
  (self:showMessage 2))

(define (vote3 ctx ::Context self ::Player)
  (self:showMessage 3))


(define (distribution ctx ::Context)
  (ctx:showMessageToAll "dist"))
