package cn.oyzh.fx.db;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Raw JDBC result sets and their owning statements.
 */
public final class DBRawQueryResults implements AutoCloseable {

    private final List<Statement> statements = new ArrayList<>();

    private final List<ResultSet> resultSets = new ArrayList<>();

    public void add(Statement statement, ResultSet resultSet) {
        if (statement != null) {
            this.statements.add(statement);
        }
        if (resultSet != null) {
            this.resultSets.add(resultSet);
        }
    }

    public List<ResultSet> getResultSets() {
        return Collections.unmodifiableList(this.resultSets);
    }

    @Override
    public void close() {
        for (ResultSet resultSet : this.resultSets) {
            try {
                resultSet.close();
            } catch (Exception ignored) {
            }
        }
        this.resultSets.clear();
        for (Statement statement : this.statements) {
            try {
                statement.close();
            } catch (Exception ignored) {
            }
        }
        this.statements.clear();
    }
}
