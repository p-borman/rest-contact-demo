package org.borman.util;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;

public class AlphanumComparatorTest {
    String blah1 = "blah 1";
    String blah10 = "blah 10";
    String blah11 = "blah 11";
    String blah100 = "blah 100";
    String blah2 = "blah 2";
    private ArrayList<String> strings;

    @Before
    public void beforeEach() {
        strings = new ArrayList<String>();
        strings.add(blah10);
        strings.add(blah1);
        strings.add(blah11);
        strings.add(blah100);
        strings.add(blah2);
    }

    @Test
    public void testShouldSortStringsWithNumbersUsingBaseStringCompare() {
        strings.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });

        assertThat(strings).hasSize(5);
        assertThat(strings.get(0)).isEqualTo(blah1);
        assertThat(strings.get(1)).isEqualTo(blah10);
        assertThat(strings.get(2)).isEqualTo(blah100);
        assertThat(strings.get(3)).isEqualTo(blah11);
        assertThat(strings.get(4)).isEqualTo(blah2);
    }

    @Test
    public void testShouldSortStringsWithNumbersUsingAlphaNumStringCompare() {
        strings.sort(new AlphanumComparator());

        assertThat(strings).hasSize(5);
        assertThat(strings.get(0)).isEqualTo(blah1);
        assertThat(strings.get(1)).isEqualTo(blah2);
        assertThat(strings.get(2)).isEqualTo(blah10);
        assertThat(strings.get(3)).isEqualTo(blah11);
        assertThat(strings.get(4)).isEqualTo(blah100);
    }

    @Test
    public void testShouldSortComplexStringsWithNumbersUsingAlphaNumStringCompare() {
        final String add1 = "123 OAK ST";
        final String add2 = "200 W GALBRAITH RD";
        final String add3 = "21 W GALBRAITH RD";
        final String add4 = "1 W GALBRAITH RD";
        final String add5 = "60 South 700 East";
        final String add6 = "600 South 700 East";
        final String add7 = "600 South 70 East";
        strings = new ArrayList<String>();
        strings.add(add1);
        strings.add(add2);
        strings.add(add3);
        strings.add(add4);
        strings.add(add5);
        strings.add(add6);
        strings.add(add7);

        strings.sort(new AlphanumComparator());

        assertThat(strings).hasSize(7);
        assertThat(strings.get(0)).isEqualTo(add4);
        assertThat(strings.get(1)).isEqualTo(add3);
        assertThat(strings.get(2)).isEqualTo(add5);
        assertThat(strings.get(3)).isEqualTo(add1);
        assertThat(strings.get(4)).isEqualTo(add2);
        assertThat(strings.get(5)).isEqualTo(add7);
        assertThat(strings.get(6)).isEqualTo(add6);
    }
}