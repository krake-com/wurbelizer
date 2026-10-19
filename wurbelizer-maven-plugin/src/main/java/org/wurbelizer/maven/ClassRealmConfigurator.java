/*
 * Wurbelizer - https://wurbelizer.org
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

package org.wurbelizer.maven;

import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.configurator.AbstractComponentConfigurator;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ConfigurationListener;
import org.codehaus.plexus.component.configurator.converters.composite.ObjectWithFieldsConverter;
import org.codehaus.plexus.component.configurator.converters.special.ClassRealmConverter;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;

import javax.inject.Named;

/**
 * A custom configurator for the {@link WurbelMojo} and the {@link TestWurbelMojo}.
 * <p>
 * Extracts the classrealm and makes it available to the mojos.
 * This is necessary in multimodule maven projects if wurblets are compiled
 * in one module and used in other modules of the project. Otherwise,
 * the first build of the super-pom will fail.
 *
 * @author harald
 * @see AbstractWurbelMojo#setClassRealm(org.codehaus.plexus.classworlds.realm.ClassRealm)
 */
@Named("set-classrealm")
public class ClassRealmConfigurator extends AbstractComponentConfigurator {


  @Override
  public void configureComponent(Object component, PlexusConfiguration configuration,
                                 ExpressionEvaluator expressionEvaluator, ClassRealm containerRealm,
                                 ConfigurationListener listener)
         throws ComponentConfigurationException {

    if (component instanceof AbstractWurbelMojo) {
      ((AbstractWurbelMojo) component).setClassRealm(containerRealm);
    }
    else  {
      throw new ComponentConfigurationException("configurator can only be applied to an AbstractWurbelMojo");
    }

    converterLookup.registerConverter(new ClassRealmConverter(containerRealm));

    ObjectWithFieldsConverter converter = new ObjectWithFieldsConverter();

    converter.processConfiguration(converterLookup, component, containerRealm,
                                   configuration, expressionEvaluator, listener);
  }

}
