# Compiler Project - CSS Lexer and Parser

This stage implements a practical CSS subset with ANTLR 4. It supports
compound selectors, combinators, attribute and pseudo selectors, custom
properties, functions, common values and `!important`.

At-rules such as `@media` and `@keyframes` are intentionally outside the
current scope.

## Run the unified analyzer

`Main` selects the correct analyzer from the source file extension:

```powershell
mvn compile dependency:copy-dependencies "-DoutputDirectory=target/dependency"

java -cp "target/classes;target/dependency/*" Main src/test/resources/css/valid/basic.css
java -cp "target/classes;target/dependency/*" Main src/test/resources/html-thymeleaf/valid/products.html
java -cp "target/classes;target/dependency/*" Main src/test/resources/java/input.java
```

Supported extensions are `.css`, `.html`, `.htm`, and `.java`. Exit code `0`
means success, `1` means a lexer/parser error, and `2` means a usage, file, or
unsupported-extension error.

## Build

```powershell
mvn clean compile
```

## Run the token demo

```powershell
mvn org.codehaus.mojo:exec-maven-plugin:3.6.2:java "-Dexec.mainClass=CssLexerDemo" "-Dexec.args=src/test/resources/css/valid/basic.css"
```

## Run the parser demo

`CssParserDemo` returns `0` for valid CSS, `1` for CSS errors, and `2` for
invalid command-line input or file access errors. Run it in a separate Java
process when the exact exit code is needed:

```powershell
mvn compile dependency:copy-dependencies "-DoutputDirectory=target/dependency"
java -cp "target/classes;target/dependency/*" CssParserDemo src/test/resources/css/valid/basic.css
$LASTEXITCODE
```

Try the wider valid example and an invalid example:

```powershell
java -cp "target/classes;target/dependency/*" CssParserDemo src/test/resources/css/valid/core-features.css
java -cp "target/classes;target/dependency/*" CssParserDemo src/test/resources/css/invalid/missing-colon.css
```

Errors use the format `file:line:column: stage error: message`, where the stage
is either `lexer` or `parser`.

## Run integrated HTML and Thymeleaf analysis

The analyzer validates the HTML document first, then sends every `th:*`
attribute value to the matching Thymeleaf parser entry rule:

```powershell
mvn compile dependency:copy-dependencies "-DoutputDirectory=target/dependency"
java -cp "target/classes;target/dependency/*" HtmlThymeleafAnalyzer src/test/resources/html-thymeleaf/valid/products.html
$LASTEXITCODE
```

Try the integration error reporting with:

```powershell
java -cp "target/classes;target/dependency/*" HtmlThymeleafAnalyzer src/test/resources/html-thymeleaf/invalid/broken-expression.html
```

An Arabic implementation explanation and supervisor question guide is available
in [`docs/CSS_LEXER_PARSER_EXPLANATION_AR.md`](docs/CSS_LEXER_PARSER_EXPLANATION_AR.md).
