/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.rdf2pt;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;

/**
 *
 * @author DiegoMoussallem
 */
public class Sparql {

	private static final String URI_RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
	private static final long DEFAULT_TIMEOUT = 20000;
	private static final String ONTOLOGY_SERVICE = "http://pt.dbpedia.org/sparql";
	private static final int MAX_PREDICATES = 6;
	private static final String SPARQL_PREDICATES = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
			+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
			+ "PREFIX dbo: <http://dbpedia.org/ontology/> "
			+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "

			+ "select distinct ?p (COUNT(?p) AS ?po) where {"
			+ "?s rdf:type <%s>. "
			+ "?s ?p ?o. "
			+ "?p rdfs:label []. "

			+ "FILTER ( strstarts(str(?p), 'http://dbpedia.org/ontology') ) "
			+ "FILTER ( !strstarts(str(?p), 'http://dbpedia.org/ontology/abstract' ) ) "
			+ "FILTER ( !strstarts(str(?o), 'http://commons.wikimedia.org/wiki/Special' ) ) "
			+ "FILTER ( !strstarts(str(?o), 'http://pt.wikipedia.org/wiki/Special' ) ) "
			+ "FILTER ( !strstarts(str(?p), 'http://www.w3.org/' ) ) "
			+ "FILTER ( !strstarts(str(?p), 'http://xmlns.com' ) ) "
			+ "FILTER ( !strstarts(str(?p), 'http://purl.org/dc/terms/subject' ) ) "
			+ "FILTER ( !strstarts(str(?p), 'http://pt.dbpedia.org/property/wikiPageUsesTemplate' ) ) "
			+ "FILTER ( !strstarts(str(?p), 'http://dbpedia.org/ontology/wikiPageExternalLink' ) ) "
			+ "FILTER ( !strstarts(str(?p), 'http://dbpedia.org/ontology/wikiPageWikiLink' ) ) "
			+ "} GROUP BY (?p) ORDER BY DESC (?po) LIMIT 50";

	private static final String SPARQL_OBJECTS = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
			+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
			+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
			+ "SELECT DISTINCT ?o WHERE { <%s> <%s> ?o. }";

	private static final String SPARQL_MOST_SPECIFIC_CLASS = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
			+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
			+ "PREFIX dbr: <http://dbpedia.org/resource/> "
			+ "PREFIX dbo: <http://dbpedia.org/ontology/> "
			+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "

			+ "SELECT DISTINCT ?lcs WHERE { "
			+ "?lcs ^rdf:type/rdfs:subClassOf* <%s>; "
			+ "       a owl:Class ."
			+ "  filter not exists { "
			+ "    ?llcs ^(rdf:type/rdfs:subClassOf*) <%s> ; "
			+ "          a owl:Class ; "
			+ "          rdfs:subClassOf+ ?lcs . "
			+ "  } "
			+ "FILTER ( !strstarts(str(?lcs), 'http://www.wikidata.org/entity/' ) )}";

	/**
	 * retorna as top 7 triplas <s, p, o> com: 
	 * 	s = resource 
	 * 	p \in top 7 predicados(um deles vai ser o rdf:type) 
	 * 		top 7 baseado em rank por quantidade de triplas, no store, com p, com o subject sendo entidade da classe mais específica 
	 * 	caso p tenha múltiplos objetos, retorna o último(detalhe de implementação...)
	 **/
	public Set<Triple> getTriples(String resourceURI) {

		Set<Triple> result = new HashSet<>();

		String mostSpecificType = mostSpecificClass(resourceURI);

		Node nodeResource = NodeFactory.createURI(resourceURI);
		Node nodeTypePredicate = NodeFactory.createURI(URI_RDF_TYPE);
		Node nodeMostSpecificClass = NodeFactory.createURI(mostSpecificType);

		result.add(Triple.create(nodeResource, nodeTypePredicate, nodeMostSpecificClass));

		try (QueryExecution query = QueryExecutionFactory.sparqlService(ONTOLOGY_SERVICE,
				String.format(SPARQL_PREDICATES, mostSpecificType))) {
			query.setTimeout(DEFAULT_TIMEOUT);

			ResultSet results = query.execSelect();

			int nPredicatesProcessed = 0;
			// TODO: não tem como pedir logo todas as triplas dos predicados?
			while (results.hasNext() && nPredicatesProcessed++ <= MAX_PREDICATES) {
				String predicate = results.next().getResource("p").toString();

				Triple predicateObject = getObject(resourceURI, predicate);
				if (predicateObject != null) {
					result.add(predicateObject);
				}
			}

			return result;
		}
	}

