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

package org.pentaho.di.core.refinery.model;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.io.FileUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.agilebi.modeler.models.annotations.CreateAttribute;
import org.pentaho.agilebi.modeler.models.annotations.CreateMeasure;
import org.pentaho.agilebi.modeler.models.annotations.LinkDimension;
import org.pentaho.agilebi.modeler.models.annotations.ModelAnnotation;
import org.pentaho.agilebi.modeler.models.annotations.ModelAnnotationGroup;
import org.pentaho.agilebi.modeler.models.annotations.ModelAnnotationManager;
import org.pentaho.agilebi.modeler.models.annotations.SharedDimensionGroup;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.stores.xml.XmlMetaStore;
import org.pentaho.metastore.util.PentahoDefaults;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * @author Rowell Belen
 */
public class ModelAnnotationManagerIT {

  private String tempDir = null;
  private IMetaStore metaStore = null;
  private ModelAnnotationManager modelAnnotationManager = null;
  private ModelAnnotationManager sharedDimensionManager = null;

  @Before
  public void before() throws Exception {
    File f = File.createTempFile( "ModelAnnotationManageTest", "before" );
    f.deleteOnExit();

    tempDir = f.getParent();
    metaStore = new XmlMetaStore( tempDir );
    modelAnnotationManager = new ModelAnnotationManager();
    sharedDimensionManager = new ModelAnnotationManager( true );
  }

  @After
  public void after() throws Exception {
    FileUtils.deleteDirectory( new File( ( (XmlMetaStore) metaStore ).getRootFolder() ) );
  }

  @Test
  public void testCreateModelAnnotationGroup() throws Exception {

    CreateAttribute ca = new CreateAttribute();
    ca.setField( "country" );
    ca.setGeoType( ModelAnnotation.GeoType.Country );
    ModelAnnotation<CreateAttribute> m1 = new ModelAnnotation<CreateAttribute>( ca );

    LinkDimension ld = new LinkDimension();
    ld.setField( "country" );
    ld.setSharedDimension( "Geo Dimension" );
    ModelAnnotation<LinkDimension> m2 = new ModelAnnotation<LinkDimension>( ld );

    ModelAnnotationGroup group = new ModelAnnotationGroup();
    group.setName( "My Category" );
    group.add( m1 );
    group.add( m2 );

    modelAnnotationManager.createGroup( group, this.metaStore );
    assertNotNull( modelAnnotationManager.readGroup( group.getName(), this.metaStore ) );
    assertNotNull(
      modelAnnotationManager.readGroup( group.getName(), this.metaStore ).get( 0 ).getAnnotation().getType() );

    SharedDimensionGroup sGroup = new SharedDimensionGroup();
    sGroup.setName( "Shared Dimension Group" );
    sGroup.add( m1 );
    sGroup.add( m2 );

    sharedDimensionManager.createGroup( sGroup, this.metaStore ); // able to save even with the same group name

    ModelAnnotationGroup sharedDimensionGroup = sharedDimensionManager.readGroup( sGroup.getName(), this.metaStore );
    assertEquals( "Shared Dimension Group", sharedDimensionGroup.getName() );
    assertTrue( sharedDimensionGroup instanceof SharedDimensionGroup );
    assertEquals( 2, sharedDimensionGroup.size() );
    assertNotNull( sharedDimensionGroup.get( 0 ).getAnnotation().getType() );
    assertNotNull( sharedDimensionGroup.get( 1 ).getAnnotation().getType() );
    assertEquals( ModelAnnotation.Type.CREATE_ATTRIBUTE, sharedDimensionGroup.get( 0 ).getAnnotation().getType() );
    assertEquals( ModelAnnotation.Type.LINK_DIMENSION, sharedDimensionGroup.get( 1 ).getAnnotation().getType() );

    CreateAttribute createAttribute = (CreateAttribute) sharedDimensionGroup.get( 0 ).getAnnotation();
    assertEquals( "country", createAttribute.getField() );

    LinkDimension linkDimension = (LinkDimension) sharedDimensionGroup.get( 1 ).getAnnotation();
    assertEquals( "country", linkDimension.getField() );
    assertEquals( "Geo Dimension", linkDimension.getSharedDimension() );
  }

