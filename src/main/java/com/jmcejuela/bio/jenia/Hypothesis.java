package com.jmcejuela.bio.jenia;

import static com.jmcejuela.bio.jenia.util.Util.newArrayList;
import static java.lang.Math.max;

import java.util.ArrayList;
import java.util.Comparator;

import com.jmcejuela.bio.jenia.common.Sentence;
import com.jmcejuela.bio.jenia.maxent.ME_Model;
import com.jmcejuela.bio.jenia.maxent.ME_Sample;
import com.jmcejuela.bio.jenia.util.Constructor;
import com.jmcejuela.bio.jenia.util.Tuple2;

/**
 * 
 * @author chanokim
 * @since 2015-02-11
 * 
 */
public class Hypothesis {
	final double BEAM_WINDOW = 0.01;

	Sentence sentence;
	ArrayList<Double> entropies;
	ArrayList<Integer> order;
	ArrayList<ArrayList<Tuple2<String, Double>>> vvp;
	double prob;
	private Bidir bidir;
	final Comparator<Hypothesis> hypothesisOrder = new HypothesisOrder();

	// jenia: standard java hasn't multimaps and the argument tagdic is
	// actually never used so it's discarded
	Hypothesis(final Sentence sentence, final ArrayList<ME_Model> vme) {
		prob = 1.0;
		this.sentence = sentence.copy();
		int n = this.sentence.size();
		entropies = newArrayList(n, 0.0);
		vvp = newArrayList(n,
				new Constructor<ArrayList<Tuple2<String, Double>>>() {
					@Override
					public ArrayList<Tuple2<String, Double>> neu() {
						return new ArrayList<Tuple2<String, Double>>();
					}
				});
		order = newArrayList(n, 0);
		for (int i = 0; i < n; i++) {
			this.sentence.get(i).pos = "";
			Update(i, vme);
		}
	}

	private Hypothesis() {
	};

	Hypothesis copy() {
		Hypothesis ret = new Hypothesis();
		ret.sentence = this.sentence.copy();
		/*
		 * The following can be done because Double, Integer, and Tuple2<String,
		 * Double> are immutable objects
		 */
		ret.entropies = new ArrayList<Double>(this.entropies);
		ret.order = new ArrayList<Integer>(this.order);
		ret.vvp = newArrayList(this.vvp.size());
		for (ArrayList<Tuple2<String, Double>> a : this.vvp) {
			ArrayList<Tuple2<String, Double>> reta = new ArrayList<Tuple2<String, Double>>(
					a);
			ret.vvp.add(reta);
		}
		ret.prob = this.prob;
		return ret;
	}

	@Override
	public String toString() {
		return "Hypothesis:" + sentence.toString() + "\n" + "    " + entropies
				+ "\n" + "    " + order + "\n" + "    " + vvp + "\n" + "    "
				+ prob + "\n";
	}

	final boolean operator_less(final Hypothesis h) {
		return prob < h.prob;
	}

	void Update(final int j, final ArrayList<ME_Model> vme) {
		String pos_left1 = "BOS", pos_left2 = "BOS2";
		if (j >= 1)
			pos_left1 = sentence.get(j - 1).pos; // maybe bug??
		// if (j >= 1 && !vt[j-1].isEmpty()) pos_left1 = vt[j-1].prd; //
		// this should be correct
		if (j >= 2)
			pos_left2 = sentence.get(j - 2).pos;
		String pos_right1 = "EOS", pos_right2 = "EOS2";
		if (j <= sentence.size() - 2)
			pos_right1 = sentence.get(j + 1).pos;
		if (j <= sentence.size() - 3)
			pos_right2 = sentence.get(j + 2).pos;

		ME_Sample mes = Bidir.mesample(sentence, j, pos_left2, pos_left1,
				pos_right1, pos_right2);

		ArrayList<Double> membp;
		ME_Model mep = null;
		int bits = 0;
		if (!pos_left2.isEmpty())
			bits += 8;
		if (!pos_left1.isEmpty())
			bits += 4;
		if (!pos_right1.isEmpty())
			bits += 2;
		if (!pos_right2.isEmpty())
			bits += 1;
		assert (bits >= 0 && bits < 16);
		mep = vme.get(bits);
		membp = mep.classify(mes);
		assert (!mes.label.isEmpty());
		entropies.set(j, entropy(membp));
		// vent[j] = -j;

		vvp.get(j).clear();
		double maxp = membp.get(mep.get_class_id(mes.label));
		// vp[j] = mes.label;
		for (int i = 0; i < mep.num_classes(); i++) {
			double p = membp.get(i);
			if (p > maxp * BEAM_WINDOW)
				vvp.get(j).add(Tuple2.$(mep.get_class_label(i), p));
		}
	}

	double entropy(final ArrayList<Double> v) {
		double maxp = 0;
		for (int i = 0; i < v.size(); i++) {
			if (v.get(i) == 0)
				continue;
			maxp = max(maxp, v.get(i));
		}
		return -maxp;
		/*
		 * jenia: the original calculated sum and had 2 return statements like
		 * this but sum was never effectively used
		 */
		// return -sum;
	}

}
