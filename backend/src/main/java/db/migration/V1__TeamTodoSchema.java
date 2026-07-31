package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Creates the team Todo schema and upgrades the original prototype tasks table in-place.
 * The migration intentionally uses metadata checks because the original table did not
 * have a Flyway history row and TDSQL deployments may run different MySQL minor versions.
 */
public class V1__TeamTodoSchema extends BaseJavaMigration {
  @Override
  public void migrate(Context context) throws Exception {
    Connection connection = context.getConnection();
    createUsersTable(connection);
    createCategoriesTable(connection);
    if (!tableExists(connection, "tasks")) {
      createTasksTable(connection);
    } else {
      upgradeLegacyTasksTable(connection);
    }
    execute(connection, "INSERT IGNORE INTO task_categories (name, color, sort_order, is_active) VALUES ('未分类', '#64748B', 0, TRUE)");
    execute(connection, "UPDATE tasks SET category_id = (SELECT id FROM task_categories WHERE name = '未分类' LIMIT 1) WHERE category_id IS NULL");
    ensureIndex(connection, "tasks", "idx_tasks_schedule", "CREATE INDEX idx_tasks_schedule ON tasks (start_date, deadline)");
    ensureIndex(connection, "tasks", "idx_tasks_category", "CREATE INDEX idx_tasks_category ON tasks (category_id)");
    ensureIndex(connection, "tasks", "idx_tasks_assignee", "CREATE INDEX idx_tasks_assignee ON tasks (assignee_id)");
    ensureIndex(connection, "tasks", "idx_tasks_status", "CREATE INDEX idx_tasks_status ON tasks (status)");
  }

  private void createUsersTable(Connection connection) throws SQLException {
    execute(connection, """
        CREATE TABLE IF NOT EXISTS users (
          id BIGINT NOT NULL AUTO_INCREMENT,
          username VARCHAR(64) NOT NULL,
          password_hash VARCHAR(100) NOT NULL,
          display_name VARCHAR(100) NOT NULL,
          role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
          is_active BOOLEAN NOT NULL DEFAULT TRUE,
          created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
          updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
          PRIMARY KEY (id),
          UNIQUE KEY uk_users_username (username)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
        """);
  }

  private void createCategoriesTable(Connection connection) throws SQLException {
    execute(connection, """
        CREATE TABLE IF NOT EXISTS task_categories (
          id BIGINT NOT NULL AUTO_INCREMENT,
          name VARCHAR(64) NOT NULL,
          color VARCHAR(7) NOT NULL,
          sort_order INT NOT NULL DEFAULT 0,
          is_active BOOLEAN NOT NULL DEFAULT TRUE,
          created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
          updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
          PRIMARY KEY (id),
          UNIQUE KEY uk_task_categories_name (name)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
        """);
  }

  private void createTasksTable(Connection connection) throws SQLException {
    execute(connection, """
        CREATE TABLE IF NOT EXISTS tasks (
          id BIGINT NOT NULL AUTO_INCREMENT,
          title VARCHAR(255) NOT NULL,
          start_date DATE NOT NULL,
          deadline DATE NOT NULL,
          category_id BIGINT NULL,
          assignee_id BIGINT NULL,
          created_by_id BIGINT NULL,
          status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED',
          progress INT NOT NULL DEFAULT 0,
          version BIGINT NOT NULL DEFAULT 0,
          legacy_assignee VARCHAR(100) NULL,
          created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
          updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
          PRIMARY KEY (id)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
        """);
  }

