package com.jmcejuela.bio.jenia;

import static com.jmcejuela.bio.jenia.util.Util.tokenize;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jmcejuela.bio.jenia.common.Sentence;
import com.jmcejuela.bio.jenia.common.Token;
import com.jmcejuela.bio.jenia.maxent.ME_Model;

public class JeniaTagger implements Serializable{
	private Logger logger = LoggerFactory.getLogger(JeniaTagger.class);
	private static final long serialVersionUID = 1L;
	/**
	 * Set the local path where the model files needed by jeniatagger can be
	 * found.
	 * 
	 * This method must be called before using any analyze method. @see
	 * https://github.com/jmcejuela/jeniatagger
	 * 
	 * @param path
	 */

	private ArrayList<ME_Model> posModels = null;
	private ArrayList<ME_Model> chunkingModels = null;
	private Chunking chunking;
	private Bidir bidir;
	private MorphDic morphDic;
	private NamedEntity namedEntity;
	private String posModelsPath = "/models_medline/model.bidir.";
	private String chunkingModelsPath = "/models_chunking/model.bidir.";

	public JeniaTagger(String modelsPath) {
		posModels = new ArrayList<ME_Model>();
		for (int i = 0; i < 16; i++) {
			ME_Model posModel = new ME_Model(modelsPath);
			logger.debug("Loading posModels from " + modelsPath + posModelsPath + i);
			posModel.load_from_file(posModelsPath + i);
			posModels.add(posModel);
		}

		chunkingModels = new ArrayList<ME_Model>();
		for (int i = 0; i < 8; i += 2) {
			ME_Model chunkingModel = new ME_Model(modelsPath);
			logger.debug("Loading chunkingModels from " + modelsPath + chunkingModelsPath + i);
			chunkingModel.load_from_file(chunkingModelsPath + i);
			chunkingModels.add(chunkingModel);
		}
		bidir = new Bidir(modelsPath);
	}

	public Sentence analyzeAll(final String line, boolean dont_tokenize) {
		Sentence sentence = analyzePos(line, dont_tokenize);

		sentence = chunking
				.bidir_chunking_decode_beam(sentence, chunkingModels);
		setBaseForm(sentence);
		namedEntity.netagging(sentence);

		return sentence;
	}

	public Sentence analyzePosAndChunk(final String line, boolean dont_tokenize) {
		Sentence sentence = analyzePos(line, dont_tokenize);

		chunking.bidir_chunking_decode_beam(sentence, chunkingModels);

		return sentence;
	}

	public Sentence analyzePos(final String line, boolean dont_tokenize) {
		if (line.matches(".*[\n\r\u0085\u2028\u2029].*"))
			throw new IllegalArgumentException(
					"The input line cannot have any line terminator");
		String trimmedLine = line.trim();
		if (trimmedLine.isEmpty())
			return new Sentence();

		Sentence sentence = createSentence(line, dont_tokenize, trimmedLine);
		sentence = bidir.bidir_decode_beam(sentence, posModels);
		return sentence;
	}

	private static Sentence createSentence(final String line,
			boolean dont_tokenize, String trimmedLine) {
		List<String> tokens = (dont_tokenize) ? Arrays.asList(trimmedLine.split("\\s+")) // jenia: see genia's README
				: tokenize(line);

		Sentence sentence = new Sentence(tokens.size());
		for (String tokenText : tokens) {
			// s = ParenConverter.Ptb2Pos(s);
			sentence.add(new Token(tokenText));
		}
		return sentence;
	}

	private void setBaseForm(Sentence sentence) {
		for (Token t : sentence) {
			t.baseForm = morphDic.base_form(t.text, t.pos);
		}
	}
}
