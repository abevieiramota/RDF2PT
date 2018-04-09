/*
 * #%L
 * Triple2NL
 * %%
 * Copyright (C) 2015 Agile Knowledge Engineering and Semantic Web (AKSW)
 * %%
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
 * #L%
 */
/**
 * 
 */
package org.aksw.rdf2pt.triple2nl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.rdf2pt.Sparql;
import org.aksw.rdf2pt.triple2nl.converter.DefaultIRIConverterPortuguese;
import org.aksw.rdf2pt.triple2nl.converter.IRIConverter;
import org.aksw.rdf2pt.triple2nl.gender.DictionaryBasedGenderDetector;
import org.aksw.rdf2pt.triple2nl.gender.Gender;
import org.aksw.rdf2pt.triple2nl.gender.GenderDetector;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;
import org.dllearner.kb.sparql.SparqlEndpoint;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

import simplenlg.features.Feature;
import simplenlg.features.InternalFeature;
import simplenlg.framework.CoordinatedPhraseElement;
import simplenlg.framework.DocumentElement;
import simplenlg.framework.NLGElement;
import simplenlg.framework.NLGFactory;
import simplenlg.lexicon.Lexicon;
import simplenlg.lexicon.portuguese.XMLLexicon;
import simplenlg.phrasespec.SPhraseSpec;
import simplenlg.realiser.Realiser;

/**
 * @author Diego Moussallem
 *
 */
public class DocumentGeneratorPortuguese {

	private TripleConverterPortuguese tripleConverter;
	private NLGFactory nlgFactory;
	private Realiser realiser;
	private GenderDetector genderDetector = new DictionaryBasedGenderDetector();
	private IRIConverter uriConverter;

	private boolean useAsWellAsCoordination = true;
	private static final boolean GENERATE_SUMMARY = true;

	public DocumentGeneratorPortuguese(SparqlEndpoint endpoint, String cacheDirectory) throws MalformedURLException {
		this(endpoint, cacheDirectory, new XMLLexicon());
	}

	public DocumentGeneratorPortuguese(SparqlEndpoint endpoint, String cacheDirectory, Lexicon lexicon)
			throws MalformedURLException {
		this(new QueryExecutionFactoryHttp(endpoint.getURL().toString(), endpoint.getDefaultGraphURIs()),
				cacheDirectory, lexicon);
	}

	public DocumentGeneratorPortuguese(QueryExecutionFactory qef, String cacheDirectory, Lexicon lexicon)
			throws MalformedURLException {
		this.tripleConverter = new TripleConverterPortuguese(qef, null, null, cacheDirectory, null, lexicon);
		this.nlgFactory = new NLGFactory(lexicon);
		this.realiser = new Realiser();
		this.uriConverter = new DefaultIRIConverterPortuguese(
				SparqlEndpoint.create("http://pt.dbpedia.org/sparql", "http://dbpedia.org"));
	}

	public String generateDocument(Model model) throws IOException {
		Set<Triple> triples = asTriples(model);
		return generateDocument(triples);
	}

	private Set<Triple> asTriples(Model model) {
		Set<Triple> triples = new HashSet<>((int) model.size());
		StmtIterator iterator = model.listStatements();
		while (iterator.hasNext()) {
			Statement statement = iterator.next();
			triples.add(statement.asTriple());
		}
		return triples;
	}

