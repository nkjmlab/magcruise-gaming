(define (def:setup-bootstrap-builder builder ::BootstrapBuilder)
    (builder:addDefBootstrap
     (def:loader "org.magcruise.gaming.examples.ultimatum.resource.UltimatumGameResourceLoader"
        (def:src "game-definition.scm")
        (def:src "two-players-definition.scm"))))
