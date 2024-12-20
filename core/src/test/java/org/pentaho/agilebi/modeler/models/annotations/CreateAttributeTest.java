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

import org.junit.Test;
import org.pentaho.agilebi.modeler.nodes.HierarchyMetaData;
import org.pentaho.agilebi.modeler.nodes.LevelMetaData;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by rfellows on 6/8/16.
 */
public class CreateAttributeTest {

  private CreateAttribute buildCreateAttributeAnnotations( String name, String dimension, String hierarchy, String parent, String description, boolean isUnique ) {
    CreateAttribute ca = new CreateAttribute();
    ca.setName( name );
    ca.setDimension( dimension );
    ca.setHierarchy( hierarchy );
    ca.setParentAttribute( parent );
    ca.setUnique( isUnique );
    ca.setDescription( description );
    return ca;
  }

  @Test
  public void equalsLogically_caseInsensitive() throws Exception {
    CreateAttribute left = buildCreateAttributeAnnotations( "sales", "sales", "sales", null, null, false );
    CreateAttribute right = buildCreateAttributeAnnotations( "Sales", "Sales", "Sales", "ignore", "ignore", true );

    assertTrue( left.equalsLogically( right ) );
  }

  @Test
  public void equalsLogically_differentName() throws Exception {
    CreateAttribute left = buildCreateAttributeAnnotations( "total", "sales", "sales", null, null, false );
    CreateAttribute right = buildCreateAttributeAnnotations( "Sales", "Sales", "Sales", null, null, false );

    assertFalse( left.equalsLogically( right ) );
  }

  @Test
  public void equalsLogically_differentDimension() throws Exception {
    CreateAttribute left = buildCreateAttributeAnnotations( "sales", "Money", "sales", null, null, false );
    CreateAttribute right = buildCreateAttributeAnnotations( "Sales", "Sales", "Sales", null, null, false );

    assertFalse( left.equalsLogically( right ) );
  }

  @Test
  public void equalsLogically_differentHierarchy() throws Exception {
    CreateAttribute left = buildCreateAttributeAnnotations( "sales", "sales", "Money", null, null, false );
    CreateAttribute right = buildCreateAttributeAnnotations( "Sales", "Sales", "Sales", null, null, false );

    assertFalse( left.equalsLogically( right ) );
  }

  @Test
  public void equalsLogically_nulls() throws Exception {
    CreateAttribute left = buildCreateAttributeAnnotations( "sales", null, "sales", null, null, false );
    CreateAttribute right = buildCreateAttributeAnnotations( "Sales", null, "Sales", null, null, false );

    assertTrue( left.equalsLogically( right ) );

    left.setHierarchy( null );
    assertFalse( left.equalsLogically( right ) );

    assertFalse( left.equalsLogically( null ) );
  }

  @Test
  public void equalsLogically_UsingDefaultAssumptions() throws Exception {
    // can't assume dimension
    CreateAttribute left = buildCreateAttributeAnnotations( "sales", null, "sales", null, null, false );
    CreateAttribute right = buildCreateAttributeAnnotations( "sales", "sales", "sales", null, null, false );
    assertFalse( left.equalsLogically( right ) );

    // but we can assume hierarchy is the dimension name
    left.setDimension( "Sales" );
    left.setHierarchy( null );
    assertTrue( left.equalsLogically( right ) );
  }

  @Test
  public void testRemovingDuplicateLevels() {
    LevelMetaData duplicateLevel = new LevelMetaData();
    duplicateLevel.setName( "testLevel" );
    LevelMetaData notDuplicateLevel = new LevelMetaData();
    notDuplicateLevel.setName( "notDuplicateLevel" );

    HierarchyMetaData existingHierarchy = mock( HierarchyMetaData.class );

    LevelMetaData newLevel = new LevelMetaData( existingHierarchy, "testLevel" );

    when( existingHierarchy.getLevels() ).thenReturn( new ArrayList<>( Arrays.asList( duplicateLevel, notDuplicateLevel, newLevel ) ) );

    CreateAttribute createAttribute = new CreateAttribute();
    createAttribute.removeDuplicateLevel( newLevel );

    // Ensure duplicate level was removed
    verify( existingHierarchy ).remove( duplicateLevel );

    // Test not being a duplicate so level should not be removed
    reset( existingHierarchy );
    duplicateLevel.setName( "alsoNotDuplicate" );
    createAttribute.removeDuplicateLevel( newLevel );
    verify( existingHierarchy, never() ).remove( duplicateLevel );
  }
}
