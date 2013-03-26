package EnterpriseSystems.CloudManager.DroolsManagement;

import EnterpriseSystems.CloudManager.Model.Cloud;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.conf.EventProcessingOption;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.drools.builder.ResourceType.DRL;
import static org.drools.io.ResourceFactory.newClassPathResource;

/**
 * Created with IntelliJ IDEA.
 * User: Luke
 * Date: 23/02/2013
 * Time: 16:08
 * To change this template use File | Settings | File Templates.
 */
public class DroolsConfiguration {

    private StatefulKnowledgeSession knowledgeSession;

    public void Init(DroolsManager droolsManager) {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        kbuilder.batch().add( newClassPathResource("EnterpriseSystems/CloudManager/DroolsManagement/Energy.drl", getClass()  ), DRL )
                        .add( newClassPathResource("EnterpriseSystems/CloudManager/DroolsManagement/Overload.drl", getClass() ), DRL ).build();
        if ( kbuilder.hasErrors() ) {
            throw new RuntimeException( kbuilder.getErrors().toString() );
        }

        KnowledgeBaseConfiguration config = KnowledgeBaseFactory.newKnowledgeBaseConfiguration();
        config.setOption( EventProcessingOption.STREAM );
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase( config );
        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
        knowledgeSession = kbase.newStatefulKnowledgeSession();
        knowledgeSession.setGlobal("manager",droolsManager);

        runKnowledgeSession(knowledgeSession);

    }

    public void addToEngine(Cloud cloud) {
        knowledgeSession.insert(cloud);
        //could do the fact checking thing for the cloud object
    }
    public void addToEngine(List<Object> objectsToAdd) {
        knowledgeSession.halt();
        for (Object obj : objectsToAdd) {
            FactHandle factHandle;
            if ((factHandle = knowledgeSession.getFactHandle(obj)) == null) {
                knowledgeSession.insert(obj);
               //System.out.println("inserted new object into engine");
            } else {
                knowledgeSession.update(factHandle,obj);
                //System.out.println("updated existing object into engine");
            }

        }
        runKnowledgeSession(knowledgeSession);
    }



    private void runKnowledgeSession(StatefulKnowledgeSession ksession) {
        final StatefulKnowledgeSession knowledgeSession = ksession;
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.submit(new Runnable() {
            public void run() {
                knowledgeSession.fireUntilHalt();
            }
        });
    }

    public StatefulKnowledgeSession getKnowledgeSession() {
        return knowledgeSession;
    }
}
