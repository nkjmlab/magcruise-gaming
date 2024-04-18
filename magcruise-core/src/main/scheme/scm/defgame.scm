(define-namespace def "org.magcruise.gaming.def")
(define-alias ActorName org.magcruise.gaming.model.game.ActorName)

(define-alias DefRound <org.magcruise.gaming.model.def.scenario.round.DefRound>)
(define-alias DefStage <org.magcruise.gaming.model.def.scenario.stage.DefStage>)
(define-alias StageElement <org.magcruise.gaming.model.def.scenario.task.StageElement>)
(define-alias RoundElement <org.magcruise.gaming.model.def.scenario.round.RoundElement>)


(define-alias GameBuilder <org.magcruise.gaming.model.def.GameBuilder>)
(define-alias ContextBuilder <org.magcruise.gaming.model.def.actor.ContextBuilder>)
(define-alias GameScenarioBuilder <org.magcruise.gaming.model.def.scenario.GameScenarioBuilder>)

(define-alias GameSystemPropertiesBuilder <org.magcruise.gaming.model.def.sys.GameSystemPropertiesBuilder>)
(define-alias DefGameSystemProperty <org.magcruise.gaming.model.def.sys.DefGameSystemProperty>)

(define-alias BootstrapBuilder <org.magcruise.gaming.model.def.boot.BootstrapBuilder>)
(define-alias DefBootstrapProperty <org.magcruise.gaming.model.def.boot.DefBootstrapProperty>)



(define (def:context . args) ::org.magcruise.gaming.model.def.actor.DefContext
  (if (instance? (car args) keyword)
    (org.magcruise.gaming.model.def.actor.DefContextFactory:create args)
    (apply make org.magcruise.gaming.model.def.actor.DefContext args)))

(define (def:player . args)
  (if (instance? (car args) keyword)
    (org.magcruise.gaming.model.def.actor.DefPlayerFactory:create args)
    (apply make org.magcruise.gaming.model.def.actor.DefPlayer args)))


(define (def:assignment-request player-name ::symbol operator-id ::string)
  (make org.magcruise.gaming.model.def.actor.DefAssignmentRequest player-name operator-id))

(define (def:players player-names ::list playerType ::symbol clazz ::Class . args)
  (map
    (lambda (player-name ::symbol)
      (apply def:player player-name playerType clazz args))
    player-names))


(define (def:task-aux player ::symbol proc-name ::symbol) ::StageElement
  (make org.magcruise.gaming.model.def.scenario.task.DefPlayerTask player proc-name))

(define (def:context-task-aux proc-name ::symbol) ::StageElement
  (make org.magcruise.gaming.model.def.scenario.task.DefContextTask proc-name))

(define (def:players-task player-list ::list proc-name ::symbol) ::list
  (map (lambda (player ::symbol) (make org.magcruise.gaming.model.def.scenario.task.DefPlayerTask player proc-name)) player-list))

(define def:task
  (make-procedure
    method: (lambda (player ::symbol proc-name ::symbol) (def:task-aux player proc-name))
    method: (lambda (proc-name ::symbol) (def:context-task-aux proc-name))
    method: (lambda args (org.magcruise.gaming.model.def.scenario.task.DefTaskFactory:create args))))


(define (def:stage-aux defStage ::DefStage . stage-elements) ::RoundElement
  (for-each (lambda (e ::StageElement) (defStage:add e)) (flatten stage-elements))
  defStage)

(define (def:sequential-stage . stage-elements) ::RoundElement
  (if (instance? (car stage-elements) symbol)
      (def:stage-aux (make org.magcruise.gaming.model.def.scenario.stage.DefSequentialStage (car stage-elements)) (cdr stage-elements))
      (def:stage-aux (make org.magcruise.gaming.model.def.scenario.stage.DefSequentialStage) stage-elements)))

(define (def:stage . stage-elements) ::RoundElement
  (cond ((keyword? (car stage-elements))
          (org.magcruise.gaming.model.def.scenario.stage.DefStageFactory:create stage-elements))
         ((symbol? (car stage-elements))
          (def:stage-aux (make org.magcruise.gaming.model.def.scenario.stage.DefSequentialStage (car stage-elements)) (cdr stage-elements)))
         (else
          (def:stage-aux (make org.magcruise.gaming.model.def.scenario.stage.DefSequentialStage) stage-elements))))


(define (def:cond cond ::procedure) ::StageElement
  (make org.magcruise.gaming.model.def.scenario.stage.DefCond cond))

;; stage-elementsはcondがtrueの時とfalseの時の二つだけ．condはContextを引数に持つprocedure
(define (def:exor-stage name ::symbol cond ::symbol . stage-elements) ::RoundElement
  (def:stage-aux (make org.magcruise.gaming.model.def.scenario.stage.DefExorStage name cond) stage-elements))

(define (def:cond-stage name ::symbol conds ::list . stage-elements) ::RoundElement
  (def:stage-aux (make org.magcruise.gaming.model.def.scenario.stage.DefCondStage name conds) stage-elements))

