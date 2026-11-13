lexer grammar JavaSpringLexer;

// ==========================================
// Some Notes:
//   1. First Match
//   2. 
// ==========================================

// ==========================================
// 1. (Structure & Modifiers)
// ==========================================
PACKAGE     : 'package';
IMPORT      : 'import';
CLASS       : 'class';
INTERFACE   : 'interface';
EXTENDS     : 'extends';
IMPLEMENTS  : 'implements';

PUBLIC      : 'public';
PRIVATE     : 'private';
PROTECTED   : 'protected';
STATIC      : 'static';
FINAL       : 'final';
ABSTRACT    : 'abstract';

// ==========================================
// 2. (Data Types - Primitives & Common Classes)
// ==========================================
VOID        : 'void';
INT         : 'int';
DOUBLE      : 'double';
FLOAT       : 'float';
LONG        : 'long';
BOOLEAN     : 'boolean';
CHAR        : 'char';
STRING_TYPE : 'String';
// LIST        : 'List';
// MAP         : 'Map';

// ==========================================
// 3. (Control Flow & Statements)
// ==========================================
IF          : 'if';
ELSE        : 'else';
FOR         : 'for';
WHILE       : 'while';
RETURN      : 'return';
NEW         : 'new';
THIS        : 'this';
SUPER       : 'super';
TRY         : 'try';
CATCH       : 'catch';
THROW       : 'throw';
THROWS      : 'throws';

// ==========================================
// 4. (Literals)
// ==========================================
BOOL_LITERAL: 'true' | 'false';
NULL_LITERAL: 'null';

// Numbers
DECIMAL_LITERAL : [0-9]+;
FLOAT_LITERAL   : [0-9]+ '.' [0-9]+ [fFdD]?
                | '.' [0-9]+ [fFdD]?
                | [0-9]+ [fFdD];

// Strings
STRING_LITERAL  : '"' (~["\\\r\n] | EscapeSequence)* '"';
CHAR_LITERAL    : '\'' (~['\\\r\n] | EscapeSequence) '\'';

// ==========================================
// 5. (Separators & Delimiters)
// ==========================================
LPAREN      : '(';
RPAREN      : ')';
LBRACE      : '{';
RBRACE      : '}';
LBRACK      : '[';
RBRACK      : ']';
SEMI        : ';';
COMMA       : ',';
DOT         : '.';
COLON       : ':';
AT          : '@'; // (Spring Boot Annotations)

// ==========================================
// 6. (Operators)
// ==========================================
ASSIGN      : '=';
GT          : '>'; // (Generics)
LT          : '<';
BANG        : '!';
QUESTION    : '?';

EQ          : '==';
LE          : '<=';
GE          : '>=';
NOTEQ       : '!=';
AND         : '&&';
OR          : '||';
INC         : '++';
DEC         : '--';

ADD         : '+';
SUB         : '-';
MUL         : '*'; // (Import)
DIV         : '/';
MOD         : '%';

ADD_ASSIGN  : '+=';
SUB_ASSIGN  : '-=';
MUL_ASSIGN  : '*=';
DIV_ASSIGN  : '/=';

// ==========================================
// 7. (Identifiers)
// ==========================================
IDENTIFIER  : Letter (Letter | Digit)*;

// ==========================================
// 8. (Whitespace & Comments)
// ==========================================
WS          : [ \t\r\n\u000C]+ -> skip;
COMMENT     : '/*' .*? '*/'    -> skip;
LINE_COMMENT: '//' ~[\r\n]*    -> skip;

// ==========================================
// 9. (Fragments)
// ==========================================
fragment EscapeSequence
    : '\\' [btnfr"'\\]
    | '\\' ([0-3]? [0-7])? [0-7]
    | '\\' 'u'+ HexDigit HexDigit HexDigit HexDigit
    ;

fragment HexDigit
    : [0-9a-fA-F]
    ;

fragment Letter
    : [a-zA-Z$_]
    ;

fragment Digit
    : [0-9]
    ;