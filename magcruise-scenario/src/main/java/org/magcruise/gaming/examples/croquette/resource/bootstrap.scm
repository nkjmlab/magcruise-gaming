(define (def:setup-bootstrap-builder builder ::BootstrapBuilder)
  (builder:addDefBootstrap
   (def:loader "org.magcruise.gaming.examples.croquette.resource.CroquetteGameResourceLoader"
        (def:src "game-definition.scm")
        (def:src "single-player-definition.scm"))))

