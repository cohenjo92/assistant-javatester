package com.ibm.watson.developer_cloud.assistant_tester;

import static com.ibm.watson.developer_cloud.assistant_tester.util.Assert.assertContains;
import static com.ibm.watson.developer_cloud.assistant_tester.util.Assert.assertContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ibm.watson.developer_cloud.assistant.v1.Assistant;
import com.ibm.watson.developer_cloud.assistant.v1.model.MessageResponse;
import com.ibm.watson.developer_cloud.assistant_tester.etl.ConversationTestLoader;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
 
@RunWith(Parameterized.class)
public class ExampleTest_Parameterized {

	private Conversation conversation = null;
	
	private static String USERNAME = System.getProperty("ASSISTANT_USERNAME");
	private static String PASSWORD = System.getProperty("ASSISTANT_PASSWORD");
	private static String VERSION = "2018-02-16";
	private static String WORKSPACE_ID = System.getProperty("WORKSPACE_ID");
	
    @Parameters(name="{0}")
    public static Collection<Object[]> data() {
    	ConversationTestLoader ctl = new ConversationTestLoader();
    	List<ConversationTest> tests = ctl.read("simple-assistant-test-cases.csv");
    	List<Object[]> ret = new ArrayList<Object[]>();
    	for(ConversationTest test : tests) {
    		ret.add(new Object[]{test});
    	}
        return ret;
    }
    
    @BeforeClass
    public static void checkEnvironment() {
    	if(USERNAME == null || PASSWORD == null || WORKSPACE_ID == null) {
    		System.err.println("Required environment variables are ASSISTANT_USERNAME, ASSISTANT_PASSWORD, and WORKSPACE_ID");
    		System.exit(-1);
    	}
    }
	
	@Before
	public void setup() {
	    Assistant service = new Assistant(VERSION);
	    service.setUsernameAndPassword(USERNAME, PASSWORD);

	    conversation = new Conversation(service, WORKSPACE_ID);
	}
	
	@After
	public void teardown() {
		conversation.reset();
	}
	
	private final ConversationTest test;
	public ExampleTest_Parameterized(ConversationTest test) {
		this.test = test;
	}
	
	@Test
	public void conversation_test() {
		System.out.println(">>>>> " + test.getName());
		
		if(test.getInitialContext() != null && test.getInitialContext().length > 0) {
			for(int i = 0; i < test.getInitialContext().length; i += 2) {
				conversation.getContext().put(test.getInitialContext()[i], test.getInitialContext()[i+1]);
			}
		}
		
		
		MessageResponse response = null;
		int turnCounter = 0;
		for(Turn t : test.getTurns()) {
			turnCounter++;
			response = conversation.turn(t.getUtterance());

			if(t.getExpectedOutput().length() > 0) {
				assertContains("turn " + turnCounter + " text", response, t.getExpectedOutput());
			}
			if(t.getContext() != null && t.getContext().length > 0) {
				for(int i = 0; i < t.getContext().length; i += 2) {
					assertContext("turn " + turnCounter + " state " + t.getContext()[i], 
							response, t.getContext()[i], t.getContext()[i+1]);
				}
			}
		}
		
	}
}
