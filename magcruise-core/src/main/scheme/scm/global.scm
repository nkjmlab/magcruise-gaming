;; kawa lib
(require 'list-lib)

;; Java
(define-alias Class <java.lang.Class>)
(define-alias Object <java.lang.Object>)
(define-alias Serializable <java.io.Serializable>)

(define-alias String <java.lang.String>)
(define-alias Integer <java.lang.Integer>)
(define-alias Double <java.lang.Double>)
(define-alias Number <java.lang.Number>)

(define-alias List <java.util.List>)
(define-alias Map <java.util.Map>)
(define-alias ArrayList <java.util.ArrayList>)
(define-alias HashMap <java.util.HashMap>)
(define-alias LinkedHashMap <java.util.LinkedHashMap>)

(define-alias File <java.io.File>)

;;lang
(define-alias Parameters <org.magcruise.gaming.lang.Parameters>)
(define-alias Properties <org.magcruise.gaming.lang.Properties>)

;;util

;;model
(define-alias Game <org.magcruise.gaming.model.game.Game>)
(define-alias Context <org.magcruise.gaming.model.game.Context>)
(define-alias Player <org.magcruise.gaming.model.game.Player>)
(define-alias Players <org.magcruise.gaming.model.game.Players>)
(define-alias GameMessage <org.magcruise.gaming.model.game.message.GameMessage>)
(define-alias Form <org.magcruise.gaming.ui.model.Form>)
(define-alias GameEvent <org.magcruise.gaming.model.game.message.GameEvent>)
(define-alias ScenarioEvent <org.magcruise.gaming.model.game.message.ScenarioEvent>)

