package ru.itis.dis403.lab2_2.orm;

import ru.itis.dis403.lab2_2.orm.annotation.*;

import java.io.Closeable;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

public class EntityManagerImpl implements EntityManager, Closeable {

    private Connection connection;
    private List<Class<?>> entities;

    public EntityManagerImpl(Connection connection, List<Class<?>> entities) {
        this.connection = connection;
        this.entities = entities;
    }

    @Override
    public <T> T save(T entity) {
        try {
            Class<?> cls = entity.getClass();
            String tableName = cls.getSimpleName().toLowerCase();

            Field idField = getIdField(cls);
            idField.setAccessible(true);
            Object idValue = idField.get(entity);

            if (idValue == null) {
                // INSERT
                List<String> colNames = new ArrayList<>();
                List<Object> values = new ArrayList<>();

                for (Field field : cls.getDeclaredFields()) {
                    field.setAccessible(true);
                    if (field.isAnnotationPresent(Column.class)) {
                        colNames.add(field.getName());
                        values.add(field.get(entity));
                    } else if (field.isAnnotationPresent(ManyToOne.class)) {
                        colNames.add(field.getName() + "_id");
                        Object related = field.get(entity);
                        if (related != null) {
                            Field relId = getIdField(related.getClass());
                            relId.setAccessible(true);
                            values.add(relId.get(related));
                        } else {
                            values.add(null);
                        }
                    }
                }

                String cols = String.join(", ", colNames);
                String placeholders = String.join(", ", Collections.nCopies(colNames.size(), "?"));
                String sql = "INSERT INTO " + tableName + " (" + cols + ") VALUES (" + placeholders + ") RETURNING id";

                PreparedStatement ps = connection.prepareStatement(sql);
                for (int i = 0; i < values.size(); i++) {
                    ps.setObject(i + 1, values.get(i));
                }
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    idField.set(entity, rs.getLong(1));
                }
                System.out.println("INSERT: " + entity);
            } else {
                // UPDATE
                List<String> setClauses = new ArrayList<>();
                List<Object> values = new ArrayList<>();

                for (Field field : cls.getDeclaredFields()) {
                    field.setAccessible(true);
                    if (field.isAnnotationPresent(Column.class)) {
                        setClauses.add(field.getName() + " = ?");
                        values.add(field.get(entity));
                    } else if (field.isAnnotationPresent(ManyToOne.class)) {
                        setClauses.add(field.getName() + "_id = ?");
                        Object related = field.get(entity);
                        if (related != null) {
                            Field relId = getIdField(related.getClass());
                            relId.setAccessible(true);
                            values.add(relId.get(related));
                        } else {
                            values.add(null);
                        }
                    }
                }
                values.add(idValue);

                String sql = "UPDATE " + tableName + " SET " + String.join(", ", setClauses) + " WHERE id = ?";
                PreparedStatement ps = connection.prepareStatement(sql);
                for (int i = 0; i < values.size(); i++) {
                    ps.setObject(i + 1, values.get(i));
                }
                ps.executeUpdate();
                System.out.println("UPDATE: " + entity);
            }
            return entity;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remove(Object entity) {
        try {
            Class<?> cls = entity.getClass();
            String tableName = cls.getSimpleName().toLowerCase();
            Field idField = getIdField(cls);
            idField.setAccessible(true);
            Object id = idField.get(entity);

            String sql = "DELETE FROM " + tableName + " WHERE id = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setObject(1, id);
            ps.executeUpdate();
            System.out.println("DELETE from " + tableName + " where id=" + id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T find(Class<T> entityType, Object key) {
        try {
            String tableName = entityType.getSimpleName().toLowerCase();
            String sql = "SELECT * FROM " + tableName + " WHERE id = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setObject(1, key);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapRow(entityType, rs);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> List<T> findAll(Class<T> entityType) {
        try {
            String tableName = entityType.getSimpleName().toLowerCase();
            String sql = "SELECT * FROM " + tableName;
            ResultSet rs = connection.createStatement().executeQuery(sql);

            List<T> result = new ArrayList<>();
            while (rs.next()) {
                result.add(mapRow(entityType, rs));
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Создаём объект из строки ResultSet
    private <T> T mapRow(Class<T> cls, ResultSet rs) throws Exception {
        T obj = cls.getDeclaredConstructor().newInstance();
        for (Field field : cls.getDeclaredFields()) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(Column.class)) {
                field.set(obj, rs.getObject(field.getName()));
            } else if (field.isAnnotationPresent(ManyToOne.class)) {
                // Загружаем связанный объект по _id
                Object relatedId = rs.getObject(field.getName() + "_id");
                if (relatedId != null) {
                    Object related = find(field.getType(), relatedId);
                    field.set(obj, related);
                }
            }
        }
        return obj;
    }

    // Ищем поле с @Id в классе
    private Field getIdField(Class<?> cls) {
        for (Field field : cls.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                return field;
            }
        }
        throw new RuntimeException("Нет @Id в классе " + cls.getSimpleName());
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
