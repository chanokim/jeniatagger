package com.jmcejuela.bio.jenia;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.jmcejuela.bio.jenia.common.Sentence;

/**
 * From main.cpp
 */
public class Main {

  public static final String ENDL = System.getProperty("line.separator");

  public static void line(StringBuilder s, String msg) {
    s.append(msg);
    s.append(ENDL);
  }

  public static String help() {
    StringBuilder s = new StringBuilder();

    line(s, "Usage: jeniatagger [OPTION]... [FILE]...");
    line(s, "Analyze English sentences from the biomedicine domain and print ");
    line(s, "the base forms, part-of-speech tags, chunk tags, and named entity tags.");
    line(s, "");
    line(s, "Options:");
    line(s, "  -nt          don't perform tokenization.");
    line(s, "  --help       display this help and exit.");
    line(s, "");
    line(s, "Report bugs to: github.com/jmcejuela/jeniatagger/issues");

    return s.toString();
  }

  public static String version() {
    return "0.1";
  }

  /**
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {
    boolean dont_tokenize = false;
    String ifilename = null;
    // String ofilename;
    for (String arg : args) {
      if (arg.equals("-nt")) {
        dont_tokenize = true;
      } else if (arg.equals("--help")) {
        System.out.println(help());
        return;
      }
      else {
        ifilename = arg;
      }
    }

    // default, standard input
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    if (ifilename != null && !ifilename.isEmpty() && !ifilename.equals("-")) {
      in = new BufferedReader(new FileReader(new File(ifilename)));
    }

    // ----------------------------------------------------------------------------

    System.err.println("Ready (the first sentence will take longer until all dictionaries are loaded)");

    String line;
    int n = 1;
    while ((line = in.readLine()) != null) {
      if (line.length() > 1024) {
        System.err.println("warning: the sentence seems to be too long at line " + n +
            " (please note that the input should be one-sentence-per-line).");
      }
      Sentence analysis = JeniaTagger.analyze(line, dont_tokenize);
      System.out.println(analysis);
      n++;
    }

    in.close();
    System.out.flush();
  }
}
