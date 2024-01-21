## symbol
Собирает символ из нескольких `значений`.<br>
Собирает строку из нескольких `значений` (во время компиляции).<br>
В случае использования в макросе - вычисляется __каждый раз заново__.

### Применение

1. `(symbol val0 valN)`<br>
`val0` `valN` - _значения_.

### Примеры

```pihta
(use-ctx pht
    (app-fn
        (println (symbol "Привет, " "Русь" "!"))))
```