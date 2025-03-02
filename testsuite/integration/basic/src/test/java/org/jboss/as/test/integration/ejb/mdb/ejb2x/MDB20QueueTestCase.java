/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.test.integration.ejb.mdb.ejb2x;

import static org.jboss.as.test.integration.ejb.mdb.ejb2x.AbstractMDB2xTestCase.logger;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.test.integration.common.jms.JMSOperations;
import org.jboss.as.test.integration.common.jms.JMSOperationsProvider;
import org.jboss.as.test.shared.TimeoutUtil;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.jms.Message;
import jakarta.jms.Queue;
import javax.naming.InitialContext;
import java.util.PropertyPermission;

import static org.jboss.as.test.shared.PermissionUtils.createPermissionsXmlAsset;

import jakarta.jms.QueueRequestor;
import jakarta.jms.QueueSession;
import org.apache.activemq.artemis.api.core.management.ResourceNames;
import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;

/**
 * Tests EJB2.0 MDBs listening on a queue.
 *
 * @author <a href="mailto:istudens@redhat.com">Ivo Studensky</a>
 */
@RunWith(Arquillian.class)
@ServerSetup({MDB20QueueTestCase.JmsQueueSetup.class})
public class MDB20QueueTestCase extends AbstractMDB2xTestCase {

    private Queue queue;
    private Queue replyQueue;

    static class JmsQueueSetup implements ServerSetupTask {

        private JMSOperations jmsAdminOperations;

        @Override
        public void setup(ManagementClient managementClient, String containerId) throws Exception {
            jmsAdminOperations = JMSOperationsProvider.getInstance(managementClient);
            jmsAdminOperations.createJmsQueue("ejb2x/queue", "java:jboss/ejb2x/queue");
            jmsAdminOperations.createJmsQueue("ejb2x/replyQueue", "java:jboss/ejb2x/replyQueue");
        }

        @Override
        public void tearDown(ManagementClient managementClient, String containerId) throws Exception {
            if (jmsAdminOperations != null) {
                jmsAdminOperations.removeJmsQueue("ejb2x/queue");
                jmsAdminOperations.removeJmsQueue("ejb2x/replyQueue");
                jmsAdminOperations.close();
            }
        }
    }

    @Deployment
    public static Archive getDeployment() {
        final JavaArchive ejbJar = ShrinkWrap.create(JavaArchive.class, "mdb.jar");
        ejbJar.addClasses(EJB2xMDB.class, AbstractMDB2xTestCase.class);
        ejbJar.addPackage(JMSOperations.class.getPackage());
        ejbJar.addClasses(JmsQueueSetup.class, TimeoutUtil.class);
        ejbJar.addAsManifestResource(MDB20QueueTestCase.class.getPackage(), "ejb-jar-20.xml", "ejb-jar.xml");
        ejbJar.addAsManifestResource(MDB20QueueTestCase.class.getPackage(), "jboss-ejb3.xml", "jboss-ejb3.xml");
        ejbJar.addAsManifestResource(new StringAsset("Dependencies: org.jboss.as.controller-client, org.jboss.dmr, org.apache.activemq.artemis \n"), "MANIFEST.MF");
        ejbJar.addAsManifestResource(createPermissionsXmlAsset(new PropertyPermission("ts.timeout.factor", "read")), "jboss-permissions.xml");
        return ejbJar;
    }

    @Before
    public void initQueues() {
        try {
            final InitialContext ic = new InitialContext();

            queue = (Queue) ic.lookup("java:jboss/ejb2x/queue");
            replyQueue = (Queue) ic.lookup("java:jboss/ejb2x/replyQueue");
        purgeQueue("ejb2x/queue");
        purgeQueue("ejb2x/replyQueue");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Tests a simple EJB2.0 MDB.
     */
    @Test
    public void testEjb20MDB() {
        sendTextMessage("Say hello to " + EJB2xMDB.class.getName(), queue, replyQueue);
        final Message reply = receiveMessage(replyQueue, TimeoutUtil.adjust(5000));
        Assert.assertNotNull("Reply message was null on reply queue: " + replyQueue, reply);
    }

    /**
     * Removes all message son a queue
     *
     * @param queueName name of the queue
     * @throws Exception
     */
    private void purgeQueue(String queueName) throws Exception {
        QueueRequestor requestor = new QueueRequestor((QueueSession) session, ActiveMQJMSClient.createQueue("activemq.management"));
        Message m = session.createMessage();
        org.apache.activemq.artemis.api.jms.management.JMSManagementHelper.putOperationInvocation(m, ResourceNames.QUEUE + "jms.queue." + queueName, "removeAllMessages");
        Message reply = requestor.request(m);
        if (!reply.getBooleanProperty("_AMQ_OperationSucceeded")) {
            logger.warn(reply.getBody(String.class));
        }
    }
}
