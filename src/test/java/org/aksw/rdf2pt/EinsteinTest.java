package org.aksw.rdf2pt;

import java.io.IOException;

import org.aksw.rdf2pt.RDF2PT;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class EinsteinTest {

	private static final String EINSTEIN_URI = "http://pt.dbpedia.org/resource/Albert_Einstein";

	@Test
	public void einstein() throws IOException {
		
		String einsteinSummary = RDF2PT.resumo(EINSTEIN_URI);
		
		System.out.println(einsteinSummary);
	}
	
}
