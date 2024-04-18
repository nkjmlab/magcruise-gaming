(define (def:setup-game-system-properties-builder builder ::GameSystemPropertiesBuilder)
    (builder:addProperties
      (org.magcruise.gaming.model.def.sys.DefUIService
        (string-append *BROKER_HOST* "/app/json/GameInteractionService"))
      (org.magcruise.gaming.model.def.sys.DefUIServiceForRegisterSession
        (string-append *WEBUI_HOST* "/app/json/WebUiService")
        *ADMIN_ID*
        (string-append *BROKER_HOST* "/app")
        (string-append "ultimatum-" *ADMIN_ID* "-2p-session") "unused description")))

(define (def:setup-context-builder builder ::ContextBuilder)
  (builder:addProperties
    (org.magcruise.gaming.model.def.actor.DefAssignmentRequest
      (org.magcruise.gaming.model.game.ActorName  "FirstPlayer")  *FIRST_PLAYER_ID*)
    (org.magcruise.gaming.model.def.actor.DefAssignmentRequest
     (org.magcruise.gaming.model.game.ActorName  "SecondPlayer")  *SECOND_PLAYER_ID*)))

(define (def:setup-bootstrap-builder builder ::BootstrapBuilder)
  (builder:addProperties
    (org.magcruise.gaming.model.def.boot.DefResourceLoader
      "org.magcruise.gaming.examples.ultimatum.resource.UltimatumGameResourceLoader"
      (org.magcruise.gaming.model.def.boot.DefResource[]
        (org.magcruise.gaming.model.def.boot.DefResource  "game-definition.scm")
        (org.magcruise.gaming.model.def.boot.DefResource  "two-players-definition.scm")))))
