package see.fa.jmeter.parallelrequest;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterVariables;

/**
 *
 * @author franz
 */
public class ParallelSamplerGroup {
    private final List<Sampler> samplers;
    private final ExecutorService executorService;

    private transient boolean alreadyRunning = false;

    private transient Map<Sampler, Future<SampleResult>> futures = null;

    public ParallelSamplerGroup(List<Sampler> samplers) {
        this.samplers = samplers;
        executorService = Executors.newFixedThreadPool(samplers.size());
    }

    public SampleResult sample(Sampler sampler) {
        if (alreadyRunning) {
            return getSamplerResult(sampler);
        }

        alreadyRunning = true;
        futures = executeAllSamplersAsynchronously();
        return getSamplerResult(sampler);
    }

    private Map<Sampler, Future<SampleResult>> executeAllSamplersAsynchronously() {
        Map<Sampler, Future<SampleResult>> futures = new LinkedHashMap<Sampler, Future<SampleResult>>();
        for (Sampler sampler : samplers) {
            futures.put(sampler, executorService.submit(new SamplerCallable(sampler)));
        }
        return futures;
    }

    private SampleResult getSamplerResult(Sampler sampler) {
        try {
            return futures.get(sampler).get();
        } catch (InterruptedException ex) {
            throw new ParallelSamplerGroupException("Unable to retrieve SampleResult.", ex);
        } catch (ExecutionException ex) {
            throw new ParallelSamplerGroupException("Unable to retrieve SampleResult.", ex);
        }
    }

    public static class SamplerCallable implements Callable<SampleResult> {
        private final Sampler sampler;
        private final Map<String, String> properties = new HashMap<String, String>();
        
        private SamplerCallable(Sampler sampler){
            this.sampler = sampler;
            PropertyIterator it = sampler.propertyIterator();
            while(it.hasNext()){
            	JMeterProperty property = it.next();
        		this.properties.put(property.getName(), property.getStringValue());
        	}
        }

        public SampleResult call() throws Exception {
        	//this.sampler.setThreadContext(this.context);
        	
        	for(String key : this.properties.keySet()){
        		this.sampler.setProperty(key, this.properties.get(key).toString());
        		System.out.println("["+sampler.getName()+" property] \t" + key +" = " + this.properties.get(key).toString() +" | "+sampler.getPropertyAsString(key));
        	}
        	
        	PropertyIterator it = sampler.propertyIterator();
        	
        	System.out.println("###########Sampler " + sampler.getName());
//        	System.out.println("Variables:");
//        	JMeterVariables variables = this.sampler.getThreadContext().getVariables();
//        	if(variables != null){
//        		Iterator<java.util.Map.Entry<String,Object>> variablesIt = variables.getIterator();
//        		while(variablesIt.hasNext()){
//        			java.util.Map.Entry<String,Object> entry = variablesIt.next();
//        			System.out.println("["+sampler.getName()+" variable] \t" + entry.getKey() + " = " + entry.getValue().toString());
//        		}        		
//        	}
        	System.out.println("Sampler Properties:");
        	while(it.hasNext()){
        		JMeterProperty property = it.next();
        		System.out.println("["+sampler.getName()+" property] \t"+property.getName() + " = " + property.getStringValue());
        	}
        	
        	
            return sampler.sample(null);
        }
    }

}
