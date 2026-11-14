
package com.compiler;

import org.antlr.v4.runtime.*;

public class HtmlLexerTest {

    public static void main(String[] args) {

        String html = """
        <!DOCTYPE html>
        <!-- This is a comment -->
        <html>
            <body>
                <input disabled>
                <p class='description'>Hello &amp; Welcome</p>
                <br/>
            </body>
        </html>
        """;

        com.compiler.HtmlLexer lexer = new com.compiler.HtmlLexer(
                CharStreams.fromString(html)
        );

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        tokens.fill();

        for (Token token : tokens.getTokens()) {

            if (token.getType() == Token.EOF) {
                continue;
            }

            String tokenName = lexer.getVocabulary()
                    .getSymbolicName(token.getType());

            System.out.println(
                    tokenName + " : " + token.getText()
            );
        }
    }
}
