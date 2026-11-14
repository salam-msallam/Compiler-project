parser grammar JavaSpringParser;

// Integrate With Lexer
options {
    tokenVocab = JavaSpringLexer;             // <--- FIXED: Restored options block
}

// ==========================================
// 1. (Compilation Unit)
// ==========================================
compilationUnit
    : packageDeclaration? importDeclaration* typeDeclaration* EOF   // <--- FIXED: Restored * and removed -
    ;

packageDeclaration
    : PACKAGE qualifiedName SEMI
    ;

importDeclaration
    : IMPORT STATIC? qualifiedName (DOT MUL)? SEMI
    ;


qualifiedName
    : (IDENTIFIER | springAnnotationToken) (DOT (IDENTIFIER | springAnnotationToken))*
    ;

// ==========================================
// 2. (Classes & Interfaces)
// ==========================================
typeDeclaration
    : annotation* modifier* (classDeclaration | interfaceDeclaration) // <--- FIXED: Restored * and removed -
    ;

classDeclaration
    : CLASS IDENTIFIER (EXTENDS typeType)? (IMPLEMENTS typeList)? classBody
    ;

interfaceDeclaration
    : INTERFACE IDENTIFIER (EXTENDS typeList)? classBody
    ;

classBody
    : LBRACE classBodyDeclaration* RBRACE   // <--- FIXED: Restored * and removed -
    ;

classBodyDeclaration
    : SEMI                                              # EmptyDecl
    | annotation* modifier* memberDeclaration           # MemberDecl // <--- FIXED: Restored * and removed -
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
    : AT springAnnotationName (LPAREN annotationElement? RPAREN)?   # SpringAnno  // <--- ADDED: Spring-specific branch
    | AT qualifiedName (LPAREN annotationElement? RPAREN)?          # GeneralAnno // <--- ADDED: Label for other annotations
    ;

// <--- ADDED: Rule matching the 7 Spring Boot annotation tokens with individual AST labels
springAnnotationName
    : CONTROLLER        # ControllerAnno
    | GET_MAPPING       # GetMappingAnno
    | POST_MAPPING      # PostMappingAnno
    | REQUEST_MAPPING   # RequestMappingAnno
    | REQUEST_PARAM     # RequestParamAnno
    | MODEL_ATTRIBUTE   # ModelAttributeAnno
    | AUTOWIRED         # AutowiredAnno
    ;

// Unlabeled twin of springAnnotationName used exclusively inside qualifiedName.
// If qualifiedName used springAnnotationName directly, every import statement
// (e.g., "import org.springframework.stereotype.Controller;") would trigger
// visitControllerAnno / enterControllerAnno AST callbacks—tricking the compiler
// into treating an import line as an actual @Controller annotation.
springAnnotationToken
    : CONTROLLER
    | GET_MAPPING
    | POST_MAPPING
    | REQUEST_MAPPING
    | REQUEST_PARAM
    | MODEL_ATTRIBUTE
    | AUTOWIRED
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
    : (typeType | VOID) (IDENTIFIER | ADD_ATTRIBUTE) LPAREN formalParameterList? RPAREN (THROWS qualifiedNameList)? (block | SEMI)
    ;

formalParameterList
    : formalParameter (COMMA formalParameter)*
    ;

formalParameter
    : annotation* FINAL? typeType IDENTIFIER            // <--- FIXED: Restored * and removed -
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
    : LBRACE blockStatement* RBRACE                     // <--- FIXED: Restored * and removed -
    ;

blockStatement
    : localVariableDeclaration SEMI                     # LocalVarStmt
    | statement                                         # GeneralStmt
    ;

localVariableDeclaration
    : FINAL? typeType variableDeclarators
    ;

statement
    : block                                                                                     # BlockStmt
    | IF LPAREN expression RPAREN statement (ELSE statement)?                                   # IfStmt
    | FOR LPAREN forControl RPAREN statement                                                    # ForStmt
    | WHILE LPAREN expression RPAREN statement                                                  # WhileStmt
    | TRY block (CATCH LPAREN formalParameter RPAREN block)+                                    # TryCatchStmt
    | expression DOT ADD_ATTRIBUTE LPAREN expression COMMA expression RPAREN SEMI                # AddModelAttributeStmt // Any receiver, any key expression
    | RETURN STRING_LITERAL SEMI                                                                # ReturnViewStmt        // <--- ADDED: Captures return "template_name";
    | RETURN expression? SEMI                                                                   # ReturnStmt
    | THROW expression SEMI                                                                     # ThrowStmt
    | expression SEMI                                                                           # ExprStmt
    | SEMI                                                                                      # EmptyStmt
    ;

forControl
    : forEnhancedControl                                                            # EnhancedForCtrl
    | forInit? SEMI expression? SEMI expressionList?                                # TraditionalForCtrl
    ;

// لدعم حلقة for-each مثل: for (Product p : products)
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
    | expression DOT (IDENTIFIER | ADD_ATTRIBUTE) LPAREN expressionList? RPAREN     # MethodCallExpr
    | (IDENTIFIER | ADD_ATTRIBUTE) LPAREN expressionList? RPAREN                    # DirectMethodCallExpr
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