import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestJavaCore {
    public static void main(String[] args) throws Exception {
        // 1. قراءة كود الجافا من ملف الاختبار
        String code = Files.readString(Paths.get("src/test/resources/java/input.java"));

        // 2. تمرير النص إلى الـ Lexer
        CharStream input = CharStreams.fromString(code);
        JavaSpringLexer lexer = new JavaSpringLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // 3. تمرير الـ Tokens إلى الـ Parser
        JavaSpringParser parser = new JavaSpringParser(tokens);
        ParseTree tree = parser.compilationUnit(); // نقطة البداية

        // 4. طباعة الشجرة كنص في الـ Console
        System.out.println(tree.toStringTree(parser));
        
        // 5. التحقق من عدم وجود أخطاء نحوية
        if (parser.getNumberOfSyntaxErrors() == 0) {
            System.out.println("\n✅ Parsing succeeded with 0 syntax errors!");
        }
    }
}