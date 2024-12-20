/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.agilebi.modeler.strategy;

import static junit.framework.Assert.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.agilebi.modeler.AbstractModelerTest;
import org.pentaho.agilebi.modeler.geo.GeoContext;
import org.pentaho.agilebi.modeler.geo.GeoContextConfigProvider;
import org.pentaho.agilebi.modeler.geo.GeoContextFactory;
import org.pentaho.agilebi.modeler.geo.GeoContextPropertiesProvider;
import org.pentaho.agilebi.modeler.geo.GeoRole;
import org.pentaho.agilebi.modeler.nodes.AvailableField;
import org.pentaho.agilebi.modeler.nodes.AvailableTable;
import org.pentaho.agilebi.modeler.nodes.CategoryMetaData;
import org.pentaho.agilebi.modeler.nodes.DimensionMetaData;
import org.pentaho.agilebi.modeler.nodes.DimensionMetaDataCollection;
import org.pentaho.agilebi.modeler.nodes.HierarchyMetaData;
import org.pentaho.agilebi.modeler.nodes.LevelMetaData;
import org.pentaho.agilebi.modeler.nodes.MainModelNode;
import org.pentaho.agilebi.modeler.nodes.RelationalModelNode;
import org.pentaho.metadata.model.concept.types.DataType;

/**
 * Created: 4/19/11
 *
 * @author rfellows
 */
public class SimpleAutoModelStrategyIT extends AbstractModelerTest {

  private static final String LOCALE = "en-US";

  private static Properties props = null;
  private static GeoContextConfigProvider config;

  @BeforeClass
  public static void bootstrap() throws IOException {
    Reader propsReader = new FileReader( new File( "src/it/resources/geoRoles.properties" ) );
    props = new Properties();
    props.load( propsReader );
    config = new GeoContextPropertiesProvider( props );
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    super.generateTestDomain();
  }

  @Test
  public void testAutoModelOlap() throws Exception {
    SimpleAutoModelStrategy strategy = new SimpleAutoModelStrategy( LOCALE );

    strategy.autoModelOlap( workspace, new MainModelNode() );

    int columnCount = 0;
    HashSet<String> columns = new HashSet<String>();
    HashSet<String> numericColumns = new HashSet<String>();
    for ( AvailableTable table : workspace.getAvailableTables().getAsAvailableTablesList() ) {
      columnCount += table.size();
      for ( AvailableField field : table.getAvailableFields() ) {
        if ( !columns.contains( field.getName() ) ) {
          columns.add( field.getName() );
        }
        if ( field.getPhysicalColumn().getDataType() == DataType.NUMERIC
            && !numericColumns.contains( field.getName() ) ) {
          numericColumns.add( field.getName() );
        }
      }
    }

    assertEquals( columns.size(), workspace.getModel().getDimensions().size() );
    assertEquals( numericColumns.size(), workspace.getModel().getMeasures().size() );

  }

  @Test
  public void testAutoGeoModel() throws Exception {
    GeoContext geo = GeoContextFactory.create( config );
    SimpleAutoModelStrategy strategy = new SimpleAutoModelStrategy( LOCALE, geo );
    assertNotNull( strategy.getGeoContext() );
    strategy.autoModelOlap( workspace, new MainModelNode() );
    DimensionMetaDataCollection dims = workspace.getModel().getDimensions();

    int geoDims = 0;
    int levelCount = 0;
    String previousDim = null;
    for ( DimensionMetaData dim : dims ) {
      if ( dim.getMemberAnnotations().get( GeoContext.ANNOTATION_DATA_ROLE ) instanceof GeoRole ) {
        // there should only be one hierarchy in a geo dimension
        assertEquals( 1, dim.size() );
        geoDims++;
      }

      for ( HierarchyMetaData hier : dim ) {
        for ( LevelMetaData level : hier ) {
          levelCount++;
        }
      }

      // make sure the dimensions are in alphabetical order
      if ( previousDim != null && dim.getDisplayName() != null ) {
        assertTrue( previousDim.compareToIgnoreCase( dim.getDisplayName() ) <= 0 );
        System.out.println( dim.getDisplayName() );
      }
      previousDim = dim.getDisplayName();
    }

    int expectedLevelCount = 0;
    for ( AvailableField field : workspace.getAvailableTables().getAsAvailableTablesList().get( 0 )
        .getAvailableFields() ) {
      expectedLevelCount++;
    }
    // make sure there are no duplicate fields between the 2 dimensions
    assertEquals( expectedLevelCount, levelCount );

    assertTrue( "No geo dimensions found", geoDims == 1 );

  }

  @Test
  public void testAutoModelRelational() throws Exception {
    SimpleAutoModelStrategy strategy = new SimpleAutoModelStrategy( LOCALE );

    strategy.autoModelRelational( workspace, new RelationalModelNode() );

    // one category per table
    // one field per table.column

    HashMap<String, Integer> tables = new HashMap<String, Integer>();
    for ( AvailableTable table : workspace.getAvailableTables().getAsAvailableTablesList() ) {
      tables.put( table.getName(), table.getAvailableFields().size() );
    }

    assertEquals( tables.size(), workspace.getRelationalModel().getCategories().size() );
    for ( CategoryMetaData cat : workspace.getRelationalModel().getCategories() ) {
      assertTrue( tables.containsKey( cat.getName() ) );
      assertEquals( tables.get( cat.getName() ).intValue(), cat.size() );
    }

  }
}
