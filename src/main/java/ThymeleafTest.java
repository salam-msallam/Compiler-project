import org.antlr.v4.runtime.tree.ParseTree;
import java.nio.charset.StandardCharsets;
import org.antlr.v4.runtime.*;
import java.nio.file.*;
import java.util.*;

public class ThymeleafTest {
    /** Collects syntax errors instead of printing them to the console. */
    static class ErrorCollector extends BaseErrorListener {
        final List<String> errors = new ArrayList<>();

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
          int line, int charPositionInLine, String msg, RecognitionException e) {
            errors.add("col " + charPositionInLine + ": " + msg);
        }
    }

    /** Result of parsing one attribute value. */
    record Outcome(List<String> errors, String tree) { }

    static Outcome parse(String attrName, String value) {
        ErrorCollector collector = new ErrorCollector();

        ThymeleafLexer lexer = new ThymeleafLexer(CharStreams.fromString(value));
        lexer.removeErrorListeners();
        lexer.addErrorListener(collector);

        ThymeleafParser parser = new ThymeleafParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(collector);

        // choose the entry rule from the attribute name (same idea as ThymeleafAnalyzer)
        ParseTree tree = switch (attrName) {
            case "th:each" -> parser.eachAttr();
            case "th:with", "th:attr" -> parser.withAttr();
            case "th:replace", "th:insert", "th:include" -> parser.fragmentAttr();
            default -> parser.exprAttr();
        };

        return new Outcome(collector.errors, tree.toStringTree(parser));
    }

    public static void main(String[] args) throws Exception {
        Path file = Path.of(args.length > 0 ? args[0] : "src/test/resources/thymeleaf/thymeleaf-invalid.txt");
          //"src/test/resources/thymeleaf/thymeleaf-valid.txt");
          //"src/test/resources/thymeleaf/thymeleaf-expressions.txt");
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);

        boolean expectFail = false;
        int passed = 0, failed = 0, lineNo = 0;

        for(String raw : lines) {
            lineNo++;
            String line = raw.trim();
            if(line.isEmpty()) continue;

            if(line.startsWith("#")) {
                if(line.toLowerCase().contains("must fail")) expectFail = true;
                continue;
            }

            int bar = line.indexOf('|');   // first '|' only: the value itself may contain '|'
            if (bar < 0) {
                System.out.println("[SKIP] line " + lineNo + ": no '|' separator -> " + line);
                continue;
            }
            String attrName = line.substring(0, bar).trim();
            String value = line.substring(bar + 1).trim();

            Outcome out = parse(attrName, value);
            boolean hadErrors = !out.errors().isEmpty();
            boolean ok = (hadErrors == expectFail);

            if (ok) passed++; else failed++;

            System.out.println((ok ? "[PASS] " : "[FAIL] ") + "line " + lineNo + "  "
                    + attrName + "  |  " + value
                    + (expectFail ? "   (expected syntax error)" : ""));
            if (hadErrors) {
                out.errors().forEach(e -> System.out.println("         error: " + e));
            }
            if (!hadErrors || !ok) {
                System.out.println("         tree : " + out.tree());
            }
        }

        System.out.println();
        System.out.println("Passed: " + passed + "   Failed: " + failed);
        if (failed > 0) System.exit(1);
    }
}