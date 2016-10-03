package see.fa.jmeter.parallelrequest;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.threads.JMeterVariables;

/**
 *
 * @author franz
 */
public class ParallelRequestsController extends GenericController {

    private transient boolean hasAlreadyParallelizedAllSamplers = false;

    private Iterator<ParallelSampler> parallelSamplersIterator;

    @Override
    public Sampler next() {
        parallelizedAllSamplers();
        return parallelSamplersIterator.hasNext() ? parallelSamplersIterator.next() : null;
    }

    private void parallelizedAllSamplers() {
        if (hasAlreadyParallelizedAllSamplers) {
            return;
        }
        hasAlreadyParallelizedAllSamplers = true;
        List<Sampler> samplers = getAllSamplers();
        ParallelSamplerGroup parallelSamplerGroup = new ParallelSamplerGroup(samplers);
        List<ParallelSampler> parallelSamplers = new LinkedList<ParallelSampler>();
        for (Sampler sampler : samplers) {
        	
//        	PropertyIterator it = sampler.propertyIterator();
//        	
//        	System.out.println("###########" + sampler.getName());
//        	System.out.println("Variables:");
//        	JMeterVariables variables = this.getThreadContext().getVariables();
//        	if(variables != null){
//        		Iterator<Entry<String,Object>> variablesIt = variables.getIterator();
//        		while(variablesIt.hasNext()){
//        			Entry<String,Object> entry = variablesIt.next();
//        			System.out.println(entry.getKey() + " = " + entry.getValue().toString());
//        		}        		
//        	}
//        	System.out.println("Properties:");
//        	while(it.hasNext()){
//        		JMeterProperty property = it.next();
//        		System.out.println(property.getName() + " = " + property.getStringValue());
//        	}
        	
            parallelSamplers.add(new ParallelSampler(parallelSamplerGroup, sampler));
        }
        parallelSamplersIterator = parallelSamplers.iterator();
    }

    private List<Sampler> getAllSamplers() {
        Sampler sampler;
        List<Sampler> samplers = new LinkedList<Sampler>();
        do {
            sampler = super.next();
            if (sampler != null) {
                samplers.add(sampler);
            }
        } while (sampler != null);
        return samplers;
    }


}
