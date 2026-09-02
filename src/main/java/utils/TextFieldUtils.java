package utils;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import java.util.function.UnaryOperator;


/**
 * This class contains utility methods for working with TextField components, since TextFields are used in multiple
 * controllers and creating the same code in each controller to write length or character filters would be redundant.
 */

public class TextFieldUtils {


    /**
     * Creates a TextFormatter that limits the length of text in a TextField.
     * @param maxLength The maximum length allowed in the TextField
     * @return A TextFormatter that limits the length of text in a TextField to int maxLength
     */
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

    /**
     * Creates a TextFormatter that limits the length of text in a TextField and only allows numbers to be entered.
     * @param maxLength The maximum length allowed in the TextField
     * @return A TextFormatter that limits the length of text in a TextField to int maxLength and only allows numbers to be entered
     */
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

    /**
     * <p>Checks if all text fields in a passed TextField array are filled.<p/>
     * <p>Note: This method can be used by controllers by placing all scene TextFields in an array and passing
     * them into the method, allowing for limited validation by only returning true if filled.</p>
     * @param textFields
     * @return
     */
    public static boolean fieldsAreFilled(TextField[] textFields) {
        for (TextField t : textFields) {
            if (t.getText().trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

}
