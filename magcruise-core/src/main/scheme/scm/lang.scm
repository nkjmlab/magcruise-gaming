(define (flatten ls)
  (cond ((null? ls) '())
        ((pair? ls) (append (flatten (car ls)) (flatten (cdr ls))))
        (else (list ls))))

;; extension for object
(define (to-string . vals) ::String
  (define (to-string-aux vals result)
    (if (null? vals)
        (result:toString)
        (let ((e (car vals)))
          (cond
              ((String? e) (to-string-aux (cdr vals) (string-append result e)))
              ((string? e) (to-string-aux (cdr vals) (string-append result e)))
              ((number? e) (to-string-aux (cdr vals) (string-append result (number->string e))))
              ((symbol? e) (to-string-aux (cdr vals) (string-append result (symbol->string e))))
              ((keyword? e) (to-string-aux (cdr vals) (string-append result (keyword->string e))))
              ((eqv? e #t) (to-string-aux (cdr vals) (string-append result "#t")))
              ((eqv? e #f) (to-string-aux (cdr vals) (string-append result "#f")))
              ((eq? e  #!null) (to-string-aux (cdr vals) (string-append result "#!null")))
              ((eq? e  #!void) (to-string-aux (cdr vals) (string-append result " #!void")))
              ((to-string-aux (cdr vals) (string-append result (e:toString))))))))
  (to-string-aux (flatten vals) ""))

(define (ln) (make org.magcruise.gaming.scm.LineNumber))


;; extension for log
(define log ::org.magcruise.gaming.scm.GameLogger (org.magcruise.gaming.scm.GameLogger:getLogger))