  @Test
  public void testListGroups() throws Exception {

    final ModelAnnotationGroup group = new ModelAnnotationGroup();
    group.setName( "Sales Category" );

    CreateMeasure cm = new CreateMeasure();
    cm.setAggregateType( AggregationType.COUNT_DISTINCT );

    final ModelAnnotation<?> mcm = new ModelAnnotation<CreateMeasure>( cm );
    group.add( mcm );

    CreateAttribute ca = new CreateAttribute();
    ca.setGeoType( ModelAnnotation.GeoType.Country );

    final ModelAnnotation<?> mca = new ModelAnnotation<CreateAttribute>( ca );
    group.add( mca );

    // add annotations
    modelAnnotationManager.createGroup( group, this.metaStore );

    List<String> names = modelAnnotationManager.listGroupNames( this.metaStore );

    assertTrue( CollectionUtils.exists( names, new Predicate() {
      @Override public boolean evaluate( Object o ) {

        String name = (String) o;
        // check mcm object
        return name.equals( group.getName() );
      }
    } ) );

    assertEquals( 1, modelAnnotationManager.listGroups( this.metaStore ).size() );
  }

  @Test
  public void testListSharedDimensionGroups() throws Exception {

    final SharedDimensionGroup group = createSampleSharedDimensionGroup();

    // add annotations
    sharedDimensionManager.createGroup( group, this.metaStore );

    List<String> names = sharedDimensionManager.listGroupNames( this.metaStore );

    assertTrue( CollectionUtils.exists( names, new Predicate() {
      @Override public boolean evaluate( Object o ) {

        String name = (String) o;
        // check mcm object
        return name.equals( group.getName() );
      }
    } ) );

    assertEquals( 1, sharedDimensionManager.listGroups( this.metaStore ).size() );
  }

  @Test
  public void testContainsGroup() throws Exception {

    ModelAnnotationGroup group = new ModelAnnotationGroup();
    group.setName( "My Group" );

    CreateMeasure cm = new CreateMeasure();
    cm.setAggregateType( AggregationType.COUNT_DISTINCT );

    final ModelAnnotation<?> mcm = new ModelAnnotation<CreateMeasure>( cm );
    group.add( mcm );

    CreateAttribute ca = new CreateAttribute();
    ca.setGeoType( ModelAnnotation.GeoType.Country );

    final ModelAnnotation<?> mca = new ModelAnnotation<>( ca );
    group.add( mca );

    // add annotations
    modelAnnotationManager.createGroup( group, this.metaStore );

    assertFalse( modelAnnotationManager.containsGroup( "null", this.metaStore ) );
    assertTrue( modelAnnotationManager.containsGroup( group.getName(), this.metaStore ) );

    assertFalse( sharedDimensionManager.containsGroup( group.getName(), this.metaStore ) );
  }

  @Test
  public void testContainsSharedDimensionGroup() throws Exception {

    SharedDimensionGroup group = createSampleSharedDimensionGroup();

    // add annotations
    sharedDimensionManager.createGroup( group, this.metaStore );

    assertFalse( sharedDimensionManager.containsGroup( "null", this.metaStore ) );
    assertTrue( sharedDimensionManager.containsGroup( group.getName(), this.metaStore ) );
    assertFalse( modelAnnotationManager.containsGroup( group.getName(), this.metaStore ) );
  }

