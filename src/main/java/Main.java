import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Locale;

/** Unified command-line entry point for all supported source file types. */
public final class Main {

    private static final int SUCCESS = 0;
    private static final int SYNTAX_ERROR = 1;
    private static final int USAGE_OR_IO_ERROR = 2;

    private Main() {
    }

    public static void main(String[] args) {
        System.exit(run(args));
    }

    static int run(String[] args) {
        if (args.length != 1) {
            printUsage();
            return USAGE_OR_IO_ERROR;
        }

        final Path file;
        try {
            file = Path.of(args[0]);
        } catch (InvalidPathException exception) {
            System.err.printf("Invalid file path: %s%n", exception.getInput());
            return USAGE_OR_IO_ERROR;
        }

        if (!Files.isRegularFile(file)) {
            System.err.printf("Source file does not exist: %s%n", file);
            return USAGE_OR_IO_ERROR;
        }

        String extension = extensionOf(file);

        try {
            return switch (extension) {
                case "css" -> analyzeCss(file);
                case "html", "htm" -> analyzeHtml(file);
                case "java" -> analyzeJava(file);
                default -> unsupportedExtension(file, extension);
            };
        } catch (IOException | SecurityException exception) {
            System.err.printf("Cannot read source file '%s': %s%n", file, exception.getMessage());
            return USAGE_OR_IO_ERROR;
        }
    }

    private static int analyzeCss(Path file) throws IOException {
        CssAnalyzer.AnalysisResult result = CssAnalyzer.analyze(file);
        if (!result.success()) {
            result.errors().forEach(error -> System.err.println(error.format()));
            printFailure("CSS", result.errors().size());
            return SYNTAX_ERROR;
        }

        printSuccess("CSS", file);
        return SUCCESS;
    }

    private static int analyzeHtml(Path file) throws IOException {
        HtmlThymeleafAnalyzer.AnalysisResult result = HtmlThymeleafAnalyzer.analyze(file);
        if (!result.success()) {
            result.errors().forEach(error -> System.err.println(error.format()));
            printFailure("HTML + Thymeleaf", result.errors().size());
            return SYNTAX_ERROR;
        }

        System.out.printf(
                Locale.ROOT,
                "HTML + Thymeleaf analysis succeeded: %s (%d Thymeleaf attribute(s))%n",
                file,
                result.thymeleafAttributeCount());
        return SUCCESS;
    }

    private static int analyzeJava(Path file) throws IOException {
        JavaSpringAnalyzer.AnalysisResult result = JavaSpringAnalyzer.analyze(file);
        if (!result.success()) {
            result.errors().forEach(error -> System.err.println(error.format()));
            printFailure("Java + Spring", result.errors().size());
            return SYNTAX_ERROR;
        }

        printSuccess("Java + Spring", file);
        return SUCCESS;
    }

    private static String extensionOf(Path file) {
        String name = file.getFileName().toString();
        int lastDot = name.lastIndexOf('.');
        if (lastDot < 0 || lastDot == name.length() - 1) {
            return "";
        }
        return name.substring(lastDot + 1).toLowerCase(Locale.ROOT);
    }

    private static int unsupportedExtension(Path file, String extension) {
        String displayExtension = extension.isEmpty() ? "<none>" : "." + extension;
        System.err.printf(
                "Unsupported file type %s for '%s'. Supported: .css, .html, .htm, .java%n",
                displayExtension,
                file);
        return USAGE_OR_IO_ERROR;
    }

    private static void printSuccess(String analyzer, Path file) {
        System.out.printf("%s analysis succeeded: %s%n", analyzer, file);
    }

    private static void printFailure(String analyzer, int errorCount) {
        System.err.printf(
                Locale.ROOT,
                "%s analysis failed with %d error(s).%n",
                analyzer,
                errorCount);
    }

    private static void printUsage() {
        System.err.println("Usage: Main <source-file>");
        System.err.println("Supported extensions: .css, .html, .htm, .java");
    }
}
