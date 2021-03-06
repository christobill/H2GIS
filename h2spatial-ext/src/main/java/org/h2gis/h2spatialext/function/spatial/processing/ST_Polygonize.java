/**
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
 * or contact directly: info_at_orbisgis.org
 */

package org.h2gis.h2spatialext.function.spatial.processing;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.operation.polygonize.Polygonizer;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

import java.util.Collection;

/**
 *
 * @author Erwan Bocher
 */
public class ST_Polygonize extends DeterministicScalarFunction {

    private static final GeometryFactory FACTORY = new GeometryFactory();
    
    public ST_Polygonize(){
        addProperty(PROP_REMARKS, "Polygonizes a set of Geometry which contain linework "
                + "that represents the edges of a planar graph");
    }

    @Override
    public String getJavaStaticMethod() {
        return "polygonize";
    }

    /**
     * Creates a GeometryCollection containing possible polygons formed 
     * from the constituent linework of a set of geometries.
     * 
     * @param geometry
     * @return 
     */
    public static Geometry polygonize(Geometry geometry) {
        Polygonizer polygonizer = new Polygonizer();
        polygonizer.add(geometry);
        Collection pols = polygonizer.getPolygons();
        if(pols.isEmpty()){
            return null;
        }
        return FACTORY.createMultiPolygon(GeometryFactory.toPolygonArray(pols));
    }
}
