(define-alias UltimatumGameContext org.magcruise.gaming.examples.ultimatum.actor.UltimatumGameContext)
(define-alias UltPlayer org.magcruise.gaming.examples.ultimatum.actor.UltimatumPlayer)
(define-alias FirstPlayer org.magcruise.gaming.examples.ultimatum.actor.FirstPlayer)
(define-alias SecondPlayer org.magcruise.gaming.examples.ultimatum.actor.SecondPlayer)
(define-alias FinalNote org.magcruise.gaming.examples.ultimatum.msg.FinalNote)

(define (def:setup-game-builder game-builder ::GameBuilder)
  (game-builder:addDefContext (def:context org.magcruise.gaming.examples.ultimatum.actor.UltimatumGameContext))

  (when (environment-bound? (interaction-environment) 'setup-players)
    (setup-players game-builder))

  (game-builder:addDefRounds
    (def:round
      (def:stage 'negotiation
        (def:task 'SecondPlayer 'beforeNegotiation)
        (def:task 'FirstPlayer 'note)
        (def:task 'SecondPlayer 'judge)
        (def:task 'paid)))

    (def:rounds 9
      (def:stage 'status
        (def:task 'FirstPlayer 'status)
        (def:task 'SecondPlayer 'status))
      (def:restage 'negotiation))

    (def:round
      (def:restage 'status))))

