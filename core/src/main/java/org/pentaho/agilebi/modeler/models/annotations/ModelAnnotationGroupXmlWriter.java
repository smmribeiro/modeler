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

import org.apache.commons.lang.StringUtils;
import org.pentaho.agilebi.modeler.models.annotations.data.ColumnMapping;
import org.pentaho.agilebi.modeler.models.annotations.data.DataProvider;
import org.pentaho.agilebi.modeler.models.annotations.util.KeyValueClosure;
import org.pentaho.agilebi.modeler.models.annotations.util.XMLUtil;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.metadata.model.concept.types.DataType;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * @author Rowell Belen
 */
public class ModelAnnotationGroupXmlWriter {

  private ModelAnnotationGroup modelAnnotations;

  public ModelAnnotationGroup getModelAnnotations() {
    return modelAnnotations;
  }

  public void setModelAnnotations(
      ModelAnnotationGroup modelAnnotations ) {
    this.modelAnnotations = modelAnnotations;
  }

  public ModelAnnotationGroupXmlWriter( ModelAnnotationGroup modelAnnotationGroup ) {
    this.modelAnnotations = modelAnnotationGroup;
  }

  public String getXML() {
    try {
      return XMLUtil.prettyPrint( getModelAnnotationsXml() );
    } catch ( Exception e ) {
      return getModelAnnotationsXml();
    }
  }

  private String getModelAnnotationsXml() {

    final StringBuffer xml = new StringBuffer();

    xml.append( "    <annotations>" );
    if ( getModelAnnotations() != null ) {
      for ( ModelAnnotation<?> modelAnnotation : getModelAnnotations() ) {

        // Add default name
        if ( StringUtils.isBlank( modelAnnotation.getName() ) ) {
          modelAnnotation.setName( UUID.randomUUID().toString() ); // backwards compatibility
        }

        xml.append( "      <annotation>" );
        xml.append( "        " ).append( XMLHandler.addTagValue( "name", modelAnnotation.getName() ) );
        xml.append( "        " )
            .append( XMLHandler.addTagValue( "field", modelAnnotation.getAnnotation().getField() ) );
        if ( modelAnnotation.getType() != null ) {
          xml.append( "        " ).append( XMLHandler.addTagValue( "type", modelAnnotation.getType().toString() ) );
          xml.append( "         <properties>" );
          modelAnnotation.iterateProperties( new KeyValueClosure() {
            @Override
            public void execute( String key, Serializable serializable ) {
              if ( !"field".equals( key ) ) {
                xml.append( "            " ).append( XMLHandler.openTag( "property" ) );
                xml.append( "               " ).append( XMLHandler.addTagValue( "name", key ) );
                xml.append( "               " ).append( XMLHandler.openTag( "value" ) );
                xml.append( XMLHandler.buildCDATA( serializable.toString() ) );
                xml.append( XMLHandler.closeTag( "value" ) );
                xml.append( "            " ).append( XMLHandler.closeTag( "property" ) );
              }
            }
          } );
          xml.append( "         </properties>" );
        }
        xml.append( "      </annotation>" );
      }
      xml.append( "    " )
          .append( XMLHandler.addTagValue( "sharedDimension", getModelAnnotations().isSharedDimension() ) );
      xml.append( "    " )
          .append( XMLHandler.addTagValue( "description", getModelAnnotations().getDescription() ) );
      xml.append( getDataProvidersXml( getModelAnnotations().getDataProviders() ) );
    }
    xml.append( "    </annotations>" );

    return xml.toString();
  }

  private String getDataProvidersXml( final List<DataProvider> dataProviders ) {
    final StringBuffer xml = new StringBuffer();

    if ( dataProviders != null && !dataProviders.isEmpty() ) {
      xml.append( "    <data-providers>" );
      for ( DataProvider provider : dataProviders ) {
        xml.append( "    <data-provider>" );
        xml.append( "        " ).append( XMLHandler.addTagValue( "name", provider.getName() ) );
        xml.append( "        " ).append( XMLHandler.addTagValue( "schemaName", provider.getSchemaName() ) );
        xml.append( "        " ).append( XMLHandler.addTagValue( "tableName", provider.getTableName() ) );
        xml.append( "        " )
            .append( XMLHandler.addTagValue( "databaseMetaRef", provider.getDatabaseMetaNameRef() ) );
        xml.append( getColumnMappingsXml( provider.getColumnMappings() ) );
        xml.append( "    </data-provider>" );
      }
      xml.append( "    </data-providers>" );
    }

    return xml.toString();
  }

  private String getColumnMappingsXml( final List<ColumnMapping> columnMappings ) {

    final StringBuffer xml = new StringBuffer();

    if ( columnMappings != null && !columnMappings.isEmpty() ) {
      xml.append( "    <column-mappings>" );
      for ( ColumnMapping columnMapping : columnMappings ) {
        xml.append( "    <column-mapping>" );
        xml.append( "        " ).append( XMLHandler.addTagValue( "name", columnMapping.getName() ) );
        xml.append( "        " ).append( XMLHandler.addTagValue( "columnName", columnMapping.getColumnName() ) );

        DataType dataType = columnMapping.getColumnDataType();
        if ( dataType != null ) {
          xml.append( "        " )
              .append( XMLHandler.addTagValue( "dataType", columnMapping.getColumnDataType().name() ) );
        }

        xml.append( "    </column-mapping>" );
      }
      xml.append( "    </column-mappings>" );
    }

    return xml.toString();
  }
}
