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

import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Document;

/**
 * @author Rowell Belen
 */
public class MockAnnotationType extends AnnotationType {

  @ModelProperty( id = "i" )
  private int i;

  @ModelProperty( id = "d" )
  private double d;

  @ModelProperty( id = "f" )
  private float f;

  @ModelProperty( id = "l" )
  private long l;

  @ModelProperty( id = "s" )
  private short s;

  public int getI() {
    return i;
  }

  public void setI( int i ) {
    this.i = i;
  }

  public double getD() {
    return d;
  }

  public void setD( double d ) {
    this.d = d;
  }

  public float getF() {
    return f;
  }

  public void setF( float f ) {
    this.f = f;
  }

  public long getL() {
    return l;
  }

  public void setL( long l ) {
    this.l = l;
  }

  public short getS() {
    return s;
  }

  public void setS( short s ) {
    this.s = s;
  }

  @Override
  public boolean apply( ModelerWorkspace workspace, final IMetaStore metaStore ) {
    return true;
  }

  @Override
  public ModelAnnotation.Type getType() {
    return null;
  }

  @Override public String getSummary() {
    return "";
  }

  @Override
  public boolean apply( Document schema ) throws ModelerException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override public String getName() {
    return "";
  }

  @Override
  public void validate() throws ModelerException {
  }

  @Override
  public String getField() {
    return null;
  }
}
