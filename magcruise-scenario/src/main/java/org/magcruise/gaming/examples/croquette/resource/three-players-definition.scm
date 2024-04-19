(define (setup-players builder ::GameBuilder)
  (builder:addDefPlayers
  (def:player name: 'Farmer type: 'agent class: Farmer)
  (def:player name: 'Factory type: 'human class: Factory)
  (def:player name: 'Shop1 type: 'human class: Shop)
  (def:player name: 'Shop2 type: 'human class: Shop)))


(define (def:after-setup-game-builder builder ::GameBuilder)

  (builder:setDefKeyValueTables
    (def:key-value-table actor: 'Factory table-name: 'auto-input
     (list name: 'potato default: 100 round: (list 100 200 300 100 700 300 100 200 300 100 200)))

    (def:key-value-table actor: 'Factory table-name: 'expected
     (list name: 'profit round: (list 0 -6000 -70 5990 29990 -29940 -4000 20000 11000 38000 0)))

    (def:key-value-table actor: 'Shop1 table-name: 'auto-input
     (list name: 'price default: 150 round: (list 100 80 120 100 80 120 100 80 120 100 80))
     (list name: 'num-of-croquette default: 200 round: (list 200 300 400 200 300 400 200 300 400 200 300)))

    (def:key-value-table actor: 'Shop1 table-name: 'expected
     (list name: 'profit round: (list 22600 27200 7980 9600 3890 7980 12000 4400 14200 12900 0)))

    (def:key-value-table actor: 'Shop2 table-name: 'auto-input
     (list name: 'price default: 150 round: (list 80 100 200 150 80 100 200 150 80 100 200))
     (list name: 'num-of-croquette default: 200 round: (list 100 200 300 400 100 200 300 400 100 200 300)))

    (def:key-value-table actor: 'Shop2 table-name: 'expected
    (list name: 'profit round: (list 26400 24000 9240 12800 5940 5360 7700 9700 10600 5900 0)))))

