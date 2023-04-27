package com.crivano.jmodel;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class UtilsTest {
	@Test
	public void testGetVariableNames() {
		String input = "(country!\"\") == myCountry?has_content || tasty_food=='French fries'";
		List<String> expectedOutput = Arrays.asList("country", "myCountry", "tasty_food");
		List<String> actualOutput = Utils.getVariableNames(input);
		assertEquals(expectedOutput, actualOutput);
	}
}
