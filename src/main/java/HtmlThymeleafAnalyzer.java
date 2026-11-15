import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
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
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

/**
 * Runs the HTML parser first, then validates every th:* attribute value with
 * the dedicated Thymeleaf parser.
 */
public final class HtmlThymeleafAnalyzer {

    private static final int SUCCESS = 0;
    private static final int SYNTAX_ERROR = 1;
    private static final int USAGE_OR_IO_ERROR = 2;

    private HtmlThymeleafAnalyzer() {
    }

    /**
     * Analyzes one HTML file and returns both HTML and Thymeleaf errors.
     */
    public static AnalysisResult analyze(Path file) throws IOException {
        List<AnalysisError> errors = new ArrayList<>();

        CharStream input = CharStreams.fromPath(file, StandardCharsets.UTF_8);
        HtmlLexer htmlLexer = new HtmlLexer(input);
        htmlLexer.removeErrorListeners();
        htmlLexer.addErrorListener(new HtmlErrorListener(file, "HTML lexer", errors));

        HtmlParser htmlParser = new HtmlParser(new CommonTokenStream(htmlLexer));
        htmlParser.removeErrorListeners();
        htmlParser.addErrorListener(new HtmlErrorListener(file, "HTML parser", errors));
        HtmlParser.DocumentContext document = htmlParser.document();

        List<HtmlParser.AttributeContext> thymeleafAttributes = new ArrayList<>();
        ParseTreeWalker.DEFAULT.walk(new HtmlParserBaseListener() {
            @Override
            public void enterAttribute(HtmlParser.AttributeContext context) {
                String name = context.NAME().getText();
                if (name.toLowerCase(Locale.ROOT).startsWith("th:")) {
                    thymeleafAttributes.add(context);
                }
            }
        }, document);

        for (HtmlParser.AttributeContext attribute : thymeleafAttributes) {
            analyzeThymeleafAttribute(file, attribute, errors);
        }

        return new AnalysisResult(thymeleafAttributes.size(), errors);
    }

    private static void analyzeThymeleafAttribute(
            Path file,
            HtmlParser.AttributeContext attribute,
            List<AnalysisError> errors) {
        String attributeName = attribute.NAME().getText();

        if (attribute.attributeValue() == null) {
            Token nameToken = attribute.NAME().getSymbol();
            errors.add(new AnalysisError(
                    file,
                    nameToken.getLine(),
                    nameToken.getCharPositionInLine() + 1,
                    "Thymeleaf integration",
                    attributeName,
                    "attribute requires a value"));
            return;
        }

        Token valueToken = attribute.attributeValue().getStart();
        String rawValue = attribute.attributeValue().getText();
        boolean quoted = isQuoted(rawValue);
        String expression = quoted
                ? rawValue.substring(1, rawValue.length() - 1)
                : rawValue;

        ThymeleafErrorListener listener = new ThymeleafErrorListener(
                file,
                attributeName,
                valueToken.getLine(),
                valueToken.getCharPositionInLine(),
                quoted ? 1 : 0,
                errors);

        ThymeleafLexer lexer = new ThymeleafLexer(CharStreams.fromString(expression));
        lexer.removeErrorListeners();
        lexer.addErrorListener(listener);

        ThymeleafParser parser = new ThymeleafParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(listener);

        switch (attributeName.toLowerCase(Locale.ROOT)) {
            case "th:each" -> parser.eachAttr();
            case "th:with", "th:attr" -> parser.withAttr();
            case "th:replace", "th:insert", "th:include" -> parser.fragmentAttr();
            default -> parser.exprAttr();
        }
    }

    private static boolean isQuoted(String value) {
        if (value.length() < 2) {
            return false;
        }

        char first = value.charAt(0);
        char last = value.charAt(value.length() - 1);
        return (first == '"' && last == '"') || (first == '\'' && last == '\'');
    }

    public static void main(String[] args) {
        System.exit(run(args));
    }

    static int run(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: HtmlThymeleafAnalyzer <path-to-html-file>");
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
            System.err.printf("HTML file does not exist: %s%n", file);
            return USAGE_OR_IO_ERROR;
        }

        try {
            AnalysisResult result = analyze(file);
            if (!result.success()) {
                result.errors().forEach(error -> System.err.println(error.format()));
                System.err.printf(
                        Locale.ROOT,
                        "HTML + Thymeleaf analysis failed with %d error(s).%n",
                        result.errors().size());
                return SYNTAX_ERROR;
            }

            System.out.printf(
                    Locale.ROOT,
                    "HTML + Thymeleaf analysis succeeded: %s (%d Thymeleaf attribute(s))%n",
                    file,
                    result.thymeleafAttributeCount());
            return SUCCESS;
        } catch (IOException | SecurityException exception) {
            System.err.printf("Cannot read HTML file '%s': %s%n", file, exception.getMessage());
            return USAGE_OR_IO_ERROR;
        }
    }

    public record AnalysisResult(
            int thymeleafAttributeCount,
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
            String attribute,
            String message) {

        public String format() {
            String attributeDetails = attribute == null ? "" : " [" + attribute + "]";
            return String.format(
                    Locale.ROOT,
                    "%s:%d:%d: %s error%s: %s",
                    file,
                    line,
                    column,
                    stage,
                    attributeDetails,
                    message);
        }
    }

    private static final class HtmlErrorListener extends BaseErrorListener {

        private final Path file;
        private final String stage;
        private final List<AnalysisError> errors;

        private HtmlErrorListener(Path file, String stage, List<AnalysisError> errors) {
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
                    null,
                    message));
        }
    }

    private static final class ThymeleafErrorListener extends BaseErrorListener {

        private final Path file;
        private final String attribute;
        private final int valueLine;
        private final int valueColumn;
        private final int quoteOffset;
        private final List<AnalysisError> errors;

        private ThymeleafErrorListener(
                Path file,
                String attribute,
                int valueLine,
                int valueColumn,
                int quoteOffset,
                List<AnalysisError> errors) {
            this.file = file;
            this.attribute = attribute;
            this.valueLine = valueLine;
            this.valueColumn = valueColumn;
            this.quoteOffset = quoteOffset;
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
            int mappedLine = valueLine + line - 1;
            int mappedColumn = line == 1
                    ? valueColumn + quoteOffset + charPositionInLine + 1
                    : charPositionInLine + 1;

            errors.add(new AnalysisError(
                    file,
                    mappedLine,
                    mappedColumn,
                    "Thymeleaf",
                    attribute,
                    message));
        }
    }
}