(define (def:parallel-stage  . stage-elements) ::RoundElement
  (if (instance? (car stage-elements) symbol)
      (def:stage-aux (make org.magcruise.gaming.model.def.scenario.stage.DefParallelStage (car stage-elements)) (cdr stage-elements))
      (def:stage-aux (make org.magcruise.gaming.model.def.scenario.stage.DefParallelStage) stage-elements)))


(define (def:stages-aux func times ::integer name ::symbol . stage-elements) ::list
  (define stages '())
  (define (stages-aux times name stage-elements stages)
    (if (= times 0)
      stages
      (begin
        (set! stages (list (apply func name stage-elements) stages))
        (stages-aux (- times 1)  name stage-elements stages))))
  (stages-aux times name (flatten stage-elements) stages))

(define (def:restage name ::symbol) ::RoundElement
  (make org.magcruise.gaming.model.def.scenario.stage.DefRestage name))

(define (def:stages times ::integer name ::symbol . stage-elements) ::list
  (def:stages-aux def:stage times name stage-elements))

(define (def:parallel-stages times ::integer name ::symbol . stage-elements) ::list
  (def:stages-aux def:parallel-stage times name stage-elements))

(define (def:round . round-elements)
  (cond ((keyword? (car round-elements))
         (org.magcruise.gaming.model.def.scenario.round.DefRoundFactory:create round-elements))
        ((symbol? (car round-elements))
           (define defGameRound ::DefRound (make DefRound ((car round-elements)):toString))
           (for-each (lambda (e ::RoundElement) (defGameRound:add e)) (cdr round-elements))
               defGameRound)
        (else
           (define defGameRound ::DefRound (make DefRound ))
           (for-each (lambda (e ::RoundElement) (defGameRound:add e)) round-elements)
               defGameRound)))


(define (def:rounds times ::integer . round-elements)
  (define result ::ArrayList (make ArrayList))
  (define (def:rounds-aux times ::integer . round-elements)
    (if (eqv? times 0)
      result
      (begin
        (result:add (apply def:round round-elements))
        (apply def:rounds-aux (- times 1) round-elements))))
  (apply def:rounds-aux times round-elements))

(define (def:player-scenario init-scene ::symbol . scenes)
  (make org.magcruise.gaming.model.def.scenario.player_scenario.DefPlayerScenario init-scene scenes))

(define (def:scene name ::symbol . behaviors)
  (make org.magcruise.gaming.model.def.scenario.player_scenario.DefScene name behaviors))

(define (def:behavior  cue ::symbol action ::symbol scene-name ::symbol)
  (make org.magcruise.gaming.model.def.scenario.player_scenario.DefBehavior cue action scene-name))

(define (def:default-behavior action ::symbol scene-name ::symbol)
  (make org.magcruise.gaming.model.def.scenario.player_scenario.DefDefaultBehavior 'always-true action scene-name))

(define (def:scenario-task player ::symbol proc-name ::symbol event-class ::Class) ::StageElement
  (make org.magcruise.gaming.model.def.scenario.task.DefPlayerScenarioTask (ActorName:of player) proc-name event-class))

;; SystemPropertiy
(define (def:game-log-db dir ::path name ::string) ::DefGameSystemProperty
  (make org.magcruise.gaming.model.def.sys.DefGameLogDb (dir:toNPath) name))

(define (def:url url ::string ) ::DefGameSystemProperty
  (make org.magcruise.gaming.model.def.sys.DefUrl url))

(define (def:ui-service ui-url ::string) ::DefGameSystemProperty
  (make org.magcruise.gaming.model.def.sys.DefUIService ui-url))

(define (def:ui-service-and-register-session . args) ::DefGameSystemProperty
  (apply make org.magcruise.gaming.model.def.sys.DefUIServiceForRegisterSession args))

(define (def:game-interaction-service url ::string) ::DefGameSystemProperty
  (make org.magcruise.gaming.model.def.sys.DefGameInteractionService url))

(define (def:request-to-game-executor-publisher-service url ::string) ::DefGameSystemProperty
  (make org.magcruise.gaming.model.def.sys.DefRequestToGameExecutorPublisherService url))

;; Bootstrap
(define (def:classpath path ::Object) ::DefBootstrapProperty
  (make org.magcruise.gaming.model.def.boot.DefClasspath path))

(define (def:cp path ::Object) ::DefBootstrapProperty
  (def:classpath path))

(define (def:game-definition path ::Object) ::DefBootstrapProperty
  (make org.magcruise.gaming.model.def.boot.DefGameDefinition path))

(define (def:loader class-name ::string . resources) ::DefBootstrapProperty
  (apply make org.magcruise.gaming.model.def.boot.DefResourceLoader class-name resources))

(define (def:src scm-name ::string) ::DefBootstrapProperty
  (make org.magcruise.gaming.model.def.boot.DefResource scm-name))

(define (def:remoteDebug flag ::boolean) ::DefBootstrapProperty
  (make org.magcruise.gaming.model.def.boot.DefRemoteDebug flag))


(define (def:object-registration . args)
  (org.magcruise.gaming.model.def.scenario.DefObjectRegistrationFactory:create args))

(define (def:key-value-table . args)
  (org.magcruise.gaming.common.KeyValueTableFactory:create args))


(define (def:call . args)
  (org.magcruise.gaming.model.def.scenario.task.DefCallFactory:create args))
