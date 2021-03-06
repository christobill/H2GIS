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

package org.h2gis.utilities;

import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import org.junit.Test;
import static junit.framework.Assert.assertEquals;

/**
 * Test SFSUtilities
 *
 * @author Nicolas Fortin
 */
public class SFSUtilitiesTest {

    @Test
    public void testGeometryTypeConvert() throws ParseException {
        WKTReader wktReader = new WKTReader();
        assertEquals(GeometryTypeCodes.POINT, SFSUtilities.getGeometryTypeFromGeometry(wktReader.read("POINT(1 1)")));
        assertEquals(GeometryTypeCodes.LINESTRING, SFSUtilities.getGeometryTypeFromGeometry(wktReader.read("LINESTRING(1 1, 2 2)")));
        assertEquals(GeometryTypeCodes.POLYGON, SFSUtilities.getGeometryTypeFromGeometry(wktReader.read("POLYGON((1 1, 1 2, 2 2, 2 1, 1 1))")));
        assertEquals(GeometryTypeCodes.MULTIPOINT, SFSUtilities.getGeometryTypeFromGeometry(wktReader.read("MULTIPOINT((1 1))")));
        assertEquals(GeometryTypeCodes.MULTILINESTRING, SFSUtilities.getGeometryTypeFromGeometry(wktReader.read("MULTILINESTRING((1 1, 2 2))")));
        assertEquals(GeometryTypeCodes.MULTIPOLYGON, SFSUtilities.getGeometryTypeFromGeometry(wktReader.read("MULTIPOLYGON(((1 1, 1 2, 2 2, 2 1, 1 1)))")));
        assertEquals(GeometryTypeCodes.GEOMCOLLECTION, SFSUtilities.getGeometryTypeFromGeometry(wktReader.read("GEOMETRYCOLLECTION(POINT(1 1))")));
    }
}
