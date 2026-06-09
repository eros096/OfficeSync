package constants;

public final class ValidationUtil {
    private ValidationUtil() {
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static int parsePositiveInt(String value, String fieldName) {
        try {
            int parsed = Integer.parseInt(value.trim());
            if (parsed <= 0) {
                throw new IllegalArgumentException(fieldName + " must be greater than zero.");
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldName + " must be a valid number.");
        }
    }

    public static int parseZeroOrPositiveInt(String value, String fieldName) {
        try {
            int parsed = Integer.parseInt(value.trim());
            if (parsed < 0) {
                throw new IllegalArgumentException(fieldName + " must be zero or greater.");
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldName + " must be a valid number.");
        }
    }
}
