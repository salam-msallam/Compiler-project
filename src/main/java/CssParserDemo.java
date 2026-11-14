import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

public class CssParserDemo {

    private static final int SUCCESS = 0;
    private static final int CSS_ERROR = 1;
    private static final int USAGE_OR_IO_ERROR = 2;

    public static void main(String[] args) {
        System.exit(run(args));
    }

    static int run(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: CssParserDemo <path-to-css-file>");
            return USAGE_OR_IO_ERROR;
        }

        final Path cssFile;
        try {
            cssFile = Path.of(args[0]);
        } catch (InvalidPathException exception) {
            System.err.printf("Invalid file path: %s%n", exception.getInput());
            return USAGE_OR_IO_ERROR;
        }

        if (!Files.isRegularFile(cssFile)) {
            System.err.printf("CSS file does not exist: %s%n", cssFile);
            return USAGE_OR_IO_ERROR;
        }

        List<String> errors = new ArrayList<>();

        try {
            CharStream input = CharStreams.fromPath(cssFile, StandardCharsets.UTF_8);

            CssLexer lexer = new CssLexer(input);
            lexer.removeErrorListeners();
            lexer.addErrorListener(new CollectingErrorListener(cssFile, "lexer", errors));

            CommonTokenStream tokens = new CommonTokenStream(lexer);
            CssParser parser = new CssParser(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(new CollectingErrorListener(cssFile, "parser", errors));
            parser.stylesheet();
        } catch (IOException | SecurityException exception) {
            System.err.printf("Cannot read CSS file '%s': %s%n", cssFile, exception.getMessage());
            return USAGE_OR_IO_ERROR;
        }

        if (!errors.isEmpty()) {
            errors.forEach(System.err::println);
            System.err.printf("CSS analysis failed with %d error(s).%n", errors.size());
            return CSS_ERROR;
        }

        System.out.printf("CSS analysis succeeded: %s%n", cssFile);
        return SUCCESS;
    }

    private static final class CollectingErrorListener extends BaseErrorListener {

        private final Path file;
        private final String stage;
        private final List<String> errors;

        private CollectingErrorListener(Path file, String stage, List<String> errors) {
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
            // ANTLR columns are zero-based; report one-based columns to users.
            errors.add(String.format(
                    "%s:%d:%d: %s error: %s",
                    file,
                    line,
                    charPositionInLine + 1,
                    stage,
                    message));
        }
    }
}
