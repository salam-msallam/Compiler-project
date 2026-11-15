
import java.nio.file.Path;
import java.util.Locale;
import org.antlr.v4.runtime.*;

public class CssLexerDemo {

    public static void main(String[] args) throws Exception {

        String file = args.length > 0
                ? args[0]
                : "src/test/resources/css/valid/basic.css";

        CharStream input = CharStreams.fromPath(Path.of(file));
        CssLexer lexer = new CssLexer(input);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        tokens.fill();

        for (Token token : tokens.getTokens()) {
            if (token.getType() == Token.EOF) {
                break;
            }

            // The parser needs whitespace/comments for descendant selectors,
            // but hiding them here keeps the token demo readable.
            if (token.getType() == CssLexer.WS
                    || token.getType() == CssLexer.COMMENT) {
                continue;
            }

            String type = lexer.getVocabulary()
                    .getSymbolicName(token.getType());

            System.out.printf(
                    Locale.ROOT,
                    "%-15s %-25s Line: %d%n",
                    type,
                    token.getText(),
                    token.getLine()
            );
        }
    }
}
