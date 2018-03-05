package org.aksw.limes.core.io.preprocessing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;

import org.aksw.limes.core.exceptions.IllegalNumberOfParametersException;
import org.aksw.limes.core.io.cache.HybridCache;
import org.aksw.limes.core.io.cache.Instance;
import org.aksw.limes.core.io.preprocessing.functions.CleanIri;
import org.aksw.limes.core.io.preprocessing.functions.CleanNumber;
import org.aksw.limes.core.io.preprocessing.functions.Concat;
import org.aksw.limes.core.io.preprocessing.functions.RegexReplace;
import org.aksw.limes.core.io.preprocessing.functions.RemoveLanguageTag;
import org.aksw.limes.core.io.preprocessing.functions.RenameProperty;
import org.aksw.limes.core.io.preprocessing.functions.Replace;
import org.aksw.limes.core.io.preprocessing.functions.ToCelsius;
import org.aksw.limes.core.io.preprocessing.functions.ToFahrenheit;
import org.aksw.limes.core.io.preprocessing.functions.ToLowercase;
import org.aksw.limes.core.io.preprocessing.functions.ToUppercase;
import org.junit.Before;
import org.junit.Test;

public class PreprocessingFunctionsTest {
	public static final String TEST_INSTANCE = "http://dbpedia.org/resource/Ibuprofen";
	public static ArrayList<ArrayList<String>> toConcatInOrder;

	//=============== EXPECTED VALUES ==================================
	public static final String UPPERCASE_EXPECTED = "IBUPROFEN@DE";
	public static final String LOWERCASE_EXPECTED = "ibuprofen@de";
	public static final String REPLACE_EXPECTED = "Ibuprofen";
	public static final String REPLACE_EXPECTED2 = "Ibuprofen@en";
	public static final String REMOVELANGUAGETAG_EXPECTED = "testext";
	public static final String REGEX_REPLACE_EXPECTED = "Ibuprofen is a nonsteroidal anti-inflammatory drug derivative of propionic acid used for relieving pain, helping with fever and reducing inflammation.";
	public static final String CLEAN_IRI_EXPECTED = "label";
	public static final String CLEAN_NUMBER_EXPECTED = "10";
	public static final String TO_CELSIUS_EXPECTED = "-28.88888888888889";
	public static final String TO_FAHRENHEIT_EXPECTED = "-4.0";
	public static final TreeSet<String> CONCAT_EXPECTED = new TreeSet<String>(Arrays.asList("helloworld","goodbyeworld"));
	public static final TreeSet<String> CONCAT_EXPECTED_GLUE = new TreeSet<String>(Arrays.asList("hello world", "goodbye world"));
	public static final ArrayList<String>CONCAT_IN_ORDER_EXPECTED = new ArrayList<String>(Arrays.asList("a1b1c1","a1b1c2","a1b2c1","a1b2c2","a1b3c1","a1b3c2","a2b1c1","a2b1c2","a2b2c1","a2b2c2","a2b3c1","a2b3c2","a3b1c1","a3b1c2","a3b2c1","a3b2c2","a3b3c1","a3b3c2","a4b1c1","a4b1c2","a4b2c1","a4b2c2","a4b3c1","a4b3c2"));

	public static final String[] FUNCTION_CHAIN_1_EXPECTED = new String[]{"label1","label2"};
	public static final String[] FUNCTION_CHAIN_2_EXPECTED = new String[]{"label1","label2","glue=,"};
	public static final String[] FUNCTION_CHAIN_3_EXPECTED = new String[]{"label1","label2","glue= "};
	public static final String[] FUNCTION_CHAIN_4_EXPECTED = new String[]{""};

