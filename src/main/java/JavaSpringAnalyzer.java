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

/** Analyzes Java and supported Spring constructs as one compilation unit. */
public final class JavaSpringAnalyzer {

    private JavaSpringAnalyzer() {
    }

    public static AnalysisResult analyze(Path file) throws IOException {
        List<AnalysisError> errors = new ArrayList<>();
        CollectingErrorListener lexerErrors =
                new CollectingErrorListener(file, "Java/Spring lexer", errors);
        CollectingErrorListener parserErrors =
                new CollectingErrorListener(file, "Java/Spring parser", errors);

        CharStream input = CharStreams.fromPath(file, StandardCharsets.UTF_8);
        JavaSpringLexer lexer = new JavaSpringLexer(input);
        lexer.removeErrorListeners();
        lexer.addErrorListener(lexerErrors);

        JavaSpringParser parser = new JavaSpringParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(parserErrors);
        JavaSpringParser.CompilationUnitContext tree = parser.compilationUnit();

        return new AnalysisResult(tree, tree.toStringTree(parser), errors);
    }

    public record AnalysisResult(
            JavaSpringParser.CompilationUnitContext tree,
            String treeText,
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
