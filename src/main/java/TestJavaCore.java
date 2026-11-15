import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestJavaCore {
    public static void main(String[] args) throws Exception {
        // 1. Read Test File 
        String filePath = "src/test/resources/java/ProductController.java";
        String code = Files.readString(Paths.get(filePath));

        // 2. Lexer
        CharStream input = CharStreams.fromString(code);
        JavaSpringLexer lexer = new JavaSpringLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        tokens.fill();

        System.out.println("Total Tokens Extracted: " + tokens.size());

        // 3. Parser
        JavaSpringParser parser = new JavaSpringParser(tokens);
        ParseTree tree = parser.compilationUnit(); // تأكد أن اسم القاعدة الجذرية لديكم compilationUnit

        // 4. File Result
        if (parser.getNumberOfSyntaxErrors() == 0) {
            System.out.println("✅ Integration Test Passed! 0 Syntax Errors.");
            System.out.println("\n--- Parse Tree ---");
            System.out.println(tree.toStringTree(parser));
        } else {
            System.out.println("❌ Test Failed with " + parser.getNumberOfSyntaxErrors() + " syntax errors.");
        }
    }
}