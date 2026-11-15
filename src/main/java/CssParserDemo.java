import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Locale;

/** Command-line wrapper around the reusable CssAnalyzer. */
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

        try {
            CssAnalyzer.AnalysisResult result = CssAnalyzer.analyze(cssFile);
            if (!result.success()) {
                result.errors().forEach(error -> System.err.println(error.format()));
                System.err.printf(
                        Locale.ROOT,
                        "CSS analysis failed with %d error(s).%n",
                        result.errors().size());
                return CSS_ERROR;
            }

            System.out.printf("CSS analysis succeeded: %s%n", cssFile);
            return SUCCESS;
        } catch (IOException | SecurityException exception) {
            System.err.printf("Cannot read CSS file '%s': %s%n", cssFile, exception.getMessage());
            return USAGE_OR_IO_ERROR;
        }
    }
}
