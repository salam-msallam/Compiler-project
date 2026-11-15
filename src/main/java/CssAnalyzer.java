import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

/** Analyzes CSS without handling command-line arguments or terminating Java. */
public final class CssAnalyzer {

    private CssAnalyzer() {
    }

    public static AnalysisResult analyze(Path file) throws IOException {
        List<AnalysisError> errors = new ArrayList<>();
        CollectingErrorListener lexerErrors =
                new CollectingErrorListener(file, "CSS lexer", errors);
        CollectingErrorListener parserErrors =
                new CollectingErrorListener(file, "CSS parser", errors);

        CharStream input = CharStreams.fromPath(file, StandardCharsets.UTF_8);
        CssLexer lexer = new CssLexer(input);
        lexer.removeErrorListeners();
        lexer.addErrorListener(lexerErrors);

        CssParser parser = new CssParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(parserErrors);
        CssParser.StylesheetContext tree = parser.stylesheet();

        return new AnalysisResult(tree, errors);
    }

    public record AnalysisResult(
            CssParser.StylesheetContext tree,
            List<AnalysisError> errors) {

        public AnalysisResult {
            errors = List.copyOf(errors);
        }

        public boolean success() {
            return errors.isEmpty();
        }
    }

    public record AnalysisError(
            Path file,
            int line,
            int column,
            String stage,
            String message) {

        public String format() {
            return String.format(
                    Locale.ROOT,
                    "%s:%d:%d: %s error: %s",
                    file,
                    line,
                    column,
                    stage,
                    message);
        }
    }

    private static final class CollectingErrorListener extends BaseErrorListener {

        private final Path file;
        private final String stage;
        private final List<AnalysisError> errors;

        private CollectingErrorListener(
                Path file,
                String stage,
                List<AnalysisError> errors) {
            this.file = file;
            this.stage = stage;
            this.errors = errors;
        }

        @Override
        public void syntaxError(
                Recognizer<?, ?> recognizer,
                Object offendingSymbol,
                int line,
                int charPositionInLine,
                String message,
                RecognitionException exception) {
            errors.add(new AnalysisError(
                    file,
                    line,
                    charPositionInLine + 1,
                    stage,
                    message));
        }
    }
}
