package utils;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import java.util.function.UnaryOperator;


public class TextFieldUtils {

    public static TextFormatter<String> createLengthLimitFormatter(int maxLength) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            boolean greaterThanMax = change.getControlNewText().length() > maxLength;

            if (greaterThanMax) {
                return null;
            }
            return change;
        };

        return new TextFormatter<>(filter);
    }

    public static TextFormatter<String> createNumberLimitFormatter(int maxLength) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            boolean isEmpty = change.getControlNewText().isEmpty();
            boolean isDigit = change.getControlNewText().matches("[0-9]+");
            boolean greaterThanMax = change.getControlNewText().length() > maxLength;

            if (greaterThanMax || !(isDigit || isEmpty)) {
                return null;
            }
            return change;
        };

        return new TextFormatter<>(filter);
    }

    public static boolean fieldsAreFilled(TextField[] textFields) {
        for (TextField t : textFields) {
            if (t.getText().trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

}
