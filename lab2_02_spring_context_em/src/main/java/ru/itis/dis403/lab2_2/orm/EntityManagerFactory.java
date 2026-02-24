package ru.itis.dis403.lab2_2.orm;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import ru.itis.dis403.lab2_2.orm.annotation.*;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

public class EntityManagerFactory {

    private HikariDataSource dataSource;
    private List<Class<?>> entities = new ArrayList<>();

    public EntityManagerFactory(List<Class<?>> entityClasses) throws Exception {
        Class.forName("org.postgresql.Driver");

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/lab2");
        config.setUsername("elvina");
        config.setPassword("");
        config.setConnectionTimeout(50000);
        config.setMaximumPoolSize(10);
        dataSource = new HikariDataSource(config);

        // Сохраняем список сущностей
        for (Class<?> cls : entityClasses) {
            if (cls.isAnnotationPresent(Entity.class)) {
                entities.add(cls);
                System.out.println("Найдена сущность: " + cls.getSimpleName());
            }
        }

        // Генерируем DDL и создаём таблицы
        generateSchema();

        // Проверяем соответствие модели и БД
        validateSchema();
    }

    // Генерация CREATE TABLE по структуре классов
    private void generateSchema() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            for (Class<?> cls : entities) {
                String tableName = cls.getSimpleName().toLowerCase();
                StringBuilder sql = new StringBuilder();
                sql.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append(" (\n");

                List<String> columns = new ArrayList<>();
                for (Field field : cls.getDeclaredFields()) {
                    if (field.isAnnotationPresent(Id.class)) {
                        columns.add("    " + field.getName() + " BIGSERIAL PRIMARY KEY");
                    } else if (field.isAnnotationPresent(Column.class)) {
                        columns.add("    " + field.getName() + " VARCHAR(255)");
                    } else if (field.isAnnotationPresent(ManyToOne.class)) {
                        // country → country_id bigint
                        columns.add("    " + field.getName() + "_id BIGINT");
                    }
                }
                sql.append(String.join(",\n", columns));
                sql.append("\n)");

                System.out.println("DDL: " + sql);
                conn.createStatement().execute(sql.toString());
                System.out.println("Таблица '" + tableName + "' создана (или уже существует)");
            }
        }
    }

    // Проверяем, что таблицы и колонки в БД совпадают с классами
    private void validateSchema() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();

            for (Class<?> cls : entities) {
                String tableName = cls.getSimpleName().toLowerCase();

                // Проверяем наличие таблицы
                ResultSet tables = meta.getTables(null, null, tableName, null);
                if (tables.next()) {
                    System.out.println("Таблица '" + tableName + "' существует");
                } else {
                    System.out.println("Таблица '" + tableName + "' НЕ найдена!");
                    continue;
                }

                // Собираем колонки таблицы из БД
                Set<String> dbColumns = new HashSet<>();
                ResultSet cols = meta.getColumns(null, null, tableName, null);
                while (cols.next()) {
                    dbColumns.add(cols.getString("COLUMN_NAME").toLowerCase());
                }

                // Проверяем каждое поле класса
                for (Field field : cls.getDeclaredFields()) {
                    String expectedCol = null;
                    if (field.isAnnotationPresent(Id.class)) {
                        expectedCol = field.getName().toLowerCase();
                    } else if (field.isAnnotationPresent(Column.class)) {
                        expectedCol = field.getName().toLowerCase();
                    } else if (field.isAnnotationPresent(ManyToOne.class)) {
                        expectedCol = field.getName().toLowerCase() + "_id";
                    }

                    if (expectedCol != null) {
                        if (dbColumns.contains(expectedCol)) {
                            System.out.println("Колонка '" + expectedCol + "' существует");
                        } else {
                            System.out.println("Колонка '" + expectedCol + "' не найдена");
                        }
                    }
                }
            }
        }
    }

    public EntityManager getEntityManager() {
        try {
            return new EntityManagerImpl(dataSource.getConnection(), entities);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        dataSource.close();
    }
}
