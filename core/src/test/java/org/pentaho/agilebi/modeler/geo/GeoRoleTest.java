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


package org.pentaho.agilebi.modeler.geo;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

/**
 * Created by IntelliJ IDEA. User: rfellows Date: 9/15/11 Time: 4:51 PM To change this template use File | Settings |
 * File Templates.
 */
public class GeoRoleTest {

  @Test
  public void testConstructor() {
    String[] aliases = { "state", "province" };

    GeoRole geoRole = new GeoRole( "State", "state, province" );
    assertEquals( "State", geoRole.getName() );
    assertEquals( 2, geoRole.getCommonAliases().size() );
    assertArrayEquals( aliases, geoRole.getCommonAliases().toArray( new String[] { } ) );
  }

  @Test
  public void testEvaluation() {
    String[] aliases = { "st", "state", "province", "stateProvince", "postal code" };
    GeoRole state = new GeoRole( "State", Arrays.asList( aliases ) );
    assertTrue( state.evaluate( "STATE" ) );
    assertTrue( state.evaluate( "province" ) );
    assertTrue( state.evaluate( "st" ) );
    assertTrue( state.evaluate( "StateProvince" ) );
    assertTrue( state.evaluate( "postalcode" ) );
    assertTrue( state.evaluate( "POSTALCODE" ) );
    assertTrue( state.evaluate( "postal_code" ) );
    assertTrue( state.evaluate( "postal code" ) );

    assertEquals( false, state.evaluate( "sta" ) );
    assertEquals( false, state.evaluate( "prov" ) );
    assertEquals( false, state.evaluate( "provinc" ) );
    assertEquals( false, state.evaluate( "past" ) );
  }

}