	//=============== PROPERTIES =======================================
	public static final String PROP_LABEL = "rdfs:label";
	public static final String PROP_TEST2 = "test2";
	public static final String PROP_ABSTRACT = "dbo:abstract";
	public static final String PROP_IRI = "iri";
	public static final String PROP_IRI_NO_HASHTAG = "irino#";
	public static final String PROP_NUMBER = "number";
	public static final String PROP_NUMBER_NOT_PARSEABLE = "numbernotparseable";
	public static final String PROP_CONCAT1 = "concat1";
	public static final String PROP_CONCAT2 = "concat2";
	public static final String PROP_CONCAT_RESULT = "concatresult";
	
	//=============== VALUES ===========================================
	public static final String PROP_NUMBER_VALUE = "10^^http://www.w3.org/2001/XMLSchema#positiveInteger";
	public static final String PROP_NUMBER_NOT_PARSEABLE_VALUE = "10.0.0.1^^http://www.w3.org/2001/XMLSchema#positiveInteger";
	public static final String PROP_TEMPERATURE = "temperature";
	public static final String PROP_TEMPERATURE_VALUE = "-20^^http://www.w3.org/2001/XMLSchema#decimal";
	public static final String PROP_ABSTRACT_VALUE = "Ibuprofen (/ˈaɪbjuːproʊfɛn/ or /aɪbjuːˈproʊfən/ EYE-bew-PROH-fən; from isobutylphenylpropanoic acid) is a nonsteroidal anti-inflammatory drug (NSAID) derivative of propionic acid used for relieving pain, helping with fever and reducing inflammation.@en";
	public static final String PROP_IRI_VALUE = "http://www.w3.org/2000/01/rdf-schema#label";
	public static final String PROP_IRI_NO_HASHTAG_VALUE = "http://www.w3.org/2000/01/rdf-schema/label";
	public static final String PROP_CONCAT1_VALUE1 = "hello";
	public static final String PROP_CONCAT1_VALUE2 = "goodbye";
	public static final String PROP_CONCAT2_VALUE = "world";
	public static final String PROP_LABEL_VALUE1 = "Ibuprofen@de";
	public static final String PROP_LABEL_VALUE2 = "Ibuprofen@en";
	
	public static final String FUNCTION_CHAIN_1 = "concat(label1,label2";
	public static final String FUNCTION_CHAIN_2 = "concat(label1,label2,glue=,";
	public static final String FUNCTION_CHAIN_3 = "concat(label1,label2,glue= ";
	public static final String FUNCTION_CHAIN_4 = "lowercase";

	//Used for RegexReplaceTest Removes everything inside braces and language tag
	public static final String REGEX = "\\((.*?)\\) |@\\w*"; 
	public Instance testInstance;

	@Before
	public void prepareData() {
		testInstance = new Instance(TEST_INSTANCE);

		TreeSet<String> labels = new TreeSet<>();
		labels.add(PROP_LABEL_VALUE1);
		labels.add(PROP_LABEL_VALUE2);
		testInstance.addProperty(PROP_LABEL, labels);

		testInstance.addProperty(PROP_TEST2, REMOVELANGUAGETAG_EXPECTED);
		testInstance.addProperty(PROP_ABSTRACT,PROP_ABSTRACT_VALUE);
		testInstance.addProperty(PROP_IRI,PROP_IRI_VALUE);
		testInstance.addProperty(PROP_NUMBER,PROP_NUMBER_VALUE);
		testInstance.addProperty(PROP_TEMPERATURE, PROP_TEMPERATURE_VALUE);
		testInstance.addProperty(PROP_NUMBER_NOT_PARSEABLE, PROP_NUMBER_NOT_PARSEABLE_VALUE);
		testInstance.addProperty(PROP_IRI_NO_HASHTAG, PROP_IRI_NO_HASHTAG_VALUE);

		TreeSet<String> concat1 = new TreeSet<>();
		concat1.add(PROP_CONCAT1_VALUE1);
		concat1.add(PROP_CONCAT1_VALUE2);
		testInstance.addProperty(PROP_CONCAT1, concat1);
		testInstance.addProperty(PROP_CONCAT2, PROP_CONCAT2_VALUE);

		
		
		toConcatInOrder = new ArrayList<>();
		toConcatInOrder.add(new ArrayList<String>(Arrays.asList("a1", "a2", "a3", "a4")));
		toConcatInOrder.add(new ArrayList<String>(Arrays.asList("b1", "b2", "b3")));
		toConcatInOrder.add(new ArrayList<String>(Arrays.asList("c1", "c2")));
	}

