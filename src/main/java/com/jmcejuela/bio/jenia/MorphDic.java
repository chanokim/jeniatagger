package com.jmcejuela.bio.jenia;

import static java.lang.Character.toUpperCase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOError;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * From morphdic.cpp
 */
public class MorphDic {
	Map<String, String> verbex;
	Map<String, String> nounex;
	Map<String, String> advex;
	Map<String, String> adjex;

	Set<String> noundic;
	Set<String> verbdic;
	Set<String> adjdic;
	String modelsPath;

	public MorphDic(String modelsPath) {
		this.modelsPath = modelsPath;
		nounex = loadEx("morphdic/noun.exc");
		verbex = loadEx("morphdic/verb.exc");
		adjex = loadEx("morphdic/adj.exc");
		advex = loadEx("morphdic/adv.exc");
		noundic = loadIdx("morphdic/noun.dic");
		verbdic = loadIdx("morphdic/verb.dic");
		adjdic = loadIdx("morphdic/adj.dic");
	}

	private Map<String, String> loadEx(final String filename) {
		try {
			Map<String, String> ret = new HashMap<String, String>();
			Scanner sc = new Scanner(new FileInputStream(new File(modelsPath,
					filename)));
			while (sc.hasNextLine()) {
				String org = sc.next();
				String base = sc.next();
				ret.put(org, base);

				ret.put(toUpperCase(org.charAt(0)) + org.substring(1),
						toUpperCase(base.charAt(0)) + base.substring(1));

				sc.nextLine();
			}
			sc.close();
			return ret;
		} catch (Exception e) {
			throw new IOError(e);
		}
	}

