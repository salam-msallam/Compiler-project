grammar Smoke;

start
    : 'hello' EOF
    ;

WS
    : [ \t\r\n]+ -> skip
    ;