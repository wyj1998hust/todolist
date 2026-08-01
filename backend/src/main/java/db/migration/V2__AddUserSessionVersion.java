package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class V2__AddUserSessionVersion extends BaseJavaMigration {
  @Override
  public void migrate(Context context) throws Exception {
    Connection connection = context.getConnection();
    if (!hasColumn(connection, "users", "session_version")) {
      try (Statement statement = connection.createStatement()) {
        statement.execute("ALTER TABLE users ADD COLUMN session_version BIGINT NOT NULL DEFAULT 0");
      }
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
}