	public String generateDocument(Set<Triple> documentTriples) throws IOException {
		Map<Node, Collection<Triple>> subject2Triples = Multimaps.index(documentTriples, t -> t.getSubject()).asMap();
		// do some sorting
		subject2Triples = sort(documentTriples, subject2Triples);

		Sparql sparql = new Sparql();

		List<DocumentElement> sentences = new ArrayList<>();

		for (Entry<Node, Collection<Triple>> entry : subject2Triples.entrySet()) {
			Node s = entry.getKey();

			boolean isPersonDead = sparql.hasDeathPlace(s.toString());

			String mostGenericType = sparql.mostGenericClass(s.toString());
			String mostSpecificType = sparql.mostSpecificClass(s.toString());

			// a partir do label(orelse) tenta determinar o gênero
			Gender gender = this.genderDetector.getGender(this.uriConverter.convert(s.toString()));

			Set<Triple> typeTriples = Sets.newHashSet(Collections2.filter(entry.getValue(),
					t -> t.predicateMatches(RDF.type.asNode())));
			Set<Triple> otherTriples = Sets.newHashSet(Collections2.filter(entry.getValue(),
					t -> !t.predicateMatches(RDF.type.asNode())));

			CoordinatedPhraseElement conjunction = this.nlgFactory.createCoordinatedPhrase();

			List<SPhraseSpec> typePhrases = generateTypePhrases(isPersonDead, mostSpecificType, gender, typeTriples);

			for (SPhraseSpec typePhrase : typePhrases) {
				conjunction.addCoordinate(typePhrase);
			}

			Collection<SPhraseSpec> otherPhrases = generateOtherPhrases(isPersonDead, mostGenericType, mostSpecificType,
					gender,
					typeTriples, otherTriples);

			int count = 0;
			boolean isThereSecondSentence = false;

			CoordinatedPhraseElement conjunction2 = this.nlgFactory.createCoordinatedPhrase();
			for (SPhraseSpec phrase : otherPhrases) {
				if (count < 2) {
					conjunction.addCoordinate(phrase);
				} else if (count >= 2) {
					if (count == 2) {
						conjunction2.addPreModifier("Além disso,");
					}
					conjunction2.addCoordinate(phrase);
					isThereSecondSentence = true;
				}
				count++;
			}

			DocumentElement sentence = this.nlgFactory.createSentence(conjunction);
			sentences.add(sentence);
			
			if (isThereSecondSentence) {
				DocumentElement sentence2 = this.nlgFactory.createSentence(conjunction2);
				sentences.add(sentence2);
			}

		}
		
		DocumentElement paragraph = this.nlgFactory.createParagraph(sentences);

		String paragraphText = this.realiser.realise(paragraph).getRealisation();
		
		paragraphText = paragraphText.replaceAll("an ", "a ");
		paragraphText = paragraphText.replaceAll(", e ", " e ");
		
		return paragraphText;
	}

	private Collection<SPhraseSpec> generateOtherPhrases(boolean isPersonDead, String mostGenericType,
			String mostSpecificType,
			Gender gender, Set<Triple> typeTriples, Set<Triple> otherTriples) throws IOException {
		// Referring Expression Generation
		// convert the other triples, but use place holders for the subject
		String placeHolderToken = determinePlaceHolder(mostGenericType, mostSpecificType, gender);
		Node placeHolder = NodeFactory.createURI("http://sparql2nl.aksw.org/placeHolder/" + placeHolderToken);

		// Node placeHolder = subject;
		Collection<Triple> otherTriplesWPlaceHolder = new ArrayList<>(otherTriples.size());
		Iterator<Triple> iterator = otherTriples.iterator();
		// se n tem tripla com type, adiciona uma tripla à lista, sem substituir o subject pelo placeholder
		if (typeTriples.isEmpty() && iterator.hasNext()) {
			otherTriplesWPlaceHolder.add(iterator.next());
		}

		while (iterator.hasNext()) {
			Triple triple = iterator.next();
			Triple wPlaceHolderTriple = Triple.create(placeHolder, triple.getPredicate(), triple.getObject());

			otherTriplesWPlaceHolder.add(wPlaceHolderTriple);
		}

		Collection<SPhraseSpec> otherPhrases = tripleConverter.convertToPhrases(otherTriplesWPlaceHolder, isPersonDead,
				GENERATE_SUMMARY);
		return otherPhrases;
	}

	private List<SPhraseSpec> generateTypePhrases(boolean isPersonDead, String mostSpecificType, Gender gender,
			Set<Triple> typeTriples) throws IOException {
		// convert the type triples
		List<SPhraseSpec> typePhrases = this.tripleConverter.convertToPhrases(typeTriples, isPersonDead,
				GENERATE_SUMMARY);

		// if there are more than one types, we combine them in a single clause
		if (typePhrases.size() > 1) {
			typePhrases = combineTypePhrases(mostSpecificType, gender, typePhrases);
		}
		return typePhrases;
	}

