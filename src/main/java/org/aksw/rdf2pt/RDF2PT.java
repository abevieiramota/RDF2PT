package org.aksw.rdf2pt;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.aksw.rdf2pt.triple2nl.DocumentGeneratorPortuguese;
import org.aksw.rdf2pt.triple2nl.TripleConverterPortuguese;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.dllearner.kb.sparql.SparqlEndpoint;

public class RDF2PT {
	
	public static String triple(String subject, String predicate, String object) throws IOException{
		// create the triple we want to convert by using JENA API
		Triple t = Triple.create(
					 NodeFactory.createURI(subject),
					 NodeFactory.createURI(predicate),
					 NodeFactory.createURI(object));

		// Optionally, we can declare a knowledge base that contains the triple.
		// This can be useful during the verbalization process, e.g. the KB could contain labels for entities.
		// Here, we use the DBpedia SPARQL endpoint.
		SparqlEndpoint endpoint = SparqlEndpoint.create("http://pt.dbpedia.org/sparql", "http://dbpedia.org");

		// create the triple converter
		TripleConverterPortuguese converter = new TripleConverterPortuguese(endpoint);

		// convert the triple into natural language
		return converter.convert(t);
	}
	
	public static String triples(List<Triple> triples) throws IOException{
		// create the triple we want to convert by using JENA API

		// Optionally, we can declare a knowledge base that contains the triple.
		// This can be useful during the verbalization process, e.g. the KB could contain labels for entities.
		// Here, we use the DBpedia SPARQL endpoint.
		SparqlEndpoint endpoint = SparqlEndpoint.create("http://pt.dbpedia.org/sparql", "http://dbpedia.org");

		// create the triple converter
		TripleConverterPortuguese converter = new TripleConverterPortuguese(endpoint);

		// convert the triple into natural language
		return converter.convert(triples);
	}
	
	public static String resumo(String uri) throws IOException{
		DocumentGeneratorPortuguese gen = new DocumentGeneratorPortuguese(SparqlEndpoint.create("http://pt.dbpedia.org/sparql", "http://dbpedia.org"), "cache");
		
		Sparql sparql = new Sparql();
		Set<Triple> triples = sparql.getTriples(uri);

		return gen.generateDocument(triples);
	}

	public static void main(String[] args) throws IOException {
		System.out.println(resumo("http://pt.dbpedia.org/resource/Os_Lusíadas"));
		System.out.println(resumo("http://pt.dbpedia.org/resource/Marcos_Pontes"));
		System.out.println(resumo("http://pt.dbpedia.org/resource/Albert_Einstein"));
	
	}
}
