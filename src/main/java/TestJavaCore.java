import java.nio.file.Path;
import java.util.Locale;

/** Manual parse-tree demo built on top of the reusable JavaSpringAnalyzer. */
public class TestJavaCore {

    public static void main(String[] args) throws Exception {
        Path file = Path.of(args.length > 0
                ? args[0]
                : "src/test/resources/java/input.java");

        JavaSpringAnalyzer.AnalysisResult result = JavaSpringAnalyzer.analyze(file);
        if (!result.success()) {
            result.errors().forEach(error -> System.err.println(error.format()));
            System.err.printf(
                    Locale.ROOT,
                    "Java + Spring analysis failed with %d error(s).%n",
                    result.errors().size());
            System.exit(1);
        }

        System.out.println(result.treeText());
        System.out.println("Java + Spring parsing succeeded with 0 syntax errors.");
    }
}
