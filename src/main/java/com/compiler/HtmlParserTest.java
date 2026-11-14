package com.compiler;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Stream;

public class HtmlParserTest {

    public static void main(String[] args) {
        Path validDir = Paths.get("src/test/resources/html/valid");
        Path invalidDir = Paths.get("src/test/resources/html/invalid");

        System.out.println("==========================================");
        System.out.println("   Running VALID HTML Test Cases");
        System.out.println("==========================================");
        int validPassed = runTestsInDirectory(validDir, true);

        System.out.println("\n==========================================");
        System.out.println("   Running INVALID HTML Test Cases");
        System.out.println("==========================================");
        int invalidPassed = runTestsInDirectory(invalidDir, false);

        System.out.println("\n==========================================");
        System.out.println("Summary: Valid Passed = " + validPassed + " | Invalid Passed = " + invalidPassed);
        System.out.println("==========================================");
    }

    private static int runTestsInDirectory(Path directory, boolean expectValid) {
        if (!Files.exists(directory)) {
            System.out.println("[WARNING] Directory not found: " + directory.toAbsolutePath());
            return 0;
        }

        int passedCount = 0;

        try (Stream<Path> paths = Files.walk(directory)) {
            List<Path> htmlFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".html"))
                    .toList();

            if (htmlFiles.isEmpty()) {
                System.out.println("[INFO] No .html files found in: " + directory);
                return 0;
            }

            for (Path file : htmlFiles) {
                System.out.println("\n--- Testing: " + file.getFileName() + " ---");
                boolean isParsedSuccessfully = parseFile(file, expectValid);

                if (isParsedSuccessfully == expectValid) {
                    System.out.println("[PASS] " + file.getFileName());
                    passedCount++;
                } else {
                    System.out.println("[FAIL] " + file.getFileName()
                            + " (Expected valid=" + expectValid + ", Actual=" + isParsedSuccessfully + ")");
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading directory " + directory + ": " + e.getMessage());
        }

        return passedCount;
    }

    private static boolean parseFile(Path file, boolean printTree) {
        try {
            ErrorCounter errorCounter = new ErrorCounter();

            com.compiler.HtmlLexer lexer = new com.compiler.HtmlLexer(CharStreams.fromPath(file));
            lexer.removeErrorListeners();
            lexer.addErrorListener(errorCounter);

            CommonTokenStream tokens = new CommonTokenStream(lexer);

            com.compiler.HtmlParser parser = new com.compiler.HtmlParser(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(errorCounter);

            ParseTree tree = parser.document();

            if (errorCounter.getErrorCount() == 0 && printTree) {
                System.out.println("   -> Parse Tree: " + tree.toStringTree(parser));
            }

            return errorCounter.getErrorCount() == 0;

        } catch (IOException e) {
            System.err.println("Could not read file " + file + ": " + e.getMessage());
            return false;
        }
    }

    static class ErrorCounter extends BaseErrorListener {
        private int errorCount = 0;

        @Override
        public void syntaxError(
                Recognizer<?, ?> recognizer,
                Object offendingSymbol,
                int line,
                int charPositionInLine,
                String msg,
                RecognitionException e
        ) {
            errorCount++;
            System.out.println(
                    "   Error at line " + line +
                            ":" + charPositionInLine +
                            " - " + msg
            );
        }

        public int getErrorCount() {
            return errorCount;
        }
    }
}