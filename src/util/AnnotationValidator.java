
package util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import model.annotation.Positive;
import model.annotation.MaxLength;

public class AnnotationValidator {
    public static String[] validate(Object obj) {
        List<String> errors = new ArrayList<>();

        if (obj == null) {
            return new String[]{"Object must not be null"};
        }

        Class<?> type = obj.getClass();

        while (type != null && type != Object.class) {
            Field[] fields = type.getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);

                try {
                    Object value = field.get(obj);

                    if (field.isAnnotationPresent(Positive.class)) {
                        Positive positive = field.getAnnotation(Positive.class);

                        if (value instanceof Number number
                                && number.doubleValue() <= 0) {
                            errors.add(field.getName() + ": " + positive.message());
                        }
                    }

                    if (field.isAnnotationPresent(MaxLength.class)) {
                        MaxLength maxLength =
                                field.getAnnotation(MaxLength.class);

                        if (value instanceof String str
                                && str.length() > maxLength.value()) {
                            errors.add(field.getName()
                                    + ": maximum length is "
                                    + maxLength.value());
                        }
                    }
                } catch (IllegalAccessException e) {
                    errors.add(field.getName() + ": could not access field");
                }
            }

            type = type.getSuperclass();
        }

        return errors.toArray(new String[0]);
    }
}