	@Test
	public void testHasPropertyPredicate() {
		Instance i = new Instance("uri");
		i.addProperty("a", "a");
		i.addProperty("b", "b");
		i.addProperty("c", "c");
		assertTrue(APreprocessingFunction.hasProperty("a").test(i));
	}

	@Test
	public void testHasPropertyPredicateWrongProperty() {
		Instance i = new Instance("uri");
		i.addProperty("a", "a");
		i.addProperty("b", "b");
		i.addProperty("c", "c");
		assertFalse(APreprocessingFunction.hasProperty("d").test(i));
	}

	@Test
	public void testHasPropertyPredicateMultipleProperties() {
		Instance i = new Instance("uri");
		i.addProperty("a", "a");
		i.addProperty("b", "b");
		i.addProperty("c", "c");
		assertTrue(APreprocessingFunction.hasProperty(new String[] { "a", "b", "d" }).test(i));
	}

	@Test
	public void testUppercase() throws IllegalNumberOfParametersException {
		new ToUppercase().applyFunction(testInstance, PROP_LABEL);
		assertEquals(UPPERCASE_EXPECTED,testInstance.getProperty(PROP_LABEL).first());
	}

	@Test
	public void testAProcessingFunctionWrongArgumentNumber() {
		try {
			// The relevant functionality is the same for all classes that
			// extend AProcessingFunction
			new ToUppercase().applyFunction(testInstance, PROP_LABEL, "bla");
			assertFalse(true);
		} catch (IllegalNumberOfParametersException e) {
		}
	}

	@Test
	public void testLowercase() throws IllegalNumberOfParametersException {
		new ToLowercase().applyFunction(testInstance, PROP_LABEL);
		assertEquals(LOWERCASE_EXPECTED,testInstance.getProperty(PROP_LABEL).first());
	}
	
	@Test
	public void testRetrieveArguments(){
		String[] args1 = new ToLowercase().retrieveArguments(FUNCTION_CHAIN_4);
		assertArrayEquals(FUNCTION_CHAIN_4_EXPECTED, args1);
	}

	@Test
	public void testRemoveLanguageTag() throws IllegalNumberOfParametersException {
		new RemoveLanguageTag().applyFunction(testInstance, PROP_LABEL);
		assertEquals(1,testInstance.getProperty(PROP_LABEL).size());
		assertEquals(REPLACE_EXPECTED,testInstance.getProperty(PROP_LABEL).first());
	}

	@Test
	public void testRemoveLanguageTagNoTagPresent() throws IllegalNumberOfParametersException {
		new RemoveLanguageTag().applyFunction(testInstance, PROP_TEST2);
		assertEquals(1,testInstance.getProperty(PROP_TEST2).size());
		assertEquals(REMOVELANGUAGETAG_EXPECTED,testInstance.getProperty(PROP_TEST2).first());
	}

	@Test
	public void testReplace() throws IllegalNumberOfParametersException {
		new Replace().applyFunction(testInstance, PROP_LABEL, "@de");
		assertTrue(testInstance.getProperty(PROP_LABEL).contains(REPLACE_EXPECTED));
		assertTrue(testInstance.getProperty(PROP_LABEL).contains(REPLACE_EXPECTED2));
	}
	
	@Test
	public void testRegexReplace() throws IllegalNumberOfParametersException {
		new RegexReplace().applyFunction(testInstance, PROP_ABSTRACT, REGEX, "");
		assertEquals(REGEX_REPLACE_EXPECTED,testInstance.getProperty(PROP_ABSTRACT).first());
	}