	private String determinePlaceHolder(String mostGenericType, String mostSpecificType, Gender gender) {
		String placeHolderToken;

		if (mostGenericType.equals("http://dbpedia.org/ontology/Person")) {
			if (gender.name().equals("FEMALE")) {
				placeHolderToken = "dela";
			} else {
				placeHolderToken = "dele";
			}
		} else if (mostGenericType.equals("http://dbpedia.org/ontology/Organisation")) {
			if (mostSpecificType.equals("http://dbpedia.org/ontology/SoccerClub")) {
				placeHolderToken = "dele";
			} else {
				placeHolderToken = "dela";
			}

		} else if (mostGenericType.equals("http://dbpedia.org/ontology/PopulatedPlace")) {
			if (mostSpecificType.equals("http://dbpedia.org/ontology/City")) {
				placeHolderToken = "dela";
			} else {
				placeHolderToken = "dele";
			}
		} else if (mostGenericType.equals("http://dbpedia.org/ontology/ArchitecturalStructure")) {
			if (mostSpecificType.equals("http://dbpedia.org/ontology/Church")) {
				placeHolderToken = "dela";
			} else {
				placeHolderToken = "dele";
			}

		} else {
			placeHolderToken = "dele";
		}
		return placeHolderToken;
	}

	private List<SPhraseSpec> combineTypePhrases(String mostSpecificType, Gender gender,
			List<SPhraseSpec> typePhrases) {
		// combine all objects in a coordinated phrase
		CoordinatedPhraseElement combinedObject = nlgFactory.createCoordinatedPhrase();

		// here will be necessary to treat the gramatical gender of
		// subjects
		// the last 2 phrases are combined via 'assim como'
		if (useAsWellAsCoordination) {
			SPhraseSpec phrase1 = typePhrases.remove(typePhrases.size() - 1);
			SPhraseSpec phrase2 = typePhrases.get(typePhrases.size() - 1);
			// combine all objects in a coordinated phrase
			CoordinatedPhraseElement combinedLastTwoObjects = nlgFactory
					.createCoordinatedPhrase(phrase1.getObject(), phrase2.getObject());
			combinedLastTwoObjects.setConjunction("assim como");
			combinedLastTwoObjects.setFeature(Feature.RAISE_SPECIFIER, false);
			if (gender.name().equals("FEMALE")) {
				combinedLastTwoObjects.setFeature(InternalFeature.SPECIFIER, "uma"); // here
			} else {
				if (mostSpecificType.equals("http://dbpedia.org/ontology/SoccerClub")) {
					combinedLastTwoObjects.setFeature(InternalFeature.SPECIFIER, "um"); // here
				}
			}
			phrase2.setObject(combinedLastTwoObjects);
		}

		Iterator<SPhraseSpec> iterator = typePhrases.iterator();
		// pick first phrase as representative
		SPhraseSpec representative = iterator.next();
		combinedObject.addCoordinate(representative.getObject());

		while (iterator.hasNext()) {
			SPhraseSpec phrase = iterator.next();
			NLGElement object = phrase.getObject();
			combinedObject.addCoordinate(object);
		}

		combinedObject.setFeature(Feature.RAISE_SPECIFIER, true);

		// set the coordinated phrase as the object
		representative.setObject(combinedObject);
		// return a single phrase
		typePhrases = Lists.newArrayList(representative);
		return typePhrases;
	}

	/**
	 * @param documentTriples
	 *            the set of triples
	 * @param mapSubjectsToTriples
	 *            a map that contains for each node the triples in which it
	 *            occurs as subject
	 */
	// prefer subjects that do not occur in object position first
	private Map<Node, Collection<Triple>> sort(Set<Triple> documentTriples,
			Map<Node, Collection<Triple>> mapSubjectsToTriples) {
		Set<Node> subjectsThatAppearAsObject = new HashSet<>();

		for (Triple t : documentTriples) {
			if (mapSubjectsToTriples.containsKey(t.getObject())) {
				subjectsThatAppearAsObject.add(t.getObject());
			}
		}

		Map<Node, Collection<Triple>> sortedTriples = new LinkedHashMap<>();

		Entry<Node, Collection<Triple>> entry = null;
		for (Iterator<Entry<Node, Collection<Triple>>> iterator = mapSubjectsToTriples.entrySet().iterator(); iterator
				.hasNext(); entry = iterator.next()) {
			Node subject = entry.getKey();

			if (!subjectsThatAppearAsObject.contains(subject)) {
				sortedTriples.put(subject, entry.getValue());

				iterator.remove();
			}
		}
		// add the rest
		sortedTriples.putAll(mapSubjectsToTriples);

		return sortedTriples;
	}

