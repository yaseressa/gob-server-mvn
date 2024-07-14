package com.kq.gob.Gob;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class Gob {
    static boolean hasError = false;
    static boolean hasRuntimeError = false;
    private static LinkedList<String> errors = new LinkedList<>();
    static Interpreter interpreter = new Interpreter();

    public static void main(String[] args){

        if (args.length > 1) {
            System.out.println("Istimaal: [programka]");
            System.exit(64);
        } else if (args.length == 1) {
            String arg = args[0].replace("\\\\", "/");
            try {
                runFile(arg);
            } catch (IOException e) {
                System.err.println("Faylka lama helin: " + arg);
                System.exit(64);
            }
        } else {
            try {
                runPrompt();
            } catch (IOException e) {
                System.err.println("Waxaad ka qaybqaadan kartaa");
                System.exit(64);
            }
        }

    }

    private static void runFile(String path) throws IOException {
        if(!path.endsWith(".gob")){
            System.err.println("Fadlan isticmaal fileka .gob");
            System.exit(64);
        }
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        if (hasError) System.exit(65);
        if (hasRuntimeError) System.exit(70);
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
        while (true) {
            System.out.print("Qor ==>> ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);
            hasError = false;
        }
    }

    public static String run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        var parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();
        Resolver resolver = new Resolver(interpreter);
        String res = "";
        try{
            resolver.resolve(statements);
            res = (String) interpreter.
                    interpret(statements);
        }catch(Exception e){
            if(errors.isEmpty()){
            errors.add(e.getMessage());}
        }
        if (!errors.isEmpty()){

        String Errors = "";
        for (String error : errors) {
            if (error != null){
            Errors += error + "\n";}
        }
        errors.clear();
        return Errors + res.trim();
        }
    return res;
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where,
                               String message) {
        errors.add("Qalad" + where + ": " + message + "   ( laynka " + line + " ).");
        hasError = true;
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " halka u dambaysa", message);
        } else {
            report(token.line, " isticmaal '" + token.lexeme + "'", message);
        }
    }

    static void runtimeError(RuntimeError error) {
        errors.add(error.getMessage() + "\t\t   ( laynka " + error.token.line + " ).");
        hasRuntimeError = true;
    }
}