  private void upgradeLegacyTasksTable(Connection connection) throws SQLException {
    ensureColumn(connection, "tasks", "start_date", "DATE NULL");
    ensureColumn(connection, "tasks", "deadline", "DATE NULL");
    ensureColumn(connection, "tasks", "category_id", "BIGINT NULL");
    ensureColumn(connection, "tasks", "assignee_id", "BIGINT NULL");
    ensureColumn(connection, "tasks", "created_by_id", "BIGINT NULL");
    ensureColumn(connection, "tasks", "version", "BIGINT NOT NULL DEFAULT 0");
    ensureColumn(connection, "tasks", "legacy_assignee", "VARCHAR(100) NULL");
    ensureColumn(connection, "tasks", "status", "VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED'");
    ensureColumn(connection, "tasks", "progress", "INT NOT NULL DEFAULT 0");
    ensureColumn(connection, "tasks", "created_at", "DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)");
    ensureColumn(connection, "tasks", "updated_at", "DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)");

    if (hasColumn(connection, "tasks", "id") && !isAutoIncrement(connection, "tasks", "id")) {
      execute(connection, "ALTER TABLE tasks MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT");
    }
    if (hasColumn(connection, "tasks", "startTime")) {
      execute(connection, "UPDATE tasks SET start_date = COALESCE(start_date, startTime, CURDATE()) WHERE start_date IS NULL");
    } else {
      execute(connection, "UPDATE tasks SET start_date = COALESCE(start_date, CURDATE()) WHERE start_date IS NULL");
    }
    if (hasColumn(connection, "tasks", "endTime")) {
      execute(connection, "UPDATE tasks SET deadline = COALESCE(deadline, endTime, start_date, CURDATE()) WHERE deadline IS NULL");
    } else {
      execute(connection, "UPDATE tasks SET deadline = COALESCE(deadline, start_date, CURDATE()) WHERE deadline IS NULL");
    }
    if (hasColumn(connection, "tasks", "assignee")) {
      execute(connection, "UPDATE tasks SET legacy_assignee = assignee WHERE legacy_assignee IS NULL AND assignee IS NOT NULL AND TRIM(assignee) <> ''");
    }
    execute(connection, "UPDATE tasks SET progress = 0 WHERE progress IS NULL OR progress < 0");
    execute(connection, "UPDATE tasks SET progress = 100 WHERE progress > 100");
    execute(connection, "UPDATE tasks SET status = UPPER(status) WHERE status IS NOT NULL");
    execute(connection, "UPDATE tasks SET status = 'NOT_STARTED' WHERE status IS NULL OR status NOT IN ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED')");
    execute(connection, "UPDATE tasks SET status = 'COMPLETED' WHERE progress = 100");
    execute(connection, "ALTER TABLE tasks MODIFY COLUMN start_date DATE NOT NULL");
    execute(connection, "ALTER TABLE tasks MODIFY COLUMN deadline DATE NOT NULL");
  }

  private void ensureColumn(Connection connection, String table, String column, String definition) throws SQLException {
    if (!hasColumn(connection, table, column)) {
      execute(connection, "ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
    }
  }

  private void ensureIndex(Connection connection, String table, String index, String sql) throws SQLException {
    try (ResultSet result = connection.getMetaData().getIndexInfo(null, null, table, false, false)) {
      while (result.next()) {
        if (index.equalsIgnoreCase(result.getString("INDEX_NAME"))) {
          return;
        }
      }
    }
    execute(connection, sql);
  }

  private boolean tableExists(Connection connection, String table) throws SQLException {
    try (ResultSet result = connection.getMetaData().getTables(null, null, table, new String[]{"TABLE"})) {
      return result.next();
    }
  }

  private boolean hasColumn(Connection connection, String table, String column) throws SQLException {
    DatabaseMetaData metadata = connection.getMetaData();
    try (ResultSet result = metadata.getColumns(null, null, table, "%")) {
      while (result.next()) {
        if (column.equalsIgnoreCase(result.getString("COLUMN_NAME"))) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean isAutoIncrement(Connection connection, String table, String column) throws SQLException {
    try (ResultSet result = connection.getMetaData().getColumns(null, null, table, "%")) {
      while (result.next()) {
        if (column.equalsIgnoreCase(result.getString("COLUMN_NAME"))) {
          return "YES".equalsIgnoreCase(result.getString("IS_AUTOINCREMENT"));
        }
      }
    }
    return false;
  }

  private void execute(Connection connection, String sql) throws SQLException {
    try (Statement statement = connection.createStatement()) {
      statement.execute(sql);
    }
  }
}
