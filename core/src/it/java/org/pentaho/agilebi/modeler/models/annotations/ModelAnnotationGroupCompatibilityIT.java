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

import org.junit.Before;
import org.junit.Test;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.stores.xml.XmlMetaStore;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Rowell Belen
 */
public class ModelAnnotationGroupCompatibilityIT {

  private static String XML_METASTORE = "src/it/resources/metastore_test";

  private IMetaStore metaStore = null;
  private ModelAnnotationManager modelAnnotationManager = null;

  @Before
  public void before() throws IOException, MetaStoreException {
    metaStore = new XmlMetaStore( XML_METASTORE );
    modelAnnotationManager = new ModelAnnotationManager();
  }

  @Test
  public void testLoadMyCategory() throws Exception {
    ModelAnnotationGroup group = modelAnnotationManager.readGroup( "My Category", this.metaStore );
    assertNotNull( group );
    assertEquals( group.size(), 100 );
    for ( ModelAnnotation m : group ) {
      assertNotNull( m );
      assertNotNull( m.getName() );
      assertNotNull( m.getField() );
      assertNotNull( m.getAnnotation() );
      assertNotNull( m.getAnnotation().getName() );
    }
  }

  @Test
  public void testLoadMyGroup() throws Exception {
    ModelAnnotationGroup group = modelAnnotationManager.readGroup( "My Group", this.metaStore );
    assertNotNull( group );
    assertEquals( group.size(), 1 );

    ModelAnnotation m = group.get( 0 );
    assertNotNull( m );
    assertNotNull( m.getName() );
    assertEquals( m.getName(), "4aae4455-5d06-4b13-a14c-f51a5a78a220" );
    assertNotNull( m.getField() );
    assertEquals( m.getField(), "f1" );

    CreateDimensionKey a = (CreateDimensionKey) m.getAnnotation();
    assertNotNull( a );
    assertNotNull( a.getName() );
    assertEquals( a.getName(), "f1" );
    assertNotNull( a.getDimension() );
    assertEquals( a.getDimension(), "1dim" );
  }

  @Test
  public void testLoadDimensionTeam() throws Exception {
    ModelAnnotationGroup group = modelAnnotationManager.readGroup( "Dimension Team", this.metaStore );
    assertNotNull( group );
    assertEquals( "Dimension Team", group.getName() );
    assertEquals( 4, group.size() );

    ModelAnnotation m1 = group.get( 0 );
    assertEquals( 2, m1.describeAnnotation().size() );
    assertNotNull( m1.getName() );
    assertEquals( "c2dc8659-8c28-45c5-b5ab-d2f69c4f531b", m1.getName() );
    assertEquals( "Id", m1.getField() );

    LinkDimension a1 = (LinkDimension) m1.getAnnotation();
    assertNotNull( a1 );
    assertEquals( "Dim Team", a1.getName() );
    assertEquals( "Teams", a1.getSharedDimension() );

    ModelAnnotation m2 = group.get( 1 );
    assertNotNull( m2.getName() );
    assertEquals( 4, m2.describeAnnotation().size() );
    assertEquals( "b4e7ef92-ad05-4e18-8850-7914684f63be", m2.getName() );
    assertEquals( "Home_Team", m2.getField() );

    CreateMeasure a2 = (CreateMeasure) m2.getAnnotation();
    assertNotNull( a2 );
    assertEquals( "HT_COUNT", a2.getName() );
    assertEquals( "Total of Home Team", a2.getDescription() );

    ModelAnnotation m3 = group.get( 2 );
    assertNotNull( m3.getName() );
    assertEquals( 4, m3.describeAnnotation().size() );
    assertEquals( "3dbe378e-4baf-4e5d-a02c-fe7f6b2f1832", m3.getName() );
    assertEquals( "AwayTeam", m3.getField() );

    CreateMeasure a3 = (CreateMeasure) m3.getAnnotation();
    assertNotNull( a3 );
    assertEquals( "AT_COUNT", a3.getName() );
    assertEquals( "Total of Away Team", a3.getDescription() );

    ModelAnnotation m4 = group.get( 3 );
    assertNotNull( m4.getName() );
    assertEquals( 4, m4.describeAnnotation().size() );
    assertEquals( "22a30b7c-9dc5-4be5-8ec8-24b863505b81", m4.getName() );
    assertEquals( "Home_Team", m4.getField() );

    CreateAttribute a4 = (CreateAttribute) m4.getAnnotation();
    assertNotNull( a4 );
    assertEquals( "MY_HOME_TEAM", a4.getName() );
    assertEquals( "The DIM Teams", a4.getDimension() );
  }

  @Test
  public void testLoadCustomerAndPrice() throws Exception {
    ModelAnnotationGroup group = modelAnnotationManager.readGroup( "Annotations_Of_CustomerAndPrice", this.metaStore );
    assertNotNull( group );
    assertEquals( "Annotations_Of_CustomerAndPrice", group.getName() );
    assertEquals( 4, group.size() );

    ModelAnnotation m1 = group.get( 0 );
    assertEquals( "CUSTOMERNAME", m1.getField() );
    assertEquals( 2, m1.describeAnnotation().size() );
    assertEquals( "SalesCustomerDimension", m1.describeAnnotation().get( "sharedDimension" ) );

    ModelAnnotation m2 = group.get( 1 );
    assertEquals( "QUANTITYORDERED", m2.getField() );
    assertEquals( 5, m2.describeAnnotation().size() );
    assertEquals( "Orders", m2.describeAnnotation().get( "hierarchy" ) );

    ModelAnnotation m3 = group.get( 2 );
    assertEquals( "PRICEEACH", m3.getField() );
    assertEquals( 4, m3.describeAnnotation().size() );
    assertEquals( "$#,###;($#,###)", m3.describeAnnotation().get( "formatString" ) );

    ModelAnnotation m4 = group.get( 3 );
    assertEquals( "PRICEEACH", m4.getField() );
    assertEquals( 4, m4.describeAnnotation().size() );
    assertEquals( AggregationType.SUM, m4.describeAnnotation().get( "aggregateType" ) );
  }
}
