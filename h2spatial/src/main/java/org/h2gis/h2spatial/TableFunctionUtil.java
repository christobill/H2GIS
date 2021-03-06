package org.h2gis.h2spatial;

import org.h2.tools.SimpleResultSet;
import org.h2gis.utilities.TableLocation;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility in order to simplify table function usage
 *
 * @author Nicolas Fortin
 * @author Adam Gouge
 */
public class TableFunctionUtil {

   private TableFunctionUtil() {
       // This is a utility class.
   }

    /**
     * Copy fields from table into a {@link org.h2.tools.SimpleResultSet}
     * @param connection Active connection
     * @param rs Result set that will receive columns
     * @param tableLocation Import columns from this table
     * @throws SQLException Error
     */
    public static void copyFields(Connection connection, SimpleResultSet rs, TableLocation tableLocation) throws SQLException {
        DatabaseMetaData meta = connection.getMetaData();
        ResultSet columnsRs = meta.getColumns(tableLocation.getCatalog(null), tableLocation.getSchema(null),
                tableLocation.getTable(), null);
        Map<Integer, Object[]> columns = new HashMap<Integer, Object[]>();
        int COLUMN_NAME = 0, COLUMN_TYPE = 1, COLUMN_TYPENAME = 2, COLUMN_PRECISION = 3, COLUMN_SCALE = 4;
        try {
            while (columnsRs.next()) {
                Object[] columnInfoObjects = new Object[COLUMN_SCALE + 1];
                columnInfoObjects[COLUMN_NAME] = columnsRs.getString("COLUMN_NAME");
                columnInfoObjects[COLUMN_TYPE] = columnsRs.getInt("DATA_TYPE");
                columnInfoObjects[COLUMN_TYPENAME] = columnsRs.getString("TYPE_NAME");
                columnInfoObjects[COLUMN_PRECISION] = columnsRs.getInt("COLUMN_SIZE");
                columnInfoObjects[COLUMN_SCALE] = columnsRs.getInt("DECIMAL_DIGITS");
                columns.put(columnsRs.getInt("ORDINAL_POSITION"), columnInfoObjects);
            }
        } finally {
            columnsRs.close();
        }
        for(int i=1;i<=columns.size();i++) {
            Object[] columnInfoObjects = columns.get(i);
            rs.addColumn((String)columnInfoObjects[COLUMN_NAME], (Integer)columnInfoObjects[COLUMN_TYPE],
                    (String)columnInfoObjects[COLUMN_TYPENAME], (Integer)columnInfoObjects[COLUMN_PRECISION]
                    , (Integer)columnInfoObjects[COLUMN_SCALE]);
        }
    }

    /**
     * Return true if this connection only wants the list of columns.
     * This is a hack. See: https://groups.google.com/forum/#!topic/h2-database/NHH0rDeU258
     *
     * @param connection Connection
     * @return True if this connection only wants the list of columns
     * @throws java.sql.SQLException
     */
    public static boolean isColumnListConnection(Connection connection) throws SQLException {
        return connection.getMetaData().getURL().equals("jdbc:columnlist:connection");
    }
}
