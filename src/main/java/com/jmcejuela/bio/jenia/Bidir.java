package com.jmcejuela.bio.jenia;

import static com.jmcejuela.bio.jenia.util.Util.last;
import static com.jmcejuela.bio.jenia.util.Util.newArrayList;
import static java.lang.Character.isDigit;
import static java.lang.Character.isUpperCase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.jmcejuela.bio.jenia.common.Sentence;
import com.jmcejuela.bio.jenia.maxent.ME_Model;
import com.jmcejuela.bio.jenia.maxent.ME_Sample;
import com.jmcejuela.bio.jenia.util.Tuple2;

/**
 * From bidir.cpp
 */
public class Bidir implements Serializable {

	private static final long serialVersionUID = 1L;
	final int UPDATE_WINDOW_SIZE = 2;
	final int BEAM_NUM = 1;
	private String modelsPath;

	public Bidir(String modlesPath) {
		this.modelsPath = modelsPath;
	}

	public static ME_Sample mesample(final Sentence sentence, int i,
			final String pos_left2, final String pos_left1,
			final String pos_right1, final String pos_right2) {
		final boolean ONLY_VERTICAL_FEATURES = false;
		ME_Sample sample = new ME_Sample("?");

		String token = sentence.get(i).text;

		sample.features.add("W0_" + token);
		String prestr = "BOS";
		if (i > 0)
			prestr = sentence.get(i - 1).text;
		String poststr = "EOS";
		if (i < sentence.size() - 1)
			poststr = sentence.get(i + 1).text;

		if (!ONLY_VERTICAL_FEATURES) {
			sample.features.add("W-1_" + prestr);
			sample.features.add("W+1_" + poststr);

			sample.features.add("W-10_" + prestr + "_" + token);
			sample.features.add("W0+1_" + token + "_" + poststr);
		}

		int limit = Math.min(token.length(), 10);
		for (int j = 1; j <= limit; j++) {
			sample.features.add("suf" + j + "_"
					+ token.substring(token.length() - j));
			sample.features.add("pre" + j + "_" + token.substring(0, j));
		}
		// L
		if (!pos_left1.isEmpty()) {
			sample.features.add("P-1_" + pos_left1);
			sample.features.add("P-1W0_" + pos_left1 + "_" + token);
		}
		// L2
		if (!pos_left2.isEmpty()) {
			sample.features.add("P-2_" + pos_left2);
		}
		// R
		if (!pos_right1.isEmpty()) {
			sample.features.add("P+1_" + pos_right1);
			sample.features.add("P+1W0_" + pos_right1 + "_" + token);
		}
		// R2
		if (!pos_right2.isEmpty()) {
			sample.features.add("P+2_" + pos_right2);
		}
		// LR
		if (!pos_left1.isEmpty() && !pos_right1.isEmpty()) {
			sample.features.add("P-1+1_" + pos_left1 + "_" + pos_right1);
			sample.features.add("P-1W0P+1_" + pos_left1 + "_" + token + "_"
					+ pos_right1);
		}
		// LL
		if (!pos_left1.isEmpty() && !pos_left2.isEmpty()) {
			sample.features.add("P-2-1_" + pos_left2 + "_" + pos_left1);
		}
		// RR
		if (!pos_right1.isEmpty() && !pos_right2.isEmpty()) {
			sample.features.add("P+1+2_" + pos_right1 + "_" + pos_right2);
		}
		// LLR
		if (!pos_left1.isEmpty() && !pos_left2.isEmpty()
				&& !pos_right1.isEmpty()) {
			sample.features.add("P-2-1+1_" + pos_left2 + "_" + pos_left1 + "_"
					+ pos_right1);
		}
		// LRR
		if (!pos_left1.isEmpty() && !pos_right1.isEmpty()
				&& !pos_right2.isEmpty()) {
			sample.features.add("P-1+1+2_" + pos_left1 + "_" + pos_right1 + "_"
					+ pos_right2);
		}
		// LLRR
		if (!pos_left2.isEmpty() && !pos_left1.isEmpty()
				&& !pos_right1.isEmpty() && !pos_right2.isEmpty()) {
			sample.features.add("P-2-1+1+2_" + pos_left2 + "_" + pos_left1
					+ "_" + pos_right1 + "_" + pos_right2);
		}

		for (int j = 0; j < token.length(); j++) {
			if (isDigit(token.charAt(j))) {
				sample.features.add("CONTAIN_NUMBER");
				break;
			}
		}
		for (int j = 0; j < token.length(); j++) {
			if (isUpperCase(token.charAt(j))) {
				sample.features.add("CONTAIN_UPPER");
				break;
			}
		}
		for (int j = 0; j < token.length(); j++) {
			if (token.charAt(j) == '-') {
				sample.features.add("CONTAIN_HYPHEN");
				break;
			}
		}

		boolean allupper = true;
		for (int j = 0; j < token.length(); j++) {
			if (!isUpperCase(token.charAt(j))) {
				allupper = false;
				break;
			}
		}
		if (allupper)
			sample.features.add("ALL_UPPER");

		return sample;
	}

