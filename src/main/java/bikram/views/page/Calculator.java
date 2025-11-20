package bikram.views.page;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * 💎 Modern iPhone-like Calculator (JavaFX)
 * by Bikram
 */
public class Calculator extends  VBox{

    private final StringBuilder expr = new StringBuilder();
    private Label display;


    public Calculator() {
        BorderPane root = new BorderPane();
        root.setPrefSize(360, 550);
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #2c2c2c, #1b1b1b);");

        // 🧠 Display
        display = new Label("0");
        display.setFont(Font.font("SF Pro Display", 48));
        display.setTextFill(Color.WHITE);
        display.setAlignment(Pos.CENTER_RIGHT);
        display.setMaxWidth(Double.MAX_VALUE);
        display.setPadding(new Insets(20, 20, 20, 20));
        BorderPane.setMargin(display, new Insets(15, 15, 0, 15));

        root.setTop(display);

        // 🧮 Buttons
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        String[][] btns = {
                {"AC", "⌫", "%", "÷"},
                {"7", "8", "9", "×"},
                {"4", "5", "6", "-"},
                {"1", "2", "3", "+"},
                {"0", ".", "=", ""}
        };

        for (int r = 0; r < btns.length; r++) {
            for (int c = 0; c < btns[r].length; c++) {
                String text = btns[r][c];
                if (text.isEmpty()) continue;
                Button btn = createButton(text);
                grid.add(btn, c, r);
            }
        }

        root.setCenter(grid);
        root.setStyle("-fx-font-weight: 500;\n" +
                "            -fx-cursor: hand;\n" +
                "            -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 8, 0, 2, 2);");
        getChildren().add(root);
    }

    private Button createButton(String text) {
        Button btn = new Button(text);
        btn.setFont(Font.font("SF Pro Display", 24));
        btn.setPrefSize(75, 75);
        btn.setStyle(getButtonStyle(text));

        btn.setOnAction(e -> handleClick(text));
        DropShadow shadow = new DropShadow(10, Color.rgb(0, 0, 0, 0.3));
        btn.setEffect(shadow);

        return btn;
    }

    private String getButtonStyle(String text) {
        if (text.matches("[÷×\\-+%=]")) {
            return "-fx-background-radius:50; -fx-background-color:#ff9f0a; -fx-text-fill:white;";
        } else if (text.equals("AC") || text.equals("⌫")) {
            return "-fx-background-radius:50; -fx-background-color:#a5a5a5; -fx-text-fill:black;";
        } else {
            return "-fx-background-radius:50; -fx-background-color:#333333; -fx-text-fill:white;";
        }
    }

    private void handleClick(String text) {
        switch (text) {
            case "AC":
                expr.setLength(0);
                display.setText("0");
                break;
            case "⌫":
                if (expr.length() > 0) expr.deleteCharAt(expr.length() - 1);
                display.setText(expr.length() == 0 ? "0" : expr.toString());
                break;
            case "=":
                calc();
                break;
            default:
                expr.append(text);
                display.setText(expr.toString());
        }
    }

    // 🧠 Calculator logic (pure Java evaluator)
    private void calc() {
        try {
            String expression = expr.toString()
                    .replace("÷", "/")
                    .replace("×", "*")
                    .replace("%", "/100");

            double result = evaluate(expression);
            display.setText(trimResult(result));
            expr.setLength(0);
            expr.append(trimResult(result));
        } catch (Exception e) {
            display.setText("Error");
            expr.setLength(0);
        }
    }

    private String trimResult(double result) {
        if (result == (long) result)
            return String.format("%d", (long) result);
        else
            return String.format("%.6f", result);
    }

    private double evaluate(String expression) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < expression.length()) ? expression.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < expression.length()) throw new RuntimeException("Unexpected: " + (char) ch);
                return x;
            }

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if (eat('+')) x += parseTerm();
                    else if (eat('-')) x -= parseTerm();
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if (eat('*')) x *= parseFactor();
                    else if (eat('/')) x /= parseFactor();
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor();
                if (eat('-')) return -parseFactor();

                double x;
                int startPos = this.pos;
                if (eat('(')) {
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(expression.substring(startPos, this.pos));
                } else {
                    throw new RuntimeException("Unexpected: " + (char) ch);
                }

                return x;
            }
        }.parse();
    }


}