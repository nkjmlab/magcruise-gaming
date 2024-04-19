(define (def:setup-game-system-properties-builder builder ::GameSystemPropertiesBuilder)
    (builder:addProperties
      (org.magcruise.gaming.model.def.sys.DefUIService
        (string-append *BROKER_HOST* "/app/json/GameInteractionService"))
      (org.magcruise.gaming.model.def.sys.DefUIServiceForRegisterSession
        (string-append *WEBUI_HOST* "/app/json/WebUiService")
        *ADMIN_ID*
        (string-append *BROKER_HOST* "/app")
        (string-append "minority-" *ADMIN_ID* "-1p-session") "unused description")))

(define (def:setup-bootstrap-builder builder ::BootstrapBuilder)
  (builder:addProperties
    (org.magcruise.gaming.model.def.boot.DefResourceLoader  "org.magcruise.gaming.examples.minority.resource.MinorityGameResourceLoader"
      (org.magcruise.gaming.model.def.boot.DefResource[]
        (org.magcruise.gaming.model.def.boot.DefResource  "game-definition.scm")))))
