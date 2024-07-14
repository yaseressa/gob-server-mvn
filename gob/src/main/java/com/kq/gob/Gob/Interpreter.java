package com.kq.gob.Gob;

import java.util.Scanner;
import java.util.*;

import static com.kq.gob.Gob.TokenType.*;
public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Object>{
    final Environment globals = new Environment();
    private Map<Expr, Integer> locals = new HashMap<>();
    private LinkedList<String> interpreted = new LinkedList<>();
    private Environment ENV = globals;
    public static Map<String, Object> objVars = new HashMap<>();

    void resolve(Expr expr, int depth) {
        locals.put(expr, depth);
    }

    public Interpreter() {
        globals.declare("saacad", new GobCalls() {
            @Override
            public int arity() { return 0; }
            @Override
            public Object call(Interpreter interpreter,
                               List<Object> arguments) {
                return (double)System.currentTimeMillis() / 1000.0;
            }
            @Override
            public String toString() { return "<native qabte>"; }
        });
        globals.declare("akhri", new GobCalls() {
            @Override
            public int arity() { return 0; }
            @Override
            public Object call(Interpreter interpreter,
                               List<Object> arguments) {
                try {
                    return evaluate(new Expr.Literal(new Scanner(System.in).nextLine().trim()));
                } catch (Exception e) {
                    return null;
                }
            }
            @Override
            public String toString() { return "<native qabte>"; }
        });
    }
    public Object interpret(List<Stmt> expression) {
        try {
            for (var expr: expression) {
                execute(expr);
            }
         if(!interpreted.isEmpty()){
             String longString = "";
             for (String s : interpreted) {
                longString += s + " ";
             }
             interpreted.clear();
             return longString;
         }
        } catch (RuntimeError error) {
            Gob.runtimeError(error);
        }
        return null;
    }
    void executeBlock(List<Stmt> statements,
                      Environment environment) {
        Environment previous = this.ENV;
        try {
            this.ENV = environment;
            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.ENV = previous;
        }
    }

    private void execute(Stmt expr) {
        expr.accept(this);
    }

    public Object evaluate(Expr expr){
        return expr.accept(this);
    }
    private static Object performArithmeticOperation(Object left, Object right, TokenType operator) {
        if (left instanceof String || right instanceof String) {
            return left.toString() + right.toString();
        }

        boolean bothIntegers = left instanceof Integer && right instanceof Integer;
        if (bothIntegers) {
            int leftInt = (Integer) left;
            int rightInt = (Integer) right;
            switch (operator) {
                case PLUS -> { return leftInt + rightInt; }
                case MINUS -> { return leftInt - rightInt; }
                case STAR -> { return leftInt * rightInt; }
                case SLASH -> { return leftInt / rightInt; }
                case PERCENT -> { return leftInt % rightInt; }
                default -> { }
            }
        } else {
            double leftDouble = ((Number) left).doubleValue();
            double rightDouble = ((Number) right).doubleValue();
            switch (operator) {
                case PLUS -> { return leftDouble + rightDouble; }
                case MINUS -> { return leftDouble - rightDouble; }
                case STAR -> { return leftDouble * rightDouble; }
                case SLASH -> { return (int)(leftDouble / rightDouble); }
                case PERCENT -> { return leftDouble % rightDouble; }
                default -> { }
            }
        }
        return 0;
    }
    private static String performComparisonOperation(Object left, Object right, TokenType operator) {
        if (left instanceof Number && right instanceof Number) {
            double leftDouble, rightDouble;

            if (!(left instanceof Integer && right instanceof Integer) || operator == BANG_EQUAL || operator == EQUAL_EQUAL) {
                leftDouble = ((Number) left).doubleValue();
                rightDouble = ((Number) right).doubleValue();
            } else {
                int leftInt = (Integer) left;
                int rightInt = (Integer) right;
                return switch (operator) {
                    case GREATER -> leftInt > rightInt ? "run" : "been";
                    case GREATER_EQUAL -> leftInt >= rightInt ? "run" : "been";
                    case LESS -> leftInt < rightInt ? "run" : "been";
                    case LESS_EQUAL -> leftInt <= rightInt ? "run" : "been";
                    default -> throw new IllegalArgumentException("Unsupported operator for integer comparison.");
                };
            }

            boolean result = switch (operator) {
                case GREATER -> leftDouble > rightDouble;
                case GREATER_EQUAL -> leftDouble >= rightDouble;
                case LESS -> leftDouble < rightDouble;
                case LESS_EQUAL -> leftDouble <= rightDouble;
                case BANG_EQUAL -> leftDouble != rightDouble;
                case EQUAL_EQUAL -> leftDouble == rightDouble;
                default -> throw new IllegalArgumentException("Unsupported operator for comparison.");
            };
            return result ? "run" : "been";
        } else {
            boolean result = switch (operator) {
                case BANG_EQUAL -> !left.equals(right);
                case EQUAL_EQUAL -> left.equals(right);
                default -> throw new IllegalArgumentException("Unsupported operator for non-Number comparison.");
            };
            return result ? "run" : "been";
        }
    }

    public Object isTruthy(Object bool){
        if(bool == "run")
            return "run";
        if(bool == "been")
            return "been";
        return "been";
    }
    private Object lookUpVariable(Token name, Expr expr) {
        Integer distance = locals.get(expr);
        if (distance != null) {
            return ENV.getAt(distance, name.lexeme);
        } else {
            return globals.getVariable(name);
        }
    }


    private boolean isEqual(Object left, Object right) {
        return (left == null && right == null) || left.equals(right);
    }
    private void checkOperand(Token operator, Object... objects) {
        for (var obj: objects) {
            if (!(obj instanceof Number)) throw new RuntimeError(operator, "Number waa inay ahaadan Labaduba.");
        }
    }
    private String stringify(Object object) {
        if (object == null) return "ban";
        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return object.toString();
    }

    @Override
    public Object visit(Expr.Binary expr) {
        Object right = evaluate(expr.right);
        Object left = evaluate(expr.left);
        switch (expr.operator.type) {
            case MINUS, PLUS, STAR, SLASH, PERCENT -> {
                return performArithmeticOperation(left, right, expr.operator.type);
            }
            case GREATER, GREATER_EQUAL, LESS, LESS_EQUAL, BANG_EQUAL, EQUAL_EQUAL -> {
                return performComparisonOperation(left, right, expr.operator.type);
            }
        }
        return null;
    }


    @Override
    public Object visit(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visit(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visit(Expr.Unary expr) {
        Object right = evaluate(expr.right);
        switch (expr.operator.type) {
            case MINUS -> {
                return -(double) right;
            }
            case BANG -> {
                if(right == "ban") return "been";
                if(right == "run")
                    return "been";
                if(right == "been")
                    return "run";
            }
        }
        return null;
    }

    @Override
    public Void visit(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(ENV));
        return null;
    }

    @Override
    public Void visit(Stmt.Class stmt) {
        Object superclass = null;
        if (stmt.superclass != null) {
            superclass = evaluate(stmt.superclass);
            if (!(superclass instanceof GobClass)) {
                throw new RuntimeError(stmt.superclass.name,
                        "Waalid Waa Inuu cayn Ahaadaa. ");
            }
        }
        ENV.declare(stmt.name.lexeme, null);
        if (stmt.superclass != null) {
            ENV = new Environment(ENV);
            ENV.declare("ab", superclass);
        }
        Map<String, GobFunction> methods = new HashMap<>();
        for (Object method : stmt.methods) {
            if (method instanceof Stmt.Function) {
                var function = new GobFunction((Stmt.Function) method, ENV, ((Stmt.Function) method).name.lexeme.equals(stmt.name.lexeme));
                if(!methods.containsKey(((Stmt.Function) method).name.lexeme))
                    methods.put(((Stmt.Function) method).name.lexeme, function);
                else
                    throw new RuntimeError((((Stmt.Function) method).name), ((Stmt.Function) method).name.lexeme + ":  qabte Hore Loomagacaabay");

            }
            else if(method instanceof Stmt.Var){
                Object ev = evaluate(((Stmt.Var) method).initializer);
                objVars.put(((Stmt.Var) method).name.lexeme, ev);

            }
        }
        GobClass klass = new GobClass(stmt.name.lexeme,(GobClass) superclass, methods);
        if (superclass != null) {
            ENV = ENV.enclosing;
        }
        ENV.assign(stmt.name, klass);
        return null;
    }

    @Override
    public Object visit(Expr.Get expr) {
        Object object = evaluate(expr.object);

        if (object instanceof GobInstance) {
            return ((GobInstance) object).get(expr.name);
        }
        throw new RuntimeError(expr.name, "walxaha kaliya Ayuunba Leh sifooyin");
    }

    @Override
    public Void visit(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visit(Stmt.PrintLN stmt) {
        interpreted.add(stringify(evaluate(stmt.expression)) + "\n");
        return null;
    }

    @Override
    public Void visit(Stmt.Print stmt) {
        interpreted.add(stringify(evaluate(stmt.expression)));
        return null;
    }

    @Override
    public Object visit(Expr.Length expr) {
    Object value = evaluate(expr.expression);
        if (value instanceof ArrayList) {
            return (double)(((ArrayList<?>) value).size());
        } else {
            throw new RuntimeError(((Expr.Variable)expr.expression).name, "taxaneyaasha kaliya Ayuunba Leh dherer");
        }
    }

    @Override
    public Void visit(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }
        ENV.declare(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Void visit(Stmt.Listing stmt) {
        ArrayList<Object> value = new ArrayList<>();
        if (stmt.initializer != null) {
            for (Expr v: stmt.initializer) {
                value.add(evaluate(v));
            }
        }
        ENV.declare(stmt.name.lexeme, value);
        return null;
    }


    @Override
    public Void visit(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.condition)) == "run") {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }else if(stmt.elseIF != null){
            stmt.elseIF.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(Stmt.Else stmt) {
        return null;
    }

    @Override
    public Void visit(Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.condition)) == "run") {
            execute(stmt.body);
        }
        return null;
    }

    @Override
    public Object visit(Expr.Variable expr) {
        return lookUpVariable(expr.name, expr);
    }

    @Override
    public Object visit(Expr.ListCall expr) {
        try {

            Object index = evaluate(expr.index);
            var looked = evaluate(expr.name);
            if (looked instanceof ArrayList) {
                return ((ArrayList) looked).get(Integer.parseInt(index.toString().split("[.]")[0]));
            } else {
                throw new RuntimeError(expr.name.name, "doorsomahani maha taxane [" + expr.name + "]");
            }

        } catch (IndexOutOfBoundsException ex) {
            ArrayList looked = (ArrayList) lookUpVariable(expr.name.name, expr);
            throw new RuntimeError(expr.name.name, "God aan jirin " + "Taxanaha '" + expr.name + "' dhererkiisu waa " + looked.size());
        }
//        try{
//        return lookUpVariable(expr.name, expr);
//        }
//        catch(Exception ex){
//            Object index = expr.index.accept(this);
//        return lookUpVariable(expr.name, expr, Integer.parseInt(String.valueOf(index).split("[.]")[0]));
//        }        try{
//        return lookUpVariable(expr.name, expr);
//        }
//        catch(Exception ex){
//            Object index = expr.index.accept(this);
//        return lookUpVariable(expr.name, expr, Integer.parseInt(String.valueOf(index).split("[.]")[0]));
//        }
    }

    @Override
    public Object visit(Expr.ListUpdate expr) {
        Object value = evaluate(expr.value);
        try {

            Object index = evaluate(expr.index);
            var looked = evaluate(expr.name);
            if (looked instanceof ArrayList) {
                ((ArrayList) looked).set(Integer.parseInt(index.toString().split("[.]")[0]), value);
                return value;
            } else {
                throw new RuntimeError(expr.name.name, "doorsomahani maha taxane [" + expr.name.name.literal + "]");
            }


        } catch (IndexOutOfBoundsException ex) {
            ArrayList looked = (ArrayList) lookUpVariable(expr.name.name, expr);
            throw new RuntimeError(expr.name.name, "God aan jirin " + "Taxanaha '" + expr.name.name.lexeme + "' dhererkiisu waa " + looked.size());
        }
    }

    @Override
    public Object visit(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        Integer distance = locals.get(expr);
        if (distance != null) {
            ENV.assignAt(distance, expr.name, value);
        } else {
            globals.assign(expr.name, value);
        }
        return value;
    }

    @Override
    public Void visit(Expr.CompAssign expr) {
        Object value = evaluate(expr.value);

        Integer distance = locals.get(expr);
        if (distance != null) {
            var ogValue = ENV.getVariable(expr.name);
            if (ogValue instanceof String) {
                if (expr.operator.type == COMPOUND_PLUS) {
                    ENV.assignAt(distance, expr.name, String.valueOf(ogValue) + value);
                    return null;
                }
                throw new RuntimeError(expr.name, "walaxdan hawlgalkan looma sameyn karo");
            }
            if (ogValue instanceof Double) {
                doubleOperations(expr, ogValue, value);
                return null;
            } else if (ogValue instanceof Integer) {
                integerOperations(expr, ogValue, value);
                return null;
            }
        } else {
            var ogValue = ENV.getVariable(expr.name);
            if(ogValue instanceof String){
                if(expr.operator.type == COMPOUND_PLUS){
                    ENV.assign(expr.name, String.valueOf(ogValue) + value);
                    return null;
                }
                throw new RuntimeError(expr.name, "walaxdan hawlgalkan looma sameyn karo");
            }
            if(ogValue instanceof Double){
                doubleOperations(expr, ogValue, value);
                return null;
            }else if(ogValue instanceof Integer){
                integerOperations(expr, ogValue, value);
                return null;
            }
        }
        throw new RuntimeError(expr.name, "walaxdan hawlgalkan looma sameyn karo");
    }

