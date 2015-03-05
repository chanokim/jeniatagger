package com.jmcejuela.bio.jenia;

import org.junit.Before;
import org.junit.Test;

import com.jmcejuela.bio.jenia.common.Sentence;
import com.jmcejuela.bio.jenia.common.Token;

public class JeniaTaggerTest {

	private JeniaTagger jeniaTagger;
	
	private String abstractString = "Public health recommendations for the US population in 1977 were to reduce fat intake to as low as 30% of calories to lower the incidence of coronary artery disease\n" + 
			"These recommendations resulted in a compositional shift in food materials throughout the agricultural industry, and the fractional content of fats was replaced principally with carbohydrates\n" + 
			"Subsequently, high-carbohydrate diets were recognized as contributing to the lipoprotein pattern that characterizes atherogenic dyslipidemia and hypertriacylglycerolemia\n" + 
			"The rising incidences of metabolic syndrome and obesity are becoming common themes in the literature\n" + 
			"Current recommendations are to keep saturated fatty acid, trans fatty acid, and cholesterol intakes as low as possible while consuming a nutritionally adequate diet\n" + 
			"In the face of such recommendations, the agricultural industry is shifting food composition toward lower proportions of all saturated fatty acids\n" + 
			"To date, no lower safe limit of specific saturated fatty acid intakes has been identified\n" + 
			"This review summarizes research findings and observations on the disparate functions of saturated fatty acids and seeks to bring a more quantitative balance to the debate on dietary saturated fat\n" + 
			"Whether a finite quantity of specific dietary saturated fatty acids actually benefits health is not yet known\n" + 
			"Because agricultural practices to reduce saturated fat will require a prolonged and concerted effort, and because the world is moving toward more individualized dietary recommendations, should the steps to decrease saturated fatty acids to as low as agriculturally possible not wait until evidence clearly indicates which amounts and types of saturated fatty acids are optimal?";
	
	@Before
	public void setUp() {
		jeniaTagger = new JeniaTagger("/models");
	}
	
	@Test
	public void testSimple() {
		Sentence sentence = jeniaTagger.analyzePos("This is a drum", true);
		for(Token token: sentence) {
			System.out.println(token.text + "|" + token.pos);
		}
	}
	@Test
	public void testAbstractPOS() {
		Sentence sentence = jeniaTagger.analyzePos(abstractString, 
				true);
		for(Token token: sentence) {
			System.out.println(token.text + "|" + token.pos);
		}
	}

	@Test
	public void testMultipleTimes() { // to make sure it initializes only once.

		Sentence sentence = jeniaTagger.analyzePos(abstractString, true);
		System.out.println("analyzsed once");
		sentence = jeniaTagger.analyzePos(abstractString, true);
		System.out.println("analyzsed twice");
		sentence = jeniaTagger.analyzePos(abstractString, true);
		sentence = jeniaTagger.analyzePos(abstractString, true);
		sentence = jeniaTagger.analyzePos(abstractString, true);
		sentence = jeniaTagger.analyzePos(abstractString, true);
		sentence = jeniaTagger.analyzePos(abstractString, true);
		System.out.println("analyzsed seven times");
		for(Token token: sentence) {
			System.out.println(token.text + "|" + token.pos);
		}
	}
}
