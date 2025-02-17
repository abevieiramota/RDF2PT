package org.aksw.rdf2pt.utils.nlp.pos;
//package org.aksw.sw2pt.utils.nlp.pos;
//
//import java.util.HashMap;
//import java.util.Map;
//
//public class PartOfSpeechTagNormalizer {
//
//	public final static Map<String,String> PART_OF_SPEECH_TAG_MAPPINGS = new HashMap<String,String>();
//
//	static {
//
//		PART_OF_SPEECH_TAG_MAPPINGS.put("CC",	"CC");
//		PART_OF_SPEECH_TAG_MAPPINGS.put("CD",	"CD");
//		PART_OF_SPEECH_TAG_MAPPINGS.put("DT",	"DT");
//		PART_OF_SPEECH_TAG_MAPPINGS.put("EX",	"EX");
//		PART_OF_SPEECH_TAG_MAPPINGS.put("FW",	"FW");
//		PART_OF_SPEECH_TAG_MAPPINGS.put("IN",	"IN");
//		PART_OF_SPEECH_TAG_MAPPINGS.put("JJ",	"JJ");
//		PART_OF_SPEECH_TAG_MAPPINGS.put("JJR",	"JJR");
//		PART_OF_SPEECH_TAG_MAPPINGS.put("JJS",	"JJS");
//		PART_OF_SPEECH_TAG_MAPPINGS.put("LS",	"LS");
//		PART_OF_SPEECH_TAG_MAPPINGS.put("MD",	"MD");
//		PART_OF_SPEECH_TAG_MAPPINGS.put("NN",	"NN");
//		PART_OF_SPEECH_TAG_MAPPINGS.put("NNP",  "NNP");
//		PART_OF_SPEECH_TAG_MAPPINGS.put("NNPS",  "NNPS");
//		PART_OF_SPEECH_TAG_MAPPINGS.put("NNS",	"NNS");
//		PART_OF_SPEECH_TAG_MAPPINGS.put("NP",	"NP");
//		PART_OF_SPEECH_TAG_MAPPINGS.put("NPS",	"NPS");
//		PART_OF_SPEECH_TAG_MAPPINGS.put("PDT",	"PDT");
//		PART_OF_SPEECH_TAG_MAPPINGS.put("POS",	"POS");
//		PART_OF_SPEECH_TAG_MAPPINGS.put("PP",	"PP");
//		PART_OF_SPEECH_TAG_MAPPINGS.put("PP$",	"PP$");
//		PART_OF_SPEECH_TAG_MAPPINGS.put("PRP",  "PRP");
//		PART_OF_SPEECH_TAG_MAPPINGS.put("PRP$", "PRP$");
//		PART_OF_SPEECH_TAG_MAPPINGS.put("RB",	"RB");
//		PART_OF_SPEECH_TAG_MAPPINGS.put("RBR",	"RBR");
//		PART_OF_SPEECH_TAG_MAPPINGS.put("RBS",	"RBS");
//		PART_OF_SPEECH_TAG_MAPPINGS.put("RP",	"RP");
//		PART_OF_SPEECH_TAG_MAPPINGS.put("SYM",	"SYM");
//		PART_OF_SPEECH_TAG_MAPPINGS.put("TO",	"TO");
//		PART_OF_SPEECH_TAG_MAPPINGS.put("UH",	"UH");
//		PART_OF_SPEECH_TAG_MAPPINGS.put("VB",	"VB");
//		PART_OF_SPEECH_TAG_MAPPINGS.put("VBD",	"VBD");
//		PART_OF_SPEECH_TAG_MAPPINGS.put("VBG",	"VBG");
//		PART_OF_SPEECH_TAG_MAPPINGS.put("VBN",	"VBN");
//		PART_OF_SPEECH_TAG_MAPPINGS.put("VBP",	"VBP");
//		PART_OF_SPEECH_TAG_MAPPINGS.put("VBZ",	"VBZ");
//		PART_OF_SPEECH_TAG_MAPPINGS.put("WDT",	"WDT");
//		PART_OF_SPEECH_TAG_MAPPINGS.put("WP",	"WP");
//		PART_OF_SPEECH_TAG_MAPPINGS.put("WP$",	"WP$");
//		PART_OF_SPEECH_TAG_MAPPINGS.put("WRB",	"WRB");
//
//		PART_OF_SPEECH_TAG_MAPPINGS.put(".",	".");
//		PART_OF_SPEECH_TAG_MAPPINGS.put(",",	",");
//		PART_OF_SPEECH_TAG_MAPPINGS.put("!",	"!");
//		PART_OF_SPEECH_TAG_MAPPINGS.put("?",	"?");
//		PART_OF_SPEECH_TAG_MAPPINGS.put(";",	";");
//		PART_OF_SPEECH_TAG_MAPPINGS.put("''",	"''");
//		PART_OF_SPEECH_TAG_MAPPINGS.put("``",	"``");
//		PART_OF_SPEECH_TAG_MAPPINGS.put(":",	":");
//		PART_OF_SPEECH_TAG_MAPPINGS.put("$",    "$");
//		PART_OF_SPEECH_TAG_MAPPINGS.put("#",    "#");
//		PART_OF_SPEECH_TAG_MAPPINGS.put("-LRB-","-LRB-");
//        PART_OF_SPEECH_TAG_MAPPINGS.put("-RRB-","-RRB-");
//	}
//}