  @Test
  public void testDeleteGroups() throws Exception {

    ModelAnnotationGroup group = new ModelAnnotationGroup();
    group.setName( "Inventory Category" );

    CreateMeasure cm = new CreateMeasure();
    cm.setAggregateType( AggregationType.COUNT_DISTINCT );

    final ModelAnnotation<?> mcm = new ModelAnnotation<>( cm );
    group.add( mcm );

    CreateAttribute ca = new CreateAttribute();
    ca.setGeoType( ModelAnnotation.GeoType.Country );

    final ModelAnnotation<?> mca = new ModelAnnotation<>( ca );
    group.add( mca );

    // add annotations
    modelAnnotationManager.createGroup( group, this.metaStore );
    assertEquals( 1, modelAnnotationManager.listGroupNames( this.metaStore ).size() );

    modelAnnotationManager.deleteGroup( group.getName(), this.metaStore );
    assertEquals( 0, modelAnnotationManager.listGroupNames( this.metaStore ).size() );

    // add more
    group.addAll( createTestGroup() );
    modelAnnotationManager.createGroup( group, this.metaStore );
    assertEquals( 1, modelAnnotationManager.listGroupNames( this.metaStore ).size() );

    modelAnnotationManager.deleteAllGroups( this.metaStore );
    assertEquals( 0, modelAnnotationManager.listGroupNames( this.metaStore ).size() );
  }

  @Test
  public void testDeleteSharedDimensionGroups() throws Exception {

    SharedDimensionGroup group = createSampleSharedDimensionGroup();

    // add annotations
    sharedDimensionManager.createGroup( group, this.metaStore );
    assertEquals( 1, sharedDimensionManager.listGroupNames( this.metaStore ).size() );

    sharedDimensionManager.deleteGroup( group.getName(), this.metaStore );
    assertEquals( 0, sharedDimensionManager.listGroupNames( this.metaStore ).size() );

    // add more
    group.addAll( createTestGroup() );
    sharedDimensionManager.createGroup( group, this.metaStore );
    assertEquals( 1, sharedDimensionManager.listGroupNames( this.metaStore ).size() );

    sharedDimensionManager.deleteAllGroups( this.metaStore );
    assertEquals( 0, sharedDimensionManager.listGroupNames( this.metaStore ).size() );
  }

  private ModelAnnotationGroup createTestGroup() {

    ModelAnnotationGroup group = new ModelAnnotationGroup();
    group.setName( "Test Group" );

    CreateMeasure cm = new CreateMeasure();
    cm.setAggregateType( AggregationType.COUNT_DISTINCT );

    final ModelAnnotation<?> mcm = new ModelAnnotation<>( cm );
    group.add( mcm );

    CreateAttribute ca = new CreateAttribute();
    ca.setGeoType( ModelAnnotation.GeoType.Country );

    final ModelAnnotation<?> mca = new ModelAnnotation<>( ca );
    group.add( mca );

    return group;
  }

  private SharedDimensionGroup createSampleSharedDimensionGroup() {

    final SharedDimensionGroup group = new SharedDimensionGroup();
    group.setName( "Shared Dimension" );

    LinkDimension ld = new LinkDimension();
    ld.setField( "country" );
    ld.setSharedDimension( "Geo Dimension" );
    ModelAnnotation<LinkDimension> mld = new ModelAnnotation<>( ld );

    group.add( mld );

    CreateAttribute ca = new CreateAttribute();
    ca.setGeoType( ModelAnnotation.GeoType.Country );

    final ModelAnnotation<?> mca = new ModelAnnotation<>( ca );
    group.add( mca );

    return group;
  }

  @Test
  public void testCreateEmptyAnnotationGroup() throws Exception {
    ModelAnnotationGroup group = new ModelAnnotationGroup();
    group.setName( "My Category" );

    modelAnnotationManager.createGroup( group, this.metaStore );

    assertEquals( 0, modelAnnotationManager.readGroup( group.getName(), this.metaStore ).size() );

    sharedDimensionManager.createGroup( group, this.metaStore );

    assertEquals( 0, sharedDimensionManager.readGroup( group.getName(), this.metaStore ).size() );
  }