void doubleOperations(Expr.CompAssign expr, Object ogValue, Object value){
    if(expr.operator.type == COMPOUND_PLUS) ENV.assign(expr.name, (Double)ogValue + (Double)value);
    if(expr.operator.type == COMPOUND_MINUS) ENV.assign(expr.name, (Double)ogValue - (Double)value);
    if(expr.operator.type == COMPOUND_SLASH) ENV.assign(expr.name, (Double)ogValue / (Double)value);
    if(expr.operator.type == COMPOUND_STAR) ENV.assign(expr.name, (Double)ogValue * (Double)value);
    if(expr.operator.type == COMPOUND_PERCENT) ENV.assign(expr.name, (Double)ogValue % (Double)value);
}
void integerOperations(Expr.CompAssign expr, Object ogValue, Object value){
    if(expr.operator.type == COMPOUND_PLUS) ENV.assign(expr.name, (Integer)ogValue + (Integer)value);
    if(expr.operator.type == COMPOUND_MINUS) ENV.assign(expr.name, (Integer)ogValue - (Integer)value);
    if(expr.operator.type == COMPOUND_SLASH) ENV.assign(expr.name, (Integer)ogValue / (Integer)value);
    if(expr.operator.type == COMPOUND_STAR) ENV.assign(expr.name, (Integer)ogValue * (Integer)value);
    if(expr.operator.type == COMPOUND_PERCENT) ENV.assign(expr.name, (Integer)ogValue % (Integer)value);
}

    @Override
    public Object visit(Expr.Logical expr) {
        Object left = evaluate(expr.left);
        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left) == "run") return left;
        } else {
            if (isTruthy(left) == "been") return left;
        }
        return evaluate(expr.right);
    }

    @Override
    public Object visit(Expr.Set expr) {
       Object  currentObject = evaluate(expr.object);
        if (!(currentObject instanceof GobInstance)) {
            throw new RuntimeError(expr.name,
                    "Walxaha Ayuunbaa Lahan kara sifooyin.");
        }
        Object value = evaluate(expr.value);
        ((GobInstance)currentObject).set(expr.name, value);

        return value;
    }

    @Override
    public Object visit(Expr.Super expr) {
        int distance = locals.get(expr);
        GobClass superclass = (GobClass)ENV.getAt(
                distance, "ab");
        GobInstance object = (GobInstance)ENV.getAt(
                distance - 1, "kan");
        GobFunction method = superclass.findMethod(expr.method.lexeme);
        if (method == null) {
            throw new RuntimeError(expr.method,
                    "Sifo Aan La Magacabin '" + expr.method.lexeme + "'.");
        }
        return method.bind(object);
    }

    @Override
    public Object visit(Expr.This expr) {
        return lookUpVariable(expr.keyword, expr);
    }

    @Override
    public Object visit(Expr.Call expr) {
        Object callee = evaluate(expr.callee);
        List<Object> arguments = new ArrayList<>();
        for (Expr argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }
        if(!(callee instanceof GobCalls)){
            throw new RuntimeError(expr.paren, "Waxa Loo Yeedhi Karaa qabte Ama cayn Uun");
        }
        GobCalls function = (GobCalls) callee;
        if (arguments.size() != function.arity()) {
            throw new RuntimeError(expr.paren, "La Filayey " +
                    function.arity() + " masalo Lakin Waxa La Helay " +
                    arguments.size() + ".");
        }
        return function.call(this, arguments);
    }
    @Override
    public Void visit(Stmt.Function func) {
        var function = new GobFunction(func, ENV, false);
        ENV.declare(func.name.lexeme, function);
        return null;    }

    @Override
    public Void visit(Stmt.Return stmt) {
        Object value = null;
        if (stmt.value != null) value = evaluate(stmt.value);
        throw new Return(value);
    }
}