	private int bidir_train(final ArrayList<Sentence> vs, int para) {

		for (int t = 0; t < 16; t++) {
			if (t != 15 && t != 0)
				continue;
			// for (int t = 15; t >= 0; t--) {
			ArrayList<ME_Sample> train = newArrayList();

			if (para != -1 && t % 4 != para)
				continue;
			int n = 0;
			for (Sentence s : vs) {
				for (int j = 0; j < s.size(); j++) {
					String pos_left1 = "BOS", pos_left2 = "BOS2";
					if (j >= 1)
						pos_left1 = s.get(j - 1).pos;
					if (j >= 2)
						pos_left2 = s.get(j - 2).pos;
					String pos_right1 = "EOS", pos_right2 = "EOS2";
					if (j <= s.size() - 2)
						pos_right1 = s.get(j + 1).pos;
					if (j <= s.size() - 3)
						pos_right2 = s.get(j + 2).pos;
					if ((t & 0x8) == 0)
						pos_left2 = "";
					if ((t & 0x4) == 0)
						pos_left1 = "";
					if ((t & 0x2) == 0)
						pos_right1 = "";
					if ((t & 0x1) == 0)
						pos_right2 = "";

					train.add(mesample(s, j, pos_left2, pos_left1, pos_right1,
							pos_right2));
				}
			}

			ME_Model m = new ME_Model(modelsPath);
			m.train(train, 2, 0, 1);
			String filename = "model.bidir." + t;
			m.save_to_file(filename);
		}

		return 0; // jenia: original didn't return explicitly
	}


	/**
	 * jenia tag_dictionary discarded as never used
	 *
	 * @param order
	 * @param h
	 * @param vme
	 * @param vh
	 */
	void generate_hypotheses(final int order, final Hypothesis h,
	// final multimap<String, String> tag_dictionary,
			final ArrayList<ME_Model> vme, List<Hypothesis> vh) {
		int n = h.sentence.size();
		int pred_position = -1;
		double min_ent = 999999;
		// String pred = ""; //jenia
		// double pred_prob = 0; //jenia
		for (int j = 0; j < n; j++) {
			if (!h.sentence.get(j).pos.isEmpty())
				continue;
			double ent = h.entropies.get(j);
			if (ent < min_ent) {
				min_ent = ent;
				pred_position = j;
			}
		}
		assert (pred_position >= 0 && pred_position < n);

		for (Tuple2<String, Double> k : h.vvp.get(pred_position)) {
			Hypothesis newh = h.copy();

			newh.sentence.get(pred_position).pos = k._1;
			newh.order.set(pred_position, order + 1);
			newh.prob = h.prob * k._2;

			// update the neighboring predictions
			for (int j = pred_position - UPDATE_WINDOW_SIZE; j <= pred_position
					+ UPDATE_WINDOW_SIZE; j++) {
				if (j < 0 || j > n - 1)
					continue;
				if (newh.sentence.get(j).pos.isEmpty())
					newh.Update(j, vme);
			}
			vh.add(newh);
		}
	}

	/**
	 * tag_dictionary discarded
	 *
	 * @param sentence
	 * @param posModels
	 */
	Sentence bidir_decode_beam(Sentence sentence,
			final ArrayList<ME_Model> posModels) {
		int n = sentence.size();
		if (n == 0)
			return null;

		ArrayList<Hypothesis> hypotheses = newArrayList();
		Hypothesis hyp = new Hypothesis(sentence, posModels);
		hypotheses.add(hyp);

		for (int i = 0; i < n; i++) {
			ArrayList<Hypothesis> newHypotheses = newArrayList();
			for (Hypothesis j : hypotheses) {
				generate_hypotheses(i, j, posModels, newHypotheses);
			}
			Collections.sort(newHypotheses, new HypothesisOrder());
			while (newHypotheses.size() > BEAM_NUM) {
				newHypotheses.remove(0);
			}
			hypotheses = newHypotheses;
		}

		hyp = last(hypotheses);
		for (int k = 0; k < n; k++) {
			sentence.get(k).pos = hyp.sentence.get(k).pos;
		}
		return sentence;
	}

}