	@Test
	public void testCleanIri() throws IllegalNumberOfParametersException {
		new CleanIri().applyFunction(testInstance, PROP_IRI);
		assertEquals(CLEAN_IRI_EXPECTED,testInstance.getProperty(PROP_IRI).first());
	}

	@Test
	public void testCleanIriNoHashtag() throws IllegalNumberOfParametersException {
		new CleanIri().applyFunction(testInstance, PROP_IRI_NO_HASHTAG);
		assertEquals(CLEAN_IRI_EXPECTED,testInstance.getProperty(PROP_IRI_NO_HASHTAG).first());
	}

	@Test
	public void testCleanNumber() throws IllegalNumberOfParametersException {
		new CleanNumber().applyFunction(testInstance, PROP_NUMBER);
		assertEquals(CLEAN_NUMBER_EXPECTED,testInstance.getProperty(PROP_NUMBER).first());
	}
	
	@Test
	public void testCleanNumberNotParseable() throws IllegalNumberOfParametersException {
		new CleanNumber().applyFunction(testInstance, PROP_NUMBER_NOT_PARSEABLE);
		assertEquals("0",testInstance.getProperty(PROP_NUMBER_NOT_PARSEABLE).first());
	}
	
	@Test
	public void testCleanNumberNoNumberPresent() throws IllegalNumberOfParametersException {
		new CleanNumber().applyFunction(testInstance, PROP_LABEL);
		assertEquals("0",testInstance.getProperty(PROP_LABEL).first());
	}

	@Test
	public void testToCelsius() throws IllegalNumberOfParametersException {
		new ToCelsius().applyFunction(testInstance, PROP_TEMPERATURE);
		assertEquals(TO_CELSIUS_EXPECTED,testInstance.getProperty(PROP_TEMPERATURE).first());
	}

	@Test
	public void testToFahrenheit() throws IllegalNumberOfParametersException {
		new ToFahrenheit().applyFunction(testInstance, PROP_TEMPERATURE);
		assertEquals(TO_FAHRENHEIT_EXPECTED,testInstance.getProperty(PROP_TEMPERATURE).first());
	}
	
	@Test
	public void testRetrieveArgumentsConcat(){
		Concat concat = new Concat();
		String[] args1 = concat.retrieveArguments(FUNCTION_CHAIN_1);
		String[] args2 = concat.retrieveArguments(FUNCTION_CHAIN_2);
		String[] args3 = concat.retrieveArguments(FUNCTION_CHAIN_3);
		assertArrayEquals(FUNCTION_CHAIN_1_EXPECTED, args1);
		assertArrayEquals(FUNCTION_CHAIN_2_EXPECTED, args2);
		assertArrayEquals(FUNCTION_CHAIN_3_EXPECTED, args3);
	}
	
	@Test
	public void testConcatElementsInOrder(){
		assertEquals(CONCAT_IN_ORDER_EXPECTED, Concat.concatElementsInOrder(toConcatInOrder));
	}
	
	
	@Test
	public void testConcat() throws IllegalNumberOfParametersException {
		new Concat().applyFunction(testInstance, PROP_CONCAT_RESULT, PROP_CONCAT1, PROP_CONCAT2);
		assertEquals(CONCAT_EXPECTED,testInstance.getProperty(PROP_CONCAT_RESULT));
	}

	@Test
	public void testConcatGlue() throws IllegalNumberOfParametersException {
		new Concat().applyFunction(testInstance, PROP_CONCAT_RESULT, PROP_CONCAT1, PROP_CONCAT2, "glue= ");
		assertEquals(CONCAT_EXPECTED_GLUE,testInstance.getProperty(PROP_CONCAT_RESULT));
	}
	
	@Test
	public void testRenameProperty() throws IllegalNumberOfParametersException {
		String expected = PROP_LABEL.replace("rdfs:","");
		new RenameProperty().applyFunction(testInstance, PROP_LABEL, expected);
		assertTrue(testInstance.getAllProperties().contains(expected));
	}
	
}
