/*
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2014 IRSTV (FR CNRS 2488)
 *
 * h2patial is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * h2spatial is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * h2spatial. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.h2gis.drivers.dbf;

import org.h2.util.StringUtils;
import org.h2gis.drivers.dbf.internal.DBFDriver;
import org.h2gis.drivers.file_table.H2TableIndex;
import org.h2gis.drivers.shp.SHPEngineTest;
import org.h2gis.h2spatial.CreateSpatialExtension;
import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.h2gis.h2spatialapi.DriverFunction;
import org.h2gis.h2spatialapi.EmptyProgressVisitor;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Nicolas Fortin
 */
public class DBFImportExportTest {
    private static Connection connection;
    private static final String DB_NAME = "DBFImportExportTest";

    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = SpatialH2UT.createSpatialDataBase(DB_NAME);
        CreateSpatialExtension.registerFunction(connection.createStatement(), new DBFRead(), "");
        CreateSpatialExtension.registerFunction(connection.createStatement(), new DBFWrite(), "");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void exportTableTestGeomEnd() throws SQLException, IOException {
        Statement stat = connection.createStatement();
        File dbfFile = new File("target/area_export.dbf");
        stat.execute("DROP TABLE IF EXISTS AREA");
        stat.execute("create table area(idarea int primary key, value DOUBLE, descr CHAR(50))");
        stat.execute("insert into area values(1, 4.9406564584124654, 'main area')");
        stat.execute("insert into area values(2, 2.2250738585072009, 'second area')");
        // Create a shape file using table area
        stat.execute("CALL DBFWrite('target/area_export.dbf', 'AREA')");
        // Read this shape file to check values
        assertTrue(dbfFile.exists());
        DBFDriver dbfDriver = new DBFDriver();
        dbfDriver.initDriverFromFile(dbfFile);
        assertEquals(3, dbfDriver.getFieldCount());
        assertEquals(2, dbfDriver.getRowCount());
        Object[] row = dbfDriver.getRow(0);
        assertEquals(1, row[0]);
        assertEquals(4.9406564584124654, (Double) row[1], 1e-12);
        assertEquals("main area", row[2]);
        row = dbfDriver.getRow(1);
        assertEquals(2, row[0]);
        assertEquals(2.2250738585072009, (Double) row[1], 1e-12);
        assertEquals("second area", row[2]);
    }

    @Test
    public void importTableTestGeomEnd() throws SQLException, IOException {
        Statement st = connection.createStatement();
        final String path = SHPEngineTest.class.getResource("waternetwork.dbf").getPath();
        DriverFunction driver = new DBFDriverFunction();
        st.execute("DROP TABLE IF EXISTS waternetwork");
        driver.importFile(connection, "WATERNETWORK", new File(path), new EmptyProgressVisitor());
        // Query declared Table columns
        ResultSet rs = st.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'WATERNETWORK'");
        assertTrue(rs.next());
        assertEquals(H2TableIndex.PK_COLUMN_NAME,rs.getString("COLUMN_NAME"));
        assertEquals("INTEGER", rs.getString("TYPE_NAME"));
        assertTrue(rs.next());
        assertEquals("TYPE_AXE",rs.getString("COLUMN_NAME"));
        assertEquals("VARCHAR", rs.getString("TYPE_NAME"));
        assertEquals(254, rs.getInt("CHARACTER_MAXIMUM_LENGTH"));
        assertTrue(rs.next());
        assertEquals("GID",rs.getString("COLUMN_NAME"));
        assertEquals("BIGINT", rs.getString("TYPE_NAME"));
        assertTrue(rs.next());
        assertEquals("LENGTH",rs.getString("COLUMN_NAME"));
        assertEquals("DOUBLE",rs.getString("TYPE_NAME"));
        rs.close();
        // Check content
        rs = st.executeQuery("SELECT * FROM WATERNETWORK");
        assertTrue(rs.next());
        assertEquals("river",rs.getString("type_axe"));
        assertEquals(9.492402903934545, rs.getDouble("length"), 1e-12);
        assertEquals(1, rs.getInt("GID"));
        assertTrue(rs.next());
        assertEquals("ditch", rs.getString("type_axe"));
        assertEquals(261.62989135452983, rs.getDouble("length"), 1e-12);
        assertEquals(2, rs.getInt("GID"));
        rs.close();
        // Computation
        rs = st.executeQuery("SELECT SUM(length) sumlen FROM WATERNETWORK");
        assertTrue(rs.next());
        assertEquals(28469.778049948833, rs.getDouble(1), 1e-12);
        rs.close();
        st.execute("drop table WATERNETWORK");
    }

    /**
     * Read a DBF where the encoding is missing in header.
     * @throws SQLException
     */
    @Test
    public void readDBFRussianEncodingTest() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("drop table if exists sotchi");
        st.execute("CALL DBFREAD("+ StringUtils.quoteStringSQL(DBFEngineTest.class.getResource("sotchi.dbf").getPath())+", 'SOTCHI', 'cp1251');");
        // Query declared Table columns
        ResultSet rs = st.executeQuery("SELECT * FROM sotchi");
        // Check if fields name are OK
        ResultSetMetaData meta = rs.getMetaData();
        assertEquals("B_ДНА",meta.getColumnName(5));
        assertEquals("ИМЕНА_УЧАС",meta.getColumnName(8));
        assertEquals("ДЛИНА_КАНА",meta.getColumnName(9));
        assertEquals("ДЛИНА_КАН_",meta.getColumnName(10));
        assertEquals("ИМЯ_МУООС",meta.getColumnName(11));
        assertTrue(rs.next());
        assertEquals("ВП-2", rs.getString("NAMESHEME"));
        assertEquals("Дубовский канал",rs.getString("NAME10000"));
        assertTrue(rs.next());
        assertEquals("ВП-2-кр1-2", rs.getString("NAMESHEME"));
        assertTrue(rs.next());
        assertEquals("ВП-1", rs.getString("NAMESHEME"));
        assertTrue(rs.next());
        assertEquals("ВП-2-кр1-4", rs.getString("NAMESHEME"));
        assertTrue(rs.next());
        assertEquals("ВП-2-кр1-4-8", rs.getString("NAMESHEME"));
        assertFalse(rs.next());
        rs.close();
        st.execute("drop table sotchi");
    }

    @Test
    public void testPkDuplicate() throws SQLException, IOException {
        Statement stat = connection.createStatement();
        File dbfFile = new File("target/area_export.dbf");
        stat.execute("DROP TABLE IF EXISTS AREA, AREA2");
        stat.execute("create table area("+H2TableIndex.PK_COLUMN_NAME+" serial, value DOUBLE, descr CHAR(50))");
        stat.execute("insert into area values(null, 4.9406564584124654, 'main area')");
        stat.execute("insert into area values(null, 2.2250738585072009, 'second area')");
        // Create a shape file using table area
        stat.execute("CALL DBFWrite('"+dbfFile.getPath()+"', 'AREA')");
        // Read this shape file to check values
        stat.execute("CALL DBFRead('"+dbfFile.getPath()+"', 'AREA2')");
        ResultSet rs = stat.executeQuery("SELECT * FROM AREA2");
        assertEquals(H2TableIndex.PK_COLUMN_NAME+"2", rs.getMetaData().getColumnName(1));
        assertEquals(H2TableIndex.PK_COLUMN_NAME, rs.getMetaData().getColumnName(2));
    }
}
