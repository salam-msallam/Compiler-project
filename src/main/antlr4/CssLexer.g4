lexer grammar CssLexer;

// Multi-character attribute operators must appear before their one-character
// parts so ANTLR emits a single token for operators such as "^=".
INCLUDES        : '~=';
DASH_MATCH      : '|=';
PREFIX_MATCH    : '^=';
SUFFIX_MATCH    : '$=';
SUBSTRING_MATCH : '*=';

LBRACE      : '{';
RBRACE      : '}';
LBRACKET    : '[';
RBRACKET    : ']';
COLON       : ':';
SEMICOLON   : ';';
COMMA       : ',';
DOT         : '.';
LPAREN      : '(';
RPAREN      : ')';
PERCENT     : '%';
STAR        : '*';
GREATER     : '>';
PLUS        : '+';
TILDE       : '~';
SLASH       : '/';
MINUS       : '-';
BANG        : '!';
EQUALS      : '=';

// An id selector and a hexadecimal color have the same lexical shape. The
// parser decides which meaning applies from the surrounding grammar rule.
HASH_VALUE
    : '#' [a-zA-Z0-9_-]+
    ;

STRING
    : '"' ('\\' . | ~["\\\r\n])* '"'
    | '\'' ('\\' . | ~['\\\r\n])* '\''
    ;

NUMBER
    : [0-9]+ ('.' [0-9]+)?
    | '.' [0-9]+
    ;

// Keeping IMPORTANT separate lets the parser accept only !important, rather
// than silently accepting arbitrary constructs such as !foo.
IMPORTANT
    : [iI] [mM] [pP] [oO] [rR] [tT] [aA] [nN] [tT]
    ;

// Covers ordinary identifiers, vendor-prefixed names and custom properties.
IDENT
    : [a-zA-Z_] [a-zA-Z0-9_-]*
    | '-' [a-zA-Z_] [a-zA-Z0-9_-]*
    | '--' [a-zA-Z0-9_-]+
    ;

// Whitespace and comments are intentionally not skipped. A parser cannot
// recognize a descendant selector (".card p") after that information is lost.
COMMENT
    : '/*' .*? '*/'
    ;

WS
    : [ \t\r\n]+
    ;
