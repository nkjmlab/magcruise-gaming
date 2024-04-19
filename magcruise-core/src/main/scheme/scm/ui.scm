(define-namespace ui  "org.magcruise.gaming.ui")

;; ui.model
(define-alias Form <org.magcruise.gaming.ui.model.Form>)
(define-alias Input <org.magcruise.gaming.ui.model.input.Input>)
(define-alias TextInput <org.magcruise.gaming.ui.model.input.TextInput>)
(define-alias TextAreaInput <org.magcruise.gaming.ui.model.input.TextAreaInput>)
(define-alias NumberInput <org.magcruise.gaming.ui.model.input.NumberInput>)
(define-alias RadioInput <org.magcruise.gaming.ui.model.input.RadioInput>)
(define-alias CheckboxInput <org.magcruise.gaming.ui.model.input.CheckboxInput>)

(define-alias Min <org.magcruise.gaming.ui.model.attr.Min>)
(define-alias Max <org.magcruise.gaming.ui.model.attr.Max>)
(define-alias Required <org.magcruise.gaming.ui.model.attr.Required>)
(define-alias SimpleSubmit <org.magcruise.gaming.ui.model.attr.SimpleSubmit>)


(define (ui:form label ::string . inputs) ::Form
  (apply make Form label inputs))

(define (ui:text label ::string name ::symbol init-val ::string . attrs) ::Input
  (apply make TextInput label name init-val attrs))

(define (ui:textarea label ::string name ::symbol init-val ::string . attrs) ::Input
  (apply make TextAreaInput label name init-val attrs))

(define (ui:number label ::string name ::symbol init-val ::number . attrs) ::Input
  (apply make NumberInput label name init-val attrs))

(define (ui:number-blank label ::string name ::symbol . attrs) ::Input
  (apply make NumberInput label name #!null attrs))


(define (ui:radio label ::string name ::symbol init-val option-labels options . attrs) ::Input
  (apply make RadioInput label name init-val option-labels options attrs))

(define (ui:checkbox label ::string name ::symbol init-vals option-labels options . attrs) ::Input
  (apply make CheckboxInput label name init-vals option-labels options attrs))