  @Test
  public void testStoreLoadDbMetaNew() throws Exception {
    KettleClientEnvironment.init();
    DatabaseMeta
      dbMeta =
      new DatabaseMeta( "dbmetaTest", "postgresql", "Native", "somehost", "db", "3001", "user", "pass" );
    dbMeta.getAttributes().setProperty( "SUPPORTS_BOOLEAN_DATA_TYPE", "N" );
    final String dbRef = modelAnnotationManager.storeDatabaseMeta( dbMeta, this.metaStore );
    assertEquals( dbMeta.getName(), dbRef );
    IMetaStore spy = spy( this.metaStore );
    DatabaseMeta dbMetaBack = modelAnnotationManager.loadDatabaseMeta( dbRef, spy );
    assertEquals( dbMeta, dbMetaBack );
    dbMetaBack.setChangedDate( dbMeta.getChangedDate() );
    verify( spy ).getElementByName(      eq( PentahoDefaults.NAMESPACE ), any(), eq( dbRef ) );
    assertEquals( dbMeta.getAccessType(), dbMetaBack.getAccessType() );
    assertEquals( dbMeta.getAttributes(), dbMetaBack.getAttributes() );
    assertEquals( dbMeta.getHostname(), dbMetaBack.getHostname() );
    assertEquals( dbMeta.getPluginId(), dbMetaBack.getPluginId() );
    assertEquals( dbMeta.getUsername(), dbMetaBack.getUsername() );
    assertEquals( dbMeta.getPassword(), dbMetaBack.getPassword() );
    assertEquals( dbMeta.getDatabaseName(), dbMetaBack.getDatabaseName() );
    assertEquals( dbMeta.getURL(), dbMetaBack.getURL() );
  }

  private Matcher<IMetaStoreElementType> idNotNull() {
    return new BaseMatcher<>() {
      @Override public boolean matches( final Object item ) {
        IMetaStoreElementType elementType = (IMetaStoreElementType) item;
        return elementType.getId() != null;
      }

      @Override public void describeTo( final Description description ) {

      }
    };
  }

  @Test
  public void testStoreLoadDbMetaUpdate() throws Exception {
    KettleClientEnvironment.init();
    DatabaseMeta
      dbMetaPrevious =
      new DatabaseMeta( "dbmetaTest", "postgresql", "Native", "otherhost", "db", "3002", "user1", "pass1" );
    final String dbRefPrev = modelAnnotationManager.storeDatabaseMeta( dbMetaPrevious, this.metaStore );
    DatabaseMeta
      dbMeta =
      new DatabaseMeta( "dbmetaTest", "postgresql", "Native", "somehost", "db", "3001", "user", "pass" );
    dbMeta.getAttributes().setProperty( "SUPPORTS_BOOLEAN_DATA_TYPE", "N" );
    final String dbRef = modelAnnotationManager.storeDatabaseMeta( dbMeta, this.metaStore );
    assertEquals( dbMeta.getName(), dbRef );
    assertEquals( dbRef, dbRefPrev );
    DatabaseMeta dbMetaBack = modelAnnotationManager.loadDatabaseMeta( dbRef, this.metaStore );
    assertEquals( dbMeta, dbMetaBack );
    dbMetaBack.setChangedDate( dbMeta.getChangedDate() );
    assertEquals( dbMeta.getAccessType(), dbMetaBack.getAccessType() );
    assertEquals( dbMeta.getAttributes(), dbMetaBack.getAttributes() );
    assertEquals( dbMeta.getHostname(), dbMetaBack.getHostname() );
    assertEquals( dbMeta.getPluginId(), dbMetaBack.getPluginId() );
    assertEquals( dbMeta.getUsername(), dbMetaBack.getUsername() );
    assertEquals( dbMeta.getPassword(), dbMetaBack.getPassword() );
    assertEquals( dbMeta.getDatabaseName(), dbMetaBack.getDatabaseName() );
    assertEquals( dbMeta.getURL(), dbMetaBack.getURL() );
  }
}
