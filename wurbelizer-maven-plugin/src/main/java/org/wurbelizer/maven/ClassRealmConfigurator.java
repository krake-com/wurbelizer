/**
 * Wurbelizer - http://www.wurbelizer.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

// $Id: ClassRealmConfigurator.java 97 2016-01-04 11:35:02Z svn $

package org.wurbelizer.maven;

import org.codehaus.classworlds.ClassRealm;
import org.codehaus.plexus.component.configurator.AbstractComponentConfigurator;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ConfigurationListener;
import org.codehaus.plexus.component.configurator.converters.composite.ObjectWithFieldsConverter;
import org.codehaus.plexus.component.configurator.converters.special.ClassRealmConverter;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;


/**
 * A custom configurator for the WurbelMojo.
 * <p>
 * It extracts the classrealm and sets it in the mojo (where it is unavailable).
 *
 * @author harald
 *
 * @plexus.component role="org.codehaus.plexus.component.configurator.ComponentConfigurator"
 *                   role-hint="set-classrealm"
 * @plexus.requirement role="org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup"
 *                   role-hint="default"
 */
public class ClassRealmConfigurator extends AbstractComponentConfigurator {


  @Override
  public void configureComponent(Object component, PlexusConfiguration configuration,
                                 ExpressionEvaluator expressionEvaluator, ClassRealm containerRealm,
                                 ConfigurationListener listener)
         throws ComponentConfigurationException {

    if (component instanceof AbstractWurbelizerMojo) {
      ((AbstractWurbelizerMojo) component).setClassRealm(containerRealm);
    }
    else  {
      throw new ComponentConfigurationException("configurator can only be applied to an AbstractWurbelizerMojo");
    }

    converterLookup.registerConverter(new ClassRealmConverter(containerRealm));

    ObjectWithFieldsConverter converter = new ObjectWithFieldsConverter();

    converter.processConfiguration(converterLookup, component, containerRealm.getClassLoader(),
                                   configuration, expressionEvaluator, listener);
  }


}
