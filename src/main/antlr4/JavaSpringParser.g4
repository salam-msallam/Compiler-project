parser grammar JavaSpringParser;

// Integrate With Lexer
options {
    tokenVocab = JavaSpringLexer;
}

// ==========================================
// 1. (Compilation Unit)
// ==========================================
compilationUnit
    : packageDeclaration? importDeclaration* typeDeclaration* EOF
    ;

packageDeclaration
    : PACKAGE qualifiedName SEMI
    ;

importDeclaration
    : IMPORT STATIC? qualifiedName (DOT MUL)? SEMI
    ;

qualifiedName
    : IDENTIFIER (DOT IDENTIFIER)*
    ;

// ==========================================
// 2. (Classes & Interfaces)
// ==========================================
typeDeclaration
    : annotation* modifier* (classDeclaration | interfaceDeclaration)
    ;

classDeclaration
    : CLASS IDENTIFIER (EXTENDS typeType)? (IMPLEMENTS typeList)? classBody
    ;

interfaceDeclaration
    : INTERFACE IDENTIFIER (EXTENDS typeList)? classBody
    ;

classBody
    : LBRACE classBodyDeclaration* RBRACE
    ;

classBodyDeclaration
    : SEMI                                              # EmptyDecl
    | annotation* modifier* memberDeclaration           # MemberDecl
    ;

memberDeclaration
    : fieldDeclaration                                  # FieldMember
    | constructorDeclaration                            # ConstructorMember
    | methodDeclaration                                 # MethodMember
    ;

// ==========================================
// 3. (Annotations Spring Boot)
// ==========================================
modifier
    : PUBLIC
    | PRIVATE
    | PROTECTED
    | STATIC
    | FINAL
    | ABSTRACT
    ;

// Annotations Spring Boot
annotation
    : AT qualifiedName (LPAREN annotationElement? RPAREN)?
    ;

annotationElement
    : elementValuePairs
    | elementValue
    ;

elementValuePairs
    : elementValuePair (COMMA elementValuePair)*
    ;

elementValuePair
    : IDENTIFIER ASSIGN elementValue
    ;

elementValue
    : expression
    ;

// ==========================================
// 4. (Fields, Constructors & Methods)
// ==========================================
fieldDeclaration
    : typeType variableDeclarators SEMI
    ;

variableDeclarators
    : variableDeclarator (COMMA variableDeclarator)*
    ;

variableDeclarator
    : IDENTIFIER (ASSIGN expression)?
    ;

constructorDeclaration
    : IDENTIFIER LPAREN formalParameterList? RPAREN block
    ;

methodDeclaration
    : (typeType | VOID) IDENTIFIER LPAREN formalParameterList? RPAREN (THROWS qualifiedNameList)? (block | SEMI)
    ;

formalParameterList
    : formalParameter (COMMA formalParameter)*
    ;

formalParameter
    : annotation* FINAL? typeType IDENTIFIER
    ;

qualifiedNameList
    : qualifiedName (COMMA qualifiedName)*
    ;

// ==========================================
// 5. Generics (Types)
// ==========================================
typeType
    : (primitiveType | classOrInterfaceType) (LBRACK RBRACK)*
    ;

primitiveType
    : INT
    | DOUBLE
    | FLOAT
    | LONG
    | BOOLEAN
    | CHAR
    | STRING_TYPE
    ;

classOrInterfaceType
    : IDENTIFIER typeArguments? (DOT IDENTIFIER typeArguments?)*
    ;

typeArguments
    : LT typeList? GT
    ;

typeList
    : typeType (COMMA typeType)*
    ;

// ==========================================
// 6. (Blocks & Statements)
// ==========================================
block
    : LBRACE blockStatement* RBRACE
    ;

blockStatement
    : localVariableDeclaration SEMI                     # LocalVarStmt
    | statement                                         # GeneralStmt
    ;

localVariableDeclaration
    : FINAL? typeType variableDeclarators
    ;

statement
    : block                                                                         # BlockStmt
    | IF LPAREN expression RPAREN statement (ELSE statement)?                       # IfStmt
    | FOR LPAREN forControl RPAREN statement                                        # ForStmt
    | WHILE LPAREN expression RPAREN statement                                      # WhileStmt
    | TRY block (CATCH LPAREN formalParameter RPAREN block)+                        # TryCatchStmt
    | RETURN expression? SEMI                                                       # ReturnStmt
    | THROW expression SEMI                                                         # ThrowStmt
    | expression SEMI                                                               # ExprStmt
    | SEMI                                                                          # EmptyStmt
    ;

forControl
    : forEnhancedControl                                                            # EnhancedForCtrl
    | forInit? SEMI expression? SEMI expressionList?                                # TraditionalForCtrl
    ;

// for-each: for (Product p : products)
forEnhancedControl
    : typeType IDENTIFIER COLON expression
    ;

forInit
    : localVariableDeclaration
    | expressionList
    ;

// ==========================================
// 7. (Expressions - Ordered)
// ==========================================
expressionList
    : expression (COMMA expression)*
    ;

expression
    : primary                                                                       # PrimaryExpr
    | expression DOT IDENTIFIER                                                     # FieldAccessExpr
    | expression DOT IDENTIFIER LPAREN expressionList? RPAREN                       # MethodCallExpr
    | IDENTIFIER LPAREN expressionList? RPAREN                                      # DirectMethodCallExpr
    | expression LBRACK expression RBRACK                                           # ArrayAccessExpr
    | NEW classOrInterfaceType LPAREN expressionList? RPAREN                        # ObjectCreationExpr
    | expression (INC | DEC)                                                        # PostfixExpr
    | (ADD | SUB | INC | DEC | BANG) expression                                     # PrefixExpr
    | expression (MUL | DIV | MOD) expression                                       # MultiplicativeExpr
    | expression (ADD | SUB) expression                                             # AdditiveExpr
    | expression (LE | GE | GT | LT) expression                                     # RelationalExpr
    | expression (EQ | NOTEQ) expression                                            # EqualityExpr
    | expression AND expression                                                     # LogicalAndExpr
    | expression OR expression                                                      # LogicalOrExpr
    | <assoc=right> expression (ASSIGN | ADD_ASSIGN | SUB_ASSIGN | MUL_ASSIGN | DIV_ASSIGN) expression # AssignmentExpr
    ;

primary
    : LPAREN expression RPAREN                                                      # ParenExpr
    | THIS                                                                          # ThisExpr
    | SUPER                                                                         # SuperExpr
    | literal                                                                       # LiteralExpr
    | IDENTIFIER                                                                    # IdentifierExpr
    ;

literal
    : DECIMAL_LITERAL                                                               # IntLiteral
    | FLOAT_LITERAL                                                                 # FloatLiteral
    | STRING_LITERAL                                                                # StringLiteral
    | CHAR_LITERAL                                                                  # CharLiteral
    | BOOL_LITERAL                                                                  # BoolLiteral
    | NULL_LITERAL                                                                  # NullLiteral
    ;