package org.aksw.rdf2pt;

import java.util.Set;

import org.apache.jena.graph.Triple;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SparqlTest {

	private static final String einsteinURI = "http://pt.dbpedia.org/resource/Albert_Einstein";

	@Test
	public void testGetTriples() {
		
		Sparql sparql = new Sparql();
		Set<Triple> triples = sparql.getTriples(einsteinURI);
		
		for(Triple t: triples) {
			System.out.println(t);
		}
	}
	
}
