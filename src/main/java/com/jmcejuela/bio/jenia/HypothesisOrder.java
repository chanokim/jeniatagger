package com.jmcejuela.bio.jenia;

import java.util.Comparator;

public class HypothesisOrder implements Comparator<Hypothesis> {
	@Override
	public int compare(Hypothesis o1, Hypothesis o2) {
		if (o1.prob < o2.prob)
			return -1;
		else if (o1.prob > o2.prob)
			return +1;
		else
			return 0;
	}
}
