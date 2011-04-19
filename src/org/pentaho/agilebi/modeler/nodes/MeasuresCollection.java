/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2010 Pentaho Corporation..  All rights reserved.
 */
package org.pentaho.agilebi.modeler.nodes;

import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerMessagesHolder;
import org.pentaho.agilebi.modeler.propforms.GenericPropertiesForm;
import org.pentaho.agilebi.modeler.propforms.ModelerNodePropertiesForm;
import org.pentaho.ui.xul.stereotype.Bindable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MeasuresCollection extends AbstractMetaDataModelNode<MeasureMetaData> implements Serializable {
  private String name = "Measures";//BaseMessages.getString(ModelerWorkspace.class, "measures");

  public static String MEASURE_PROP = "potential_measure";

  public MeasuresCollection() {
    this.valid = false;
  }

  @Bindable
  public String getName() {
    return name;
  }

  @Bindable
  public String getDisplayName() {
    return getName();
  }

  @Bindable
  public void setName( String name ) {
    this.name = name;
  }

  @Bindable
  public boolean isUiExpanded() {
    return true;
  }

  @Override
  @Bindable
  public String getValidImage() {
    return "images/sm_folder_icon.png"; //$NON-NLS-1$
  }

  @Override
  public void validate() {
    boolean prevVal = valid;
    valid = true;
    validationMessages.clear();

    if (size() == 0) {
      validationMessages.add("Need a measure");//BaseMessages.getString(ModelerWorkspace.class, "need_one_measure"));
      valid = false;
    }
    List<String> usedNames = new ArrayList<String>();
    for (MeasureMetaData measure : children) {
      valid &= measure.isValid();
      validationMessages.addAll(measure.getValidationMessages());
      if (usedNames.contains(measure.getName())) {
        valid = false;
        validationMessages.add(
            "duplicate measure");//BaseMessages.getString(ModelerWorkspace.class, "duplicate_measure_name"));
      }

      usedNames.add(measure.getName());

    }
    this.firePropertyChange("valid", prevVal, valid);
  }

  @Override
  public void onAdd( MeasureMetaData child ) {
    child.setParent(this);
    child.addPropertyChangeListener("name", nameListener);
    child.addPropertyChangeListener("valid", validListener);
    child.addPropertyChangeListener("children", childrenListener);
    validateNode();
  }

  public void onRemove( MeasureMetaData child ) {
    child.removePropertyChangeListener(validListener);
    child.removePropertyChangeListener(nameListener);
    child.removePropertyChangeListener(childrenListener);
    validateNode();
  }


  @Bindable
  public boolean isEditingDisabled() {
    return true;
  }

  @Override
  public Class<? extends ModelerNodePropertiesForm> getPropertiesForm() {
    return GenericPropertiesForm.class;
  }

  @Override
  public boolean acceptsDrop(Object obj) {
    if(obj instanceof AvailableField){
      // check to see if the backing column is flagged as a potential Measure
      return ((AvailableField) obj).getPhysicalColumn().getProperty(MEASURE_PROP).equals(Boolean.TRUE);
    } else if(obj instanceof LevelMetaData){
      // check to see if the backing column is flagged as a potential Measure
      // this really only makes sense in the single-table modeling scenario
      return ((LevelMetaData) obj).getLogicalColumn().getProperty(MEASURE_PROP).equals(Boolean.TRUE);
    } else {
      return(obj instanceof MeasureMetaData);
    }
  }

  @Override
  public Object onDrop(Object data) throws ModelerException {
    try{
      MeasureMetaData measure = null;
      if(data instanceof AvailableField){
        measure = getWorkspace().createMeasureForNode((AvailableField) data);
      } else if(data instanceof MeasureMetaData){
        measure = (MeasureMetaData) data;
      } else {
        throw new IllegalArgumentException(ModelerMessagesHolder.getMessages().getString("invalid_drop"));
      }
      String agileBiVersion = (String) getWorkspace().getDomain().getLogicalModels().get(0).getProperty("AGILE_BI_VERSION");

      if(measure != null && agileBiVersion != null && Float.parseFloat(agileBiVersion) >= 2.0 ){
        Object factProp = measure.getLogicalColumn().getLogicalTable().getPhysicalTable().getProperty("FACT_TABLE");
        if(factProp == null || factProp.equals(Boolean.FALSE)){
          throw new IllegalStateException(ModelerMessagesHolder.getMessages().getString("DROP.ERROR.MEASURE_NOT_FROM_FACT"));
        }
      }
      return measure;
    } catch(Exception e){
      throw new ModelerException(e);
    }
  }
}