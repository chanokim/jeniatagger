package com.jmcejuela.bio.jenia;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MorphDicTest {

	private MorphDic morphDic;
	
  @Before
  public void setUpClass() {
	  this.morphDic = new MorphDic("src/test/resources/models");
  }

  @Test
  public void testMorphDicLoadEx() {
    testMap(morphDic.getVerbex(), 5254);
    testMap(morphDic.getAdjex(), 1325);
    testMap(morphDic.getAdvex(), 6);
    testMap(morphDic.getNounex(), 5974);
  }

  @Test
  public void testMorphDicLoadIdx() {
    testSet(morphDic.getAdjdic(), 19101);
    testSet(morphDic.getNoundic(), 87642);
    testSet(morphDic.getVerbdic(), 14727);
  }

  /**
   * The size of each dic must be the file's number of lines times 2 - number of elems that do not start with a letter
   * (see alg.)
   */
  public void testSet(Set<String> set, int numLinesFile) {
    assertEquals(numLinesFile * 2 - nonLetterStart(set), set.size());
    for (String elem : set) {
      assertTrue(elem != null);
    }
  }

  public void testMap(Map<String, String> map, int numLinesFile) {
    testSet(map.keySet(), numLinesFile);
    for (String value : map.values()) {
      assertTrue(value != null);
    }
  }

  public int nonLetterStart(Set<String> set) {
    int ret = 0;
    for (String elem : set) {
      if (!Character.isLetter(elem.charAt(0)))
        ret++;
    }
    return ret;
  }

}
