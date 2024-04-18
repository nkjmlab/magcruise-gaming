(define-alias Response org.magcruise.gaming.examples.ultimatum.actor.Response)

(define  (setup-players builder ::GameBuilder)
  (builder:addDefPlayers
    (def:player 'FirstPlayer 'human FirstPlayer (list 10000 20000 30000 40000 50000 60000 10000 20000 30000 40000))
    (def:player 'SecondPlayer 'human SecondPlayer (list (Response "YES") (Response "YES") (Response "NO") (Response "YES") (Response "YES") (Response "NO") (Response "YES") (Response "YES") (Response "NO") (Response "YES")))))