	public Triple getObject(String s, String p) {

		String sparqlObjects = String.format(SPARQL_OBJECTS, s, p);
		try (QueryExecution query = QueryExecutionFactory.sparqlService(ONTOLOGY_SERVICE, sparqlObjects)) {

			ResultSet results = query.execSelect();

			Node sNode = NodeFactory.createURI(s);
			Node pNode = NodeFactory.createURI(p);
			Node oNode;

			Triple triple = null;
			// FIXME: retorna o último objeto retornado na consulta, tal que
			// oNode <> null?
			while (results.hasNext()) {
				QuerySolution qs = results.next();

				if (qs.get("o").isLiteral()) {
					Literal literal = qs.getLiteral("o");

					String lexicalForm = literal.getLexicalForm();
					String language = literal.getLanguage();
					RDFDatatype datatype = literal.getDatatype();

					oNode = NodeFactory.createLiteral(lexicalForm, language, datatype);
				} else {
					oNode = NodeFactory.createURI(qs.getResource("o").toString());
				}

				if (oNode != null) {
					triple = Triple.create(sNode, pNode, oNode);
				}
			}

			return triple;
		}

	}

	public String mostGenericClass(String uri) {
		String sparqlQuery = " PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ " PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ " PREFIX dbr: <http://dbpedia.org/resource/>" + " PREFIX dbo: <http://dbpedia.org/ontology/>"
				+ " PREFIX owl: <http://www.w3.org/2002/07/owl#>" + "SELECT DISTINCT ?type WHERE {" + "<" + uri
				+ "> rdf:type ?type." + "?type rdfs:subClassOf ?genericType. "
				+ "?genericType rdfs:subClassOf owl:Thing ;"
				+ "FILTER ( strstarts(str(?type), 'http://dbpedia.org/ontology' ) )}";

		QueryExecution query = QueryExecutionFactory.sparqlService(ONTOLOGY_SERVICE, String.format(sparqlQuery));

		ResultSet results = null;
		try {
			results = query.execSelect();
		} catch (Exception e) {
			return "";
		}

		String property = "";
		while (results.hasNext()) {
			QuerySolution qs = results.next();
			property = qs.getResource("type").toString();

		}
		return property;
	}

	public String mostSpecificClass(String uri) {

		String sparqlMostSpecificClass = String.format(SPARQL_MOST_SPECIFIC_CLASS, uri, uri);
		try (QueryExecution query = QueryExecutionFactory.sparqlService(ONTOLOGY_SERVICE, sparqlMostSpecificClass)) {

			ResultSet results = query.execSelect();
			QuerySolution qs = results.next();

			return qs.getResource("lcs").toString();
		}
	}

	public boolean hasDeathPlace(String uri) {
		String sparqlQuery = " PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ " PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ " PREFIX dbr: <http://dbpedia.org/resource/>" + " PREFIX dbo: <http://dbpedia.org/ontology/>"
				+ " PREFIX owl: <http://www.w3.org/2002/07/owl#>" + "SELECT DISTINCT ?o WHERE {<" + uri + "> ?p ?o."
				+ "FILTER (?p = <http://dbpedia.org/ontology/deathPlace> || ?p = <http://pt.dbpedia.org/property/localMorte> )}";

		QueryExecution query = QueryExecutionFactory.sparqlService(ONTOLOGY_SERVICE, String.format(sparqlQuery));

		ResultSet results = null;
		try {
			results = query.execSelect();
		} catch (Exception e) {
			return false;
		}

		Node property = null;
		boolean contain = false;
		while (results.hasNext()) {
			QuerySolution qs = results.next();
			if (qs.get("o").isLiteral()) {
				property = NodeFactory.createLiteral(qs.getLiteral("o").getLexicalForm(),
						qs.getLiteral("o").getLanguage(), qs.getLiteral("o").getDatatype());
				// System.out.println(object.toString());
			} else {
				property = NodeFactory.createURI(qs.getResource("o").toString());
				// System.out.println(object.toString());
			}

		}

		if (property != null) {
			// System.out.println("property" + property.toString());
			contain = true;
		}
		return contain;
	}

}
