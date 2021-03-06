/*
 * Copyright 2011 Red Hat Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.drools.persistence.session;

import static org.drools.persistence.util.PersistenceUtil.DROOLS_PERSISTENCE_UNIT_NAME;
import static org.drools.persistence.util.PersistenceUtil.createEnvironment;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.drools.command.impl.CommandBasedStatefulKnowledgeSession;
import org.drools.command.impl.GenericCommand;
import org.drools.command.impl.KnowledgeCommandContext;
import org.drools.io.impl.ClassPathResource;
import org.drools.persistence.util.PersistenceUtil;
import org.drools.runtime.rule.impl.InternalAgenda;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.KnowledgeBase;
import org.kie.KnowledgeBaseFactory;
import org.kie.builder.KnowledgeBuilder;
import org.kie.builder.KnowledgeBuilderFactory;
import org.kie.command.Context;
import org.kie.io.ResourceType;
import org.kie.persistence.jpa.JPAKnowledgeService;
import org.kie.runtime.Environment;
import org.kie.runtime.KieSession;
import org.kie.runtime.StatefulKnowledgeSession;

public class RuleFlowGroupRollbackTest {
    
    private HashMap<String, Object> context;

    @Before
    public void setUp() throws Exception {
        context = PersistenceUtil.setupWithPoolingDataSource(DROOLS_PERSISTENCE_UNIT_NAME);
    }
	
	@After
	public void tearDown() {
		PersistenceUtil.tearDown(context);
	}

    @Test	
	public void testRuleFlowGroupRollback() throws Exception {
		
		CommandBasedStatefulKnowledgeSession ksession = createSession();
		
		List<String> list = new ArrayList<String>();
		list.add("Test");
		
		ksession.insert(list);
		ksession.execute(new ActivateRuleFlowCommand("ruleflow-group"));
		assertEquals(1, ksession.fireAllRules());
		
		try {
			ksession.execute(new ExceptionCommand());
			fail("Process must throw an exception");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		ksession.insert(list);
		ksession.execute(new ActivateRuleFlowCommand("ruleflow-group"));
		assertEquals(1, ksession.fireAllRules());
		
	}
	
	private CommandBasedStatefulKnowledgeSession createSession() {
		
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add( new ClassPathResource("ruleflowgroup_rollback.drl"), ResourceType.DRL );
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();

        if ( kbuilder.hasErrors() ) {
            fail( kbuilder.getErrors().toString() );
        }

        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );

        Environment env = createEnvironment(context);
        return (CommandBasedStatefulKnowledgeSession) JPAKnowledgeService.newStatefulKnowledgeSession( kbase, null, env );
	}
	
	@SuppressWarnings("serial")
	public class ActivateRuleFlowCommand implements GenericCommand<Object> {
		
		private String ruleFlowGroupName;
		
		public ActivateRuleFlowCommand(String ruleFlowGroupName){
			this.ruleFlowGroupName = ruleFlowGroupName;
		}

	    public Void execute(Context context) {
	        KieSession ksession = ((KnowledgeCommandContext) context).getKieSession();
	        ((InternalAgenda) ksession.getAgenda()).activateRuleFlowGroup(ruleFlowGroupName);
	        return null;
	    }

	}
	
	@SuppressWarnings("serial")
	public class ExceptionCommand implements GenericCommand<Object> {

	    public Void execute(Context context) {
	    	throw new RuntimeException();
	    }

	}
	
}
