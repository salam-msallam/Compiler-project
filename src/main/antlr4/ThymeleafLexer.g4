lexer grammar ThymeleafLexer;

// ============ Expression openers (each closes with RBRACE) ============
DOLLAR_LBRACE : '${' ;   // variable expression   ${product.name}
STAR_LBRACE   : '*{' ;   // selection expression  *{name}
AT_LBRACE     : '@{' ;   // link expression       @{/product/{id}(id=${p.id})}
HASH_LBRACE   : '#{' ;   // message expression    #{home.title}
TILDE_LBRACE  : '~{' ;   // fragment expression   ~{layout :: header}

// ============ Brackets / punctuation ============
LBRACE   : '{' ;
RBRACE   : '}' ;
LPAREN   : '(' ;
RPAREN   : ')' ;
LBRACK   : '[' ;
RBRACK   : ']' ;
COMMA    : ',' ;
SEMI     : ';' ;
DCOLON   : '::' ;        // fragment separator (before COLON: longest match)
COLON    : ':' ;
ELVIS    : '?:' ;        // default operator  a ?: b
SAFE_NAV : '?.' ;        // safe navigation   user?.name
QUESTION : '?' ;
DOT      : '.' ;
PIPE     : '|' ;         // literal substitution |Hello ${name}|
HASH     : '#' ;         // utility objects   #lists, #strings
AT       : '@' ;         // bean reference    ${@myBean.run()}
TILDE    : '~' ;         // server-relative link prefix
AMP      : '&' ;         // query string in URLs
UNDERSCORE : '_' ;       // no-operation token

// ============ Operators ============
EQ     : '==' ;
NE     : '!=' ;
GE     : '>=' ;
LE     : '<=' ;
GT     : '>' ;
LT     : '<' ;
ASSIGN : '=' ;
PLUS   : '+' ;
MINUS  : '-' ;
STAR   : '*' ;
SLASH  : '/' ;
PERCENT: '%' ;
NOT_OP : '!' ;

// ============ Keywords (MUST come before IDENTIFIER) ============
AND   : 'and' ;
OR    : 'or' ;
NOT   : 'not' ;
EQ_W  : 'eq' ;
NE_W  : 'neq' | 'ne' ;
GT_W  : 'gt' ;
LT_W  : 'lt' ;
GE_W  : 'ge' ;
LE_W  : 'le' ;
DIV   : 'div' ;
MOD   : 'mod' ;
TRUE  : 'true' ;
FALSE : 'false' ;
NULL  : 'null' ;
NEW : 'new' ;       // new java.util.Date()
STATIC_T : 'T' ;         // T(java.lang.Math)

// ============ Literals ============
STRING : '\'' ( ~['\\\r\n] | '\\' . )* '\''
    | '"'  ( ~["\\\r\n] | '\\' . )* '"';
DECIMAL : [0-9]+ '.' [0-9]+ ;
INTEGER : [0-9]+ ;

// ============ Identifier ============
IDENTIFIER : [a-zA-Z_] [a-zA-Z0-9_]* ;

// ============ Whitespace ============
WS : [ \t\r\n]+ -> skip ;

// Catch-all so unknown chars (e.g. Arabic/other text in plain literals) don't crash the lexer
OTHER : . ;