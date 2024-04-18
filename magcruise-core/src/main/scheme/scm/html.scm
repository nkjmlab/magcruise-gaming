(define-namespace htmlext "org.magcruise.gaming.htmlext")

;;; HTML作成ヘルパー
(define (htmlext:tag tagname . contents)
  (to-string "<" tagname ">" (apply to-string contents) "</" tagname ">"))

(define (htmlext:tag-attr-aux tagname . contents)
  (if (keyword? (car contents))
        (htmlext:tag-attr tagname
          (list (cons (car contents) (cadr contents))) (cddr contents))
        (htmlext:tag tagname contents)))

(define (htmlext:tag-attr tagname attrs . contents)
  (if (keyword? (car contents))
      (htmlext:tag-attr tagname
        (append attrs (list (cons (car contents) (cadr contents)))) (cddr contents))
      (to-string 
        "<" tagname
        (map
          (lambda (pair) (list " " (keyword->string (car pair)) "='" (cdr pair) "'"))
          attrs) ">"
        contents
        "</" tagname ">")))

(define (htmlext:blockquote . contents)
  (htmlext:tag "blockquote" contents))

(define (htmlext:p . contents)
  (htmlext:tag "p" contents))

(define (htmlext:pre . contents)
  (htmlext:tag "pre" contents))

(define (htmlext:div . contents)
  (apply htmlext:tag-attr-aux "div" contents))


(define (htmlext:h level . contents)
  (htmlext:tag (to-string "h" level) contents))

(define (htmlext:h1 . contents)
  (htmlext:h 1 contents))

(define (htmlext:h2 . contents)
  (htmlext:h 2 contents))

(define (htmlext:h3 . contents)
  (htmlext:h 3 contents))

(define (htmlext:h4 . contents)
  (htmlext:h 4 contents))

(define (htmlext:h4-attr attrs . contents)
  (htmlext:tag-attr "h4" attrs contents))

(define (htmlext:h5 . contents)
  (htmlext:h 5 contents))

(define (htmlext:h6 . contents)
  (htmlext:h 6 contents))

(define (htmlext:li . contents)
  (htmlext:tag "li" contents))

(define (htmlext:ul . lists)
  (htmlext:tag "ul"  lists))

(define (htmlext:ol . lists)
  (htmlext:tag "ol" lists))

(define (htmlext:img . contents)
  (apply htmlext:tag-attr-aux "img" contents))

(define (htmlext:strong . contents)
  (htmlext:tag "strong" contents))

(define (htmlext:tr tag contents)
  (htmlext:tag (map (lambda (data) (htmlext:tag tag data)) contents)))

(define (htmlext:br) "<br>")

(define (htmlext:script . contents)
  (htmlext:tag "script" contents))

(define (htmlext:rt content)
  (htmlext:tag "rt" content))

(define (htmlext:rb content)
  (htmlext:tag "rb" content))

(define (htmlext:rp content)
  (htmlext:tag "rp" content))

(define (htmlext:ruby rbc rtc)
  (htmlext:tag  "ruby" (htmlext:rb  rbc) (htmlext:rp  "(") (htmlext:rt rtc) (htmlext:rp  ")")))


(define-alias <rt> htmlext:rt)
(define-alias <rb> htmlext:rb)
(define-alias <rp> htmlext:rp)
(define-alias <ruby> htmlext:ruby)

(define-alias <p> htmlext:p)
(define-alias <pre> htmlext:pre)
(define-alias <div> htmlext:div)
(define-alias <div-class> htmlext:div-class)
(define-alias <div-attr> htmlext:div-attr)
(define-alias <h1> htmlext:h1)
(define-alias <h2> htmlext:h1)
(define-alias <h3> htmlext:h3)
(define-alias <h4> htmlext:h4)
(define-alias <h4-attr> htmlext:h4-attr)
(define-alias <h5> htmlext:h5)
(define-alias <h6> htmlext:h6)
(define-alias <li> htmlext:li)
(define-alias <ul> htmlext:ul)
(define-alias <ol> htmlext:ol)
(define-alias <img> htmlext:img)
(define-alias <strong> htmlext:strong)
(define-alias <tr> htmlext:tr)
(define-alias <blockquote> htmlext:blockquote)
(define-alias <br> htmlext:br)
(define-alias <script> htmlext:script)

(define (htmlext:ok-alert)
  (<script> "alert(OK?);"))
