/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.extension.messaging.activemq;

import static org.jboss.as.controller.RunningMode.ADMIN_ONLY;
import static org.wildfly.extension.messaging.activemq.MessagingExtension.BINDINGS_DIRECTORY_PATH;
import static org.wildfly.extension.messaging.activemq.MessagingExtension.JOURNAL_DIRECTORY_PATH;
import static org.wildfly.extension.messaging.activemq.MessagingExtension.LARGE_MESSAGES_DIRECTORY_PATH;
import static org.wildfly.extension.messaging.activemq.MessagingExtension.PAGING_DIRECTORY_PATH;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import org.apache.activemq.artemis.cli.commands.tools.xml.XmlDataExporter;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.SimpleOperationDefinitionBuilder;
import org.jboss.as.controller.descriptions.ResourceDescriptionResolver;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.wildfly.extension.messaging.activemq.logging.MessagingLogger;

/**
 * Export a dump of Artemis journal. WildFly must be running in ADMIN-ONLY mode to perform this operation.
 *
 * The dump is stored on WildFly host and is not sent to the client invoking the operation.
 *
 * @author <a href="http://jmesnil.net/">Jeff Mesnil</a> (c) 2015 Red Hat inc.
 */
public class ExportJournalOperation extends AbstractArtemisActionHandler {

    private static final String OPERATION_NAME = "export-journal";
    static final ExportJournalOperation INSTANCE = new ExportJournalOperation();

    // name file of the dump follows the format journal-yyyyMMdd-HHmmssSSSTZ-dump.xml
    private static final String FILE_NAME_FORMAT = "journal-%1$tY%<tm%<td-%<tH%<tM%<tS%<TL%<tz-dump.xml";

    private ExportJournalOperation() {

    }

    static void registerOperation(final ManagementResourceRegistration registry, final ResourceDescriptionResolver resourceDescriptionResolver) {
        registry.registerOperationHandler(new SimpleOperationDefinitionBuilder(OPERATION_NAME, resourceDescriptionResolver)
                        .setRuntimeOnly()
                        .setReplyValueType(ModelType.STRING)
                        .build(),
                INSTANCE);
    }

    @Override
    protected void executeRuntimeStep(OperationContext context, ModelNode operation) throws OperationFailedException {
        if (context.getRunningMode() != ADMIN_ONLY) {
            throw MessagingLogger.ROOT_LOGGER.managementOperationAllowedOnlyInRunningMode(OPERATION_NAME, ADMIN_ONLY);
        }
        checkAllowedOnJournal(context, OPERATION_NAME);

        final String journal = resolvePath(context, JOURNAL_DIRECTORY_PATH);
        final String bindings = resolvePath(context, BINDINGS_DIRECTORY_PATH);
        final String paging = resolvePath(context,  PAGING_DIRECTORY_PATH);
        final String largeMessages = resolvePath(context, LARGE_MESSAGES_DIRECTORY_PATH);

        final XmlDataExporter exporter = new XmlDataExporter();

        String name = String.format(FILE_NAME_FORMAT, new Date());
        // write the exported dump at the same level than the journal directory
        File dump = new File(new File(journal).getParent(), name);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(dump);
            exporter.process(fos, bindings, journal, paging, largeMessages);
            context.getResult().set(dump.getAbsolutePath());
        } catch (Exception e) {
            throw new OperationFailedException(e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
