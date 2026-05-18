package com.example.mad_pop;

import com.example.mad_pop.util.SearchUtil;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExampleUnitTest {
    @Test
    public void searchMatcher_handlesEmptyAndKeywordQuery() {
        assertTrue(SearchUtil.matches("", "Android", "Career"));
        assertTrue(SearchUtil.matches("sql", "Android", "SQL Mastery"));
        assertFalse(SearchUtil.matches("design", "Android", "SQL Mastery"));
    }
}