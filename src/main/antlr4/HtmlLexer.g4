lexer grammar HtmlLexer;

// =====================================
// 1. HTML Comments
// =====================================

COMMENT
    : '<!--' .*? '-->'
    ;

// =====================================
// 2. HTML5 DOCTYPE
// =====================================

DOCTYPE
    : '<!' [dD][oO][cC][tT][yY][pP][eE] .*? '>'
    ;

// =====================================
// 3. Opening Tag
// =====================================

TAG_OPEN
    : '<' -> pushMode(TAG_MODE)
    ;

// =====================================
// 4. Whitespace outside tags
// =====================================

WS
    : [ \t\r\n]+ -> skip
    ;

// =====================================
// 5. Text outside tags
// =====================================

TEXT
    : ~[<]+
    ;

// =====================================
// 6. Tag Mode
// =====================================

mode TAG_MODE;

TAG_CLOSE_OPEN
    : '/'
    ;

// Void tag names take precedence over generic NAME
VOID_TAG_NAME
    : [iI][nN][pP][uU][tT]
    | [bB][rR]
    | [iI][mM][gG]
    | [hH][rR]
    | [mM][eE][tT][aA]
    | [lL][iI][nK]
    | [aA][rR][eE][aA]
    | [bB][aA][sS][eE]
    | [cC][oO][lL]
    | [eE][mM][bB][eE][dD]
    | [sS][oO][uU][rR][cC][eE]
    | [tT][rR][aA][cC][kK]
    | [wW][bB][rR]
    ;

NAME
    : [a-zA-Z] [a-zA-Z0-9:_-]*
    ;

TAG_WS
    : [ \t\r\n]+ -> skip
    ;

EQUALS
    : '=' -> pushMode(ATTR_VALUE_MODE)
    ;

SELF_CLOSE
    : '/>' -> popMode
    ;

TAG_CLOSE
    : '>' -> popMode
    ;

// =====================================
// 7. Attribute Value Mode
// =====================================

mode ATTR_VALUE_MODE;

DOUBLE_QUOTED_STRING
    : '"' ~["]* '"' -> popMode
    ;

SINGLE_QUOTED_STRING
    : '\'' ~[']* '\'' -> popMode
    ;

UNQUOTED_VALUE
    : ~[ \t\r\n"'`=<>]+ -> popMode
    ;

ATTR_WS
    : [ \t\r\n]+ -> skip
    ;
