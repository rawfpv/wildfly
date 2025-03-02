/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.as.service.logging;

import static org.jboss.logging.Logger.Level.ERROR;
import static org.jboss.logging.Logger.Level.WARN;

import javax.xml.namespace.QName;
import javax.management.ObjectName;

import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.logging.BasicLogger;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.vfs.VirtualFile;
import org.jboss.msc.service.StartException;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
@MessageLogger(projectCode = "WFLYSAR", length = 4)
public interface SarLogger extends BasicLogger {

    /**
     * A logger with a category of the package name.
     */
    SarLogger ROOT_LOGGER = Logger.getMessageLogger(SarLogger.class, "org.jboss.as.service");

    /**
     * A message indicating a failure to execute a legacy service method, represented by the {@code methodName}
     * parameter.
     *
     * @param methodName the name of the method that failed to execute.
     *
     * @return the message
     */
    @Message(id = 1, value = "Failed to execute legacy service %s method")
    String failedExecutingLegacyMethod(String methodName);

    /**
     * Logs a warning message indicating the inability to find a {@link java.beans.PropertyEditor} for the type.
     *
     * @param type the type.
     */
    @LogMessage(level = WARN)
    @Message(id = 2, value = "Unable to find PropertyEditor for type %s")
    void propertyNotFound(Class<?> type);

    /**
     * Creates an exception indicating the class could not be found.
     *
     * @param cause the cause of the error.
     *
     * @return an {@link IllegalArgumentException} for the error.
     */
    @Message(id = 3, value = "Class not found")
    IllegalArgumentException classNotFound(@Cause Throwable cause);

    /**
     * Creates an exception indicating the class was not instantiated.
     *
     * @param cause the cause of the error.
     *
     * @return an {@link IllegalArgumentException} for the error.
     */
    @Message(id = 4, value = "Class not instantiated")
    IllegalArgumentException classNotInstantiated(@Cause Throwable cause);

    /**
     * Creates an exception indicating a failure to get an attachment.
     *
     * @param attachmentType the type/name of the attachment.
     * @param deploymentUnit the deployment unit.
     *
     * @return a {@link org.jboss.as.server.deployment.DeploymentUnitProcessingException} for the error.
     */
    @Message(id = 5, value = "Failed to get %s attachment for %s")
    DeploymentUnitProcessingException failedToGetAttachment(String attachmentType, DeploymentUnit deploymentUnit);

    /**
     * Creates an exception indicating a failure to parse the service XML file.
     *
     * @param xmlFile the XML file that failed to parse.
     *
     * @return a {@link DeploymentUnitProcessingException} for the error.
     */
    @Message(id = 6, value = "Failed to parse service xml [%s]")
    DeploymentUnitProcessingException failedXmlParsing(VirtualFile xmlFile);

    /**
     * Creates an exception indicating a failure to parse the service XML file.
     *
     * @param cause   the cause of the error.
     * @param xmlFile the XML file that failed to parse.
     *
     * @return a {@link DeploymentUnitProcessingException} for the error.
     */
    DeploymentUnitProcessingException failedXmlParsing(@Cause Throwable cause, VirtualFile xmlFile);

    /**
     * Creates an exception indicating the method could not be found.
     *
     * @param methodName   the name of the method.
     * @param methodParams the method parameters.
     * @param className    the name of the class.
     *
     * @return an {@link IllegalStateException} for the error.
     */
    @Message(id = 7, value = "Method '%s(%s)' not found for: %s")
    IllegalStateException methodNotFound(String methodName, String methodParams, String className);

    /**
     * A message indicating one or more required attributes are missing.
     *
     * @return the message.
     */
    @Message(id = 8, value = "Missing one or more required attributes:")
    String missingRequiredAttributes();

    /**
     * Creates an exception indicating the variable, represented by the {@code name} parameter, is {@code null}.
     *
     * @param name the name of the variable.
     *
     * @return an {@link IllegalArgumentException} for the error.
     */
    @Message(id = 9, value = "%s is null")
    IllegalArgumentException nullVar(String name);

    /**
     * Creates an exception indicating the method for the property is not found.
     *
     * @param methodPrefix the prefix for the method, e.g. {@code get} or {@code set}.
     * @param propertyName the name of the property.
     * @param className    the class the property was being queried on.
     *
     * @return an {@link IllegalStateException} for the error.
     */
    @Message(id = 10, value = "%s method for property '%s' not found for: %s")
    IllegalStateException propertyMethodNotFound(String methodPrefix, String propertyName, String className);

    /**
     * A message indicating unexpected content was found.
     *
     * @param kind the kind of content.
     * @param name the name of the attribute.
     * @param text the value text of the attribute.
     *
     * @return the message.
     */
    @Message(id = 11, value = "Unexpected content of type '%s' named '%s', text is: %s")
    String unexpectedContent(String kind, QName name, String text);

    /**
     * Creates an exception indicating a failure to process the resource adapter child archives for the deployment root
     * represented by the {@code deploymentRoot} parameter.
     *
     * @param cause          the cause of the error.
     * @param deploymentRoot the deployment root.
     *
     * @return a {@link DeploymentUnitProcessingException} for the error.
     */
    @Message(id = 12, value = "Failed to process SAR child archives for [%s]")
    DeploymentUnitProcessingException failedToProcessSarChild(@Cause Throwable cause, VirtualFile deploymentRoot);

    /**
     * Creates an exception indicating that dependency of mbean was malformed in service descriptor.
     *
     * @param cause MalformedObjectNameException.
     * @param dependencyName the name of malformed dependency.
     *
     * @return a {@link DeploymentUnitProcessingException} for the error.
     */
    @Message(id = 13, value = "Malformed dependency name %s")
    DeploymentUnitProcessingException malformedDependencyName(@Cause Throwable cause,  String dependencyName);

    /**
     * Creates an exception indicating the default constructor for the class, represented by the {@code clazz} parameter, could
     * not be found.
     *
     * @param clazz the class.
     *
     * @return a {@link DeploymentUnitProcessingException} for the error.
     */
    @Message(id = 14, value = "Could not find default constructor for %s")
    DeploymentUnitProcessingException defaultConstructorNotFound(Class<?> clazz);

    /**
     * Creates an exception indicating a failure to register the MBean.
     *
     * @param cause the cause of the error.
     * @param name  the name of the MBean.
     *
     * @return a {@link StartException} for the error.
     */
    @Message(id = 15, value = "Failed to register mbean [%s]")
    StartException mbeanRegistrationFailed(@Cause Throwable cause, String name);

    /**
     * Logs a warning message indicating no {@link javax.management.ObjectName} is available to unregister.
     */
    @LogMessage(level = WARN)
    @Message(id = 16, value = "No ObjectName available to unregister")
    void cannotUnregisterObject();

    /**
     * Logs an error message indicating a failure to unregister the object name.
     *
     * @param cause the cause of the error.
     * @param name  the name of the object name.
     */
    @LogMessage(level = ERROR)
    @Message(id = 17, value = "Failed to unregister [%s]")
    void unregistrationFailure(@Cause Throwable cause, ObjectName name);

}
