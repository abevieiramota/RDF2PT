/*
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is "Simplenlg".
 *
 * The Initial Developer of the Original Code is Ehud Reiter, Albert Gatt and Dave Westwater.
 * Portions created by Ehud Reiter, Albert Gatt and Dave Westwater are Copyright (C) 2010-11 The University of Aberdeen. All Rights Reserved.
 *
 * Contributor(s): Ehud Reiter, Albert Gatt, Dave Wewstwater, Roman Kutlak, Margaret Mitchell.
 */
package org.aksw.sw2pt.morphosyntax;

import org.junit.Before;

import junit.framework.TestCase;
import simplenlg.framework.NLGFactory;
import simplenlg.lexicon.Lexicon;
import simplenlg.lexicon.portuguese.XMLLexicon;
import simplenlg.phrasespec.NPPhraseSpec;
import simplenlg.phrasespec.SPhraseSpec;
import simplenlg.phrasespec.VPPhraseSpec;
import simplenlg.realiser.Realiser;

/**
 * This class is the base class for all JUnit test cases in this package.
 * 
 * @author R. de Oliveira, University of Aberdeen.
 */
public abstract class Setup extends TestCase {

	/** The realiser. */
	Realiser realiser;

	NLGFactory phraseFactory;

	Lexicon lexicon;

	VPPhraseSpec cantar, vender, partir;

	NPPhraseSpec eu, tu, voce, ela, ele, nos, aGente, vos, voces, elas, eles;

	SPhraseSpec clause;

	/**
	 * Instantiates a new SimpleNLG test.
	 * 
	 * @param name
	 *            the name
	 */
	public Setup(String name) {
		super(name);
	}

	/**
	 * Set up the variables we'll need for this SimpleNLG test to run (Called
	 * automatically by JUnit)
	 */
	@Override
	@Before
	protected void setUp() {
		lexicon = new XMLLexicon();
		this.phraseFactory = new NLGFactory(this.lexicon);
		this.realiser = new Realiser();

		this.cantar = this.phraseFactory.createVerbPhrase("cantar");
		this.vender = this.phraseFactory.createVerbPhrase("vender");
		this.partir = this.phraseFactory.createVerbPhrase("partir");

		this.eu = this.phraseFactory.createNounPhrase("eu");
		this.tu = this.phraseFactory.createNounPhrase("tu");
		this.voce = this.phraseFactory.createNounPhrase("você");
		this.ela = this.phraseFactory.createNounPhrase("ela");
		this.ele = this.phraseFactory.createNounPhrase("ele");
		this.nos = this.phraseFactory.createNounPhrase("nós");
		this.aGente = this.phraseFactory.createNounPhrase("a gente");
		this.vos = this.phraseFactory.createNounPhrase("vós");
		this.voces = this.phraseFactory.createNounPhrase("vocês");
		this.elas = this.phraseFactory.createNounPhrase("elas");
		this.eles = this.phraseFactory.createNounPhrase("eles");

		this.clause = this.phraseFactory.createClause();
	}
}
