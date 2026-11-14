parser grammar ThymeleafParser;
options { tokenVocab = ThymeleafLexer; }

// =====================================================================
//  Entry points: the analyzer picks one according to the attribute name
// =====================================================================
exprAttr     : expr EOF ;                                   // th:text th:utext th:if th:unless th:value th:href th:src th:action th:object th:field th:classappend ...
eachAttr     : word (COMMA word)? COLON expr EOF ;          // th:each="p, stat : ${products}"
withAttr     : assignment (COMMA assignment)* EOF ;         // th:with / th:attr  ->  a=${x}, b=@{/y}
fragmentAttr : (fragmentExpr | fragmentSpec) EOF ;          // th:insert / th:replace / th:include

assignment   : word ASSIGN expr ;

// =====================================================================
//  Expressions (alternative order = precedence, highest first)
// =====================================================================
expr
    // ---- postfix ----
    : expr DOT word LPAREN args? RPAREN                  # MethodCall
    | expr SAFE_NAV word LPAREN args? RPAREN             # SafeMethodCall
    | expr SAFE_NAV word                                 # SafeMemberAccess
    | expr DOT word                                      # MemberAccess
    | expr LBRACK expr RBRACK                            # IndexAccess
    // ---- unary ----
    | op=(MINUS | NOT_OP | NOT) expr                     # UnaryExpr
    // ---- binary ----
    | expr op=(STAR | SLASH | PERCENT | DIV | MOD) expr  # MulDivExpr
    | expr op=(PLUS | MINUS) expr                        # AddSubExpr
    | expr op=(GT | LT | GE | LE | GT_W | LT_W | GE_W | LE_W) expr  # RelationalExpr
    | expr op=(EQ | NE | EQ_W | NE_W) expr               # EqualityExpr
    | expr AND expr                                      # AndExpr
    | expr OR expr                                       # OrExpr
    // ---- conditional ----
    | <assoc=right> expr QUESTION expr COLON expr        # TernaryExpr
    | <assoc=right> expr QUESTION expr                   # IfThenExpr
    | <assoc=right> expr ELVIS expr                      # ElvisExpr
    // ---- Thymeleaf expression kinds ----
    | variableExpr                                       # VariableAlt
    | selectionExpr                                      # SelectionAlt
    | messageExpr                                        # MessageAlt
    | linkExpr                                           # LinkAlt
    | fragmentExpr                                       # FragmentAlt
    | literalSubst                                       # LiteralSubstAlt
    // ---- primaries ----
    | staticType                                         # StaticTypeAlt
    | NEW word (DOT word)* LPAREN args? RPAREN           # NewObject
    | HASH word                                          # UtilityObject    // #lists, #strings, #dates
    | AT word                                            # BeanRef          // @myBean
    | IDENTIFIER                                         # VarRef
    | STRING                                             # StringLit
    | INTEGER                                            # IntLit
    | DECIMAL                                            # DecimalLit
    | op=(TRUE | FALSE)                                  # BoolLit
    | NULL                                               # NullLit
    | UNDERSCORE                                         # NoOp
    | LPAREN expr RPAREN                                 # ParenExpr
    ;

args : expr (COMMA expr)* ;

// ----- ${...}  *{...} -----
variableExpr  : DOLLAR_LBRACE expr RBRACE ;
selectionExpr : STAR_LBRACE expr RBRACE ;

// ----- #{home.title(arg1, arg2)} -----
messageExpr   : HASH_LBRACE messageKey (LPAREN args? RPAREN)? RBRACE ;
messageKey    : word (DOT word)* ;

// ----- @{/product/{id}(id=${p.id}, q='x')} -----
linkExpr      : AT_LBRACE urlPath (LPAREN linkParams? RPAREN)? RBRACE ;
linkParams    : linkParam (COMMA linkParam)* ;
linkParam     : word ASSIGN expr ;
urlPath       : urlPart+ ;
urlPart
    : SLASH | DOT | MINUS | TILDE | COLON | QUESTION | ASSIGN | AMP | HASH
    | UNDERSCORE | INTEGER | DECIMAL
    | word
    | pathVar                      // {id}
    | variableExpr                 // @{${base}/x}
    ;
pathVar       : LBRACE word RBRACE ;

// ----- ~{layout :: header(arg)} -----
fragmentExpr  : TILDE_LBRACE fragmentSpec RBRACE ;
fragmentSpec
    : templateName (DCOLON word (LPAREN args? RPAREN)?)?
    | DCOLON word (LPAREN args? RPAREN)?
    ;
templateName  : STRING | word ((SLASH | DOT | MINUS) word)* ;

// ----- |Hello ${name}!| -----
literalSubst  : PIPE substPart* PIPE ;
substPart
    : variableExpr | selectionExpr | messageExpr | linkExpr
    | substText
    ;
substText     : ~(PIPE | DOLLAR_LBRACE | STAR_LBRACE | HASH_LBRACE | AT_LBRACE | TILDE_LBRACE) ;

// ----- T(java.lang.Math) -----
staticType    : STATIC_T LPAREN word (DOT word)* RPAREN ;

// ----- a name that may also be spelled like a keyword (p.new, /product/new, mod=1 ...) -----
word
    : IDENTIFIER
    | AND | OR | NOT | EQ_W | NE_W | GT_W | LT_W | GE_W | LE_W
    | DIV | MOD | TRUE | FALSE | NULL | NEW | STATIC_T
    ;