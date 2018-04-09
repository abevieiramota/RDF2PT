package org.aksw.rdf2pt;

import static org.junit.Assert.assertEquals;

import org.aksw.rdf2pt.triple2nl.gender.DictionaryBasedGenderDetector;
import org.aksw.rdf2pt.triple2nl.gender.Gender;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class DictionaryBasedGenderDetectorTest {

	@Test
	public void testTarsila() {
		DictionaryBasedGenderDetector genderDetector = new DictionaryBasedGenderDetector();
		Gender gender = genderDetector.getGender("Tarsila do Amaral");
		
		assertEquals(Gender.FEMALE, gender);
	}
}
