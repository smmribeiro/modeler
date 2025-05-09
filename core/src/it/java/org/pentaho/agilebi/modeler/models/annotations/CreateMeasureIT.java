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

package org.pentaho.agilebi.modeler.models.annotations;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.agilebi.modeler.ModelerPerspective;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.models.annotations.util.ModelITHelper;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metadata.model.olap.OlapCube;
import org.pentaho.metastore.stores.memory.MemoryMetaStore;

import java.util.List;

import static org.junit.Assert.*;

public class CreateMeasureIT {
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    KettleClientEnvironment.init();
    PluginRegistry.addPluginType( StepPluginType.getInstance() );
    PluginRegistry.init();
    if ( !Props.isInitialized() ) {
      Props.init( 0 );
    }
  }

  @Test
  public void createMeasureWorksOnDimensionlessModel() throws Exception {
    ModelerWorkspace workspace = createWorkspace();

    CreateMeasure valueMeasure2 = new CreateMeasure();
    valueMeasure2.setField( "value" );
    valueMeasure2.setName( "value" );
    valueMeasure2.setAggregateType( AggregationType.SUM );
    valueMeasure2.apply( workspace, new MemoryMetaStore() );

    CreateMeasure valueMeasure = new CreateMeasure();
    valueMeasure.setField( "value" );
    valueMeasure.setName( "The Value" );
    valueMeasure.setAggregateType( AggregationType.SUM );
    valueMeasure.apply( workspace, new MemoryMetaStore() );

    CreateMeasure idMeasure = new CreateMeasure();
    idMeasure.setField( "id" );
    idMeasure.setName( "Id Count" );
    idMeasure.setAggregateType( AggregationType.COUNT );
    idMeasure.apply( workspace, new MemoryMetaStore() );
    OlapCube olapCube = getCubes( workspace ).get( 0 );
    assertEquals( 3, olapCube.getOlapMeasures().size() );
    assertEquals( 3, workspace.getModel().getMeasures().size() );
    assertEquals( 0, olapCube.getOlapDimensionUsages().size() );
    assertEquals( "The Value", olapCube.getOlapMeasures().get( 1 ).getName() );
    assertEquals( "Id Count", olapCube.getOlapMeasures().get( 2 ).getName() );
  }

  @SuppressWarnings( "unchecked" )
  private List<OlapCube> getCubes( ModelerWorkspace wspace ) {
    return (List<OlapCube>) wspace.getLogicalModel( ModelerPerspective.ANALYSIS ).getProperty(
        LogicalModel.PROPERTY_OLAP_CUBES );
  }

  private ModelerWorkspace createWorkspace() throws Exception {
    String sql =
        "DROP TABLE IF EXISTS testtable; CREATE TABLE testTable\n" + "(\n" + "  value bigint\n" + ", id varchar(25)\n"
            + ");\n";
    return ModelITHelper.modelTable( "CreateMeasureIT-H2-DB", "testTable", sql );
  }
}