	Set<String> loadIdx(final String filename) {
		try {
			Set<String> ret = new HashSet<String>();
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(new File(modelsPath, filename))));
			String line;
			while ((line = br.readLine()) != null) {
				if (line.charAt(0) == ' ')
					continue;

				String base = line.split(" ")[0];
				ret.add(base);

				String baseCapitalized = toUpperCase(base.charAt(0))
						+ base.substring(1);
				ret.add(baseCapitalized);
			}
			br.close();
			return ret;
		} catch (Exception e) {
			throw new IOError(e);
		}
	}

	boolean lookUpDicNoun(final String s) {
		return noundic.contains(s);
	}

	boolean lookUpDicVerb(final String s) {
		return verbdic.contains(s);
	}

	boolean lookUpDicAdj(final String s) {
		return adjdic.contains(s);
	}

	String baseFormNoun(final String s) {
		String ret = nounex.get(s);
		if (ret == null)
			return "";
		else
			return ret;
	}

	String baseFormVerb(final String s) {
		String ret = verbex.get(s);
		if (ret == null)
			return "";
		else
			return ret;
	}

	String baseFormAdj(final String s) {
		String ret = adjex.get(s);
		if (ret == null)
			return "";
		else
			return ret;
	}

	String baseFormAdv(final String s) {
		String ret = advex.get(s);
		if (ret == null)
			return "";
		else
			return ret;
	}

	String base_form_noun(final String s) {
		String ex = baseFormNoun(s);
		if (!ex.equals(""))
			return ex;

		int len = s.length();
		if (len > 1) {
			String suf1 = s.substring(len - 1);
			if (suf1.equals("s")) {
				if (lookUpDicNoun(s.substring(0, len - 1)))
					return s.substring(0, len - 1);
				// if (MorphDic.LookUpDicVerb(s.substring(0, len - 1))) return
				// s.substring(0, len - 1);
			}
		}
		if (len > 4) {
			String suf4 = s.substring(len - 4);
			if (suf4.equals("ches"))
				return s.substring(0, len - 4) + "ch";
			if (suf4.equals("shes"))
				return s.substring(0, len - 4) + "sh";
		}
		if (len > 3) {
			String suf3 = s.substring(len - 3);
			if (suf3.equals("ses"))
				return s.substring(0, len - 3) + "s";
			if (suf3.equals("xes"))
				return s.substring(0, len - 3) + "x";
			if (suf3.equals("zes"))
				return s.substring(0, len - 3) + "z";
			if (suf3.equals("men"))
				return s.substring(0, len - 3) + "man";
			if (suf3.equals("ies"))
				return s.substring(0, len - 3) + "y";
		}
		if (len > 1) {
			String suf1 = s.substring(len - 1);
			if (suf1.equals("s"))
				return s.substring(0, len - 1);
		}
		return s;
	}

	String base_form_verb(final String s) {
		String ex = baseFormVerb(s);
		if (!ex.equals(""))
			return ex;
		if (lookUpDicVerb(s))
			return s;

		int len = s.length();
		if (len > 3) {
			String suf3 = s.substring(len - 3);
			if (suf3.equals("ies"))
				return s.substring(0, len - 3) + "y";
			if (suf3.equals("ing")) {
				if (lookUpDicVerb(s.substring(0, len - 3)))
					return s.substring(0, len - 3);
				else
					return s.substring(0, len - 3) + "e";
			}
		}
		if (len > 2) {
			String suf2 = s.substring(len - 2);
			if (suf2.equals("es") || suf2.equals("ed")) {
				if (lookUpDicVerb(s.substring(0, len - 2)))
					return s.substring(0, len - 2);
				else
					return s.substring(0, len - 2) + "e";
			}
		}
		if (len > 1) {
			String suf1 = s.substring(len - 1);
			if (suf1.equals("s"))
				return s.substring(0, len - 1);
		}
		return s;
	}

	String base_form_adjective(final String s) {
		String ex = baseFormAdj(s);
		if (!ex.equals(""))
			return ex;

		int len = s.length();
		if (len > 3) {
			String suf3 = s.substring(len - 3);
			if (suf3.equals("est")) {
				if (lookUpDicAdj(s.substring(0, len - 3) + "e"))
					return s.substring(0, len - 3) + "e";
				else
					return s.substring(0, len - 3);
			}
		}
		if (len > 2) {
			String suf2 = s.substring(len - 2);
			if (suf2.equals("er")) {
				if (lookUpDicAdj(s.substring(0, len - 2) + "e"))
					return s.substring(0, len - 2) + "e";
				else
					return s.substring(0, len - 2);
			}
		}
		return s;
	}

	String base_form_adverb(final String s) {
		String ex = baseFormAdv(s);
		if (!ex.equals(""))
			return ex;

		return s;
	}

	public String base_form(final String s, final String pos) {
		if (pos.equals("NNS"))
			return base_form_noun(s);
		if (pos.equals("NNPS"))
			return base_form_noun(s);

		if (pos.equals("JJR"))
			return base_form_adjective(s);
		if (pos.equals("JJS"))
			return base_form_adjective(s);

		if (pos.equals("RBR"))
			return base_form_adverb(s);
		if (pos.equals("RBS"))
			return base_form_adverb(s);

		if (pos.equals("VBD"))
			return base_form_verb(s);
		if (pos.equals("VBG"))
			return base_form_verb(s);
		if (pos.equals("VBN"))
			return base_form_verb(s);
		if (pos.equals("VBP"))
			return base_form_verb(s);
		if (pos.equals("VBZ"))
			return base_form_verb(s);

		return s;
	}

	public Map<String, String> getVerbex() {
		return verbex;
	}

	public void setVerbex(Map<String, String> verbex) {
		this.verbex = verbex;
	}

	public Map<String, String> getNounex() {
		return nounex;
	}

	public void setNounex(Map<String, String> nounex) {
		this.nounex = nounex;
	}

	public Map<String, String> getAdvex() {
		return advex;
	}

	public void setAdvex(Map<String, String> advex) {
		this.advex = advex;
	}

	public Map<String, String> getAdjex() {
		return adjex;
	}

	public void setAdjex(Map<String, String> adjex) {
		this.adjex = adjex;
	}

	public Set<String> getNoundic() {
		return noundic;
	}

	public void setNoundic(Set<String> noundic) {
		this.noundic = noundic;
	}

	public Set<String> getVerbdic() {
		return verbdic;
	}

	public void setVerbdic(Set<String> verbdic) {
		this.verbdic = verbdic;
	}

	public Set<String> getAdjdic() {
		return adjdic;
	}

	public void setAdjdic(Set<String> adjdic) {
		this.adjdic = adjdic;
	}

}