	public static void main(String[] args) throws Exception {
		String triples = "@prefix dbr: <http://dbpedia.org/resource/>." + "@prefix dbo: <http://dbpedia.org/ontology/>."
				+ "@prefix xsd: <http://www.w3.org/2001/XMLSchema#>."
				+ "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>."

				+ "dbr:Albert_Einstein rdf:type dbo:Physican, dbo:Philosopher;" + "dbo:birthPlace dbr:Ulm;"
				+ "dbo:birthDate \"1879-03-14\"^^xsd:date;" + "dbo:academicAdvisor dbr:Heinrich_Friedrich_Weber;"
				// + "dbo:almaMater dbr:ETH_Zurich."; //fix from ex-aluno to
				// ex-instituto.
				// + "dbo:almaMater dbr:University_of_Zurich;"
				+ "dbo:award dbr:Max_Planck_Medal;"
				// + "dbo:award
				// dbr:Time_100:_The_Most_Important_People_of_the_Century;"
				// + "dbo:award dbr:Copley_Medal;"
				// + "dbo:award dbr:Nobel_Prize_in_Physics;"
				// + "dbo:award
				// dbr:Barnard_Medal_for_Meritorious_Service_to_Science;"
				// + "dbo:award dbr:Matteucci_Medal;"
				// + "dbo:award dbr:Royal_Society.";
				+ "dbo:citizenship dbr:Austria-Hungary.";
		// + "dbo:citizenship dbr:Kingdom_of_Württemberg;"
		// + "dbo:citizenship dbr:Statelessness;"
		// + "dbo:citizenship dbr:Switzerland;"
		// + "dbo:doctoralAdvisor dbr:Alfred_Kleiner.";
		// + "dbo:field dbr:Physics;"
		// + "dbo:field dbr:Philosophy;"
		// + "dbo:influenced dbr:Nathan_Rosen;"
		// + "dbo:influenced dbr:Leo_Szilard;"
		// + "dbo:influenced dbr:Ernst_G._Straus;"
		// + "dbo:knownFor dbr:Bose–Einstein_condensate;"
		// + "dbo:knownFor dbr:Brownian_motion;"
		// + "dbo:knownFor dbr:EPR_paradox;"
		// + "dbo:knownFor dbr:General_relativity;"
		// + "dbo:knownFor dbr:Photoelectric_effect;"
		// + "dbo:knownFor dbr:Special_relativity;"
		// + "dbo:knownFor dbr:Cosmological_constant;"
		// + "dbo:knownFor dbr:Mass–energy_equivalence;"
		// + "dbo:knownFor dbr:Gravitational_wave;"
		// + "dbo:knownFor dbr:Einstein_field_equations;"
		// + "dbo:knownFor dbr:Classical_unified_field_theories;"
		// + "dbo:knownFor dbr:Bose–Einstein_statistics;"
		// + "dbo:residence dbr:Switzerland;"
		// + "dbo:spouse dbr:Elsa_Einstein;"
		// + "dbo:spouse dbr:Mileva_Marić;"
		// + "dbo:deathPlace dbr:Princeton,_New_Jersey;"
		// + "dbo:deathDate \"1955-04-18\"^^xsd:date .";
		// + "dbr:Ulm rdf:type dbo:city.";
		// + "dbo:country dbr:Germany.";
		// + "";
		// + "dbo:federalState :Baden_Württemberg ."
		// + ":Leipzig a dbo:City;"
		// + "dbo:country :Germany;"
		// + "dbo:federalState :Saxony .";

		// String triples =
		// "@prefix dbr: <http://dbpedia.org/resource/>."
		// + "@prefix dbo: <http://dbpedia.org/ontology/>."
		// + "@prefix xsd: <http://www.w3.org/2001/XMLSchema#>."
		// + "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>."
		// + "dbr:Angela_Merkel rdf:type dbo:Scientist, dbo:OfficeHolder;"
		// + "dbo:birthPlace dbr:Hamburg.";
		// //+ "dbo:birthDate \"1954-07-17\"^^xsd:date,"
		// //+ "dbo:studiedIn dbr:Leipzig."
		// + "dbr:Hamburg rdf:type dbo:City;"
		// + "dbo:country dbr:Germany.";
		// //+ "dbr:Leipzig rdf:type dbo:City,"
		// //+ "dbo:country dbr:Germany,"
		// //+ "dbo:federalState dbr:Saxony.";

		Model model = ModelFactory.createDefaultModel();
		model.read(new ByteArrayInputStream(triples.getBytes()), null, "TURTLE");

		DocumentGeneratorPortuguese gen = new DocumentGeneratorPortuguese(
				SparqlEndpoint.create("http://pt.dbpedia.org/sparql", ""), "cache");
		String document = gen.generateDocument(model);
		System.out.println(document);
	}

}
