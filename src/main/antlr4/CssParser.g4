parser grammar CssParser;

options {
    tokenVocab = CssLexer;
}

stylesheet
    : spacing cssRule* EOF
    ;

cssRule
    : selectorList spacing LBRACE spacing declarationList? RBRACE spacing
    ;

selectorList
    : complexSelector (spacing COMMA spacing complexSelector)*
    ;

complexSelector
    : compoundSelector (combinator compoundSelector)*
    ;

// Required whitespace is a descendant combinator. Whitespace around an
// explicit combinator is optional.
combinator
    : spacing explicitCombinator spacing
    | requiredSpacing
    ;

explicitCombinator
    : GREATER
    | PLUS
    | TILDE
    ;

compoundSelector
    : typeSelector simpleSelector*
    | simpleSelector+
    ;

typeSelector
    : identifier
    | STAR
    ;

simpleSelector
    : classSelector
    | idSelector
    | attributeSelector
    | pseudoSelector
    ;

classSelector
    : DOT identifier
    ;

idSelector
    : HASH_VALUE
    ;

attributeSelector
    : LBRACKET spacing identifier
      (spacing attributeOperator spacing attributeValue)?
      spacing RBRACKET
    ;

attributeOperator
    : EQUALS
    | INCLUDES
    | DASH_MATCH
    | PREFIX_MATCH
    | SUFFIX_MATCH
    | SUBSTRING_MATCH
    ;

attributeValue
    : identifier
    | STRING
    | NUMBER
    | HASH_VALUE
    ;

// Supports :hover, ::before, :not(.disabled) and :nth-child(2n + 1).
pseudoSelector
    : COLON COLON? identifier
      (LPAREN spacing pseudoArgument spacing RPAREN)?
    ;

pseudoArgument
    : selectorList
    | value
    ;

// A semicolon is mandatory between declarations, but optional after the last.
declarationList
    : declaration (spacing SEMICOLON spacing declaration)*
      spacing SEMICOLON? spacing
    ;

declaration
    : identifier spacing COLON spacing value (spacing important)?
    ;

important
    : BANG spacing IMPORTANT
    ;

value
    : valueSequence (spacing COMMA spacing valueSequence)*
    ;

valueSequence
    : valueAtom (spacing valueAtom)*
    ;

valueAtom
    : functionCall
    | MINUS? NUMBER (identifier | PERCENT)?
    | HASH_VALUE
    | STRING
    | identifier
    | SLASH
    | MINUS
    | PLUS
    ;

functionCall
    : identifier LPAREN spacing value? spacing RPAREN
    ;

// IMPORTANT remains usable as an ordinary CSS name unless it follows '!'.
identifier
    : IDENT
    | IMPORTANT
    ;

spacing
    : (WS | COMMENT)*
    ;

requiredSpacing
    : (WS | COMMENT)+
    ;
