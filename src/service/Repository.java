
package service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import model.annotation.Id;

public class Repository<T> {

    private final HashMap<String, T> items = new HashMap<>();

    public T save(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null");
        }

        try {
            Class<?> type = entity.getClass();

            while (type != null) {
                for (Field field : type.getDeclaredFields()) {
                    if (field.isAnnotationPresent(Id.class)) {
                        field.setAccessible(true);
                        Object idValue = field.get(entity);

                        if (idValue == null) {
                            throw new IllegalArgumentException(
                                "Entity ID cannot be null"
                            );
                        }

                        String id = idValue.toString();
                        items.put(id, entity);
                        return entity;
                    }
                }

                type = type.getSuperclass();
            }

            throw new IllegalArgumentException(
                "No field marked with @Id was found"
            );

        } catch (IllegalAccessException e) {
            throw new IllegalStateException(
                "Could not read entity ID", e
            );
        }
    }

    public T findById(String id) {
        return items.get(id);
    }

    public ArrayList<T> findAll() {
        return new ArrayList<>(items.values());
    }

    public boolean delete(String id) {
        return items.remove(id) != null;
    }

    @SafeVarargs
    public final void saveAll(T... entities) {
        for (T entity : entities) {
            save(entity);
        }
    }

    public int count() {
        return items.size();
    }

    public static <N extends Number> double average(N[] values) {
        if (values == null || values.length == 0) {
            return 0.0;
        }

        double sum = 0;

        for (N value : values) {
            if (value != null) {
                sum += value.doubleValue();
            }
        }

        return sum / values.length;
    }
}
