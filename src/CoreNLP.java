import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

/**
 * Servlet implementation class CoreNLP
 */
public class CoreNLP extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private StanfordCoreNLP nlp = null;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public CoreNLP() {
		super();

		// initialize the parser with all properties
		Properties props = new Properties();
		props.put("annotators",
		// "tokenize, cleanxml, ssplit, pos, lemma, ner, regexner, truecase, parse, dcoref");
		// default annotators for stanford-corenlp-python
				"tokenize, ssplit, pos, lemma, ner, parse, dcoref");
		this.nlp = new StanfordCoreNLP(props);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		String text = request.getParameter("text");

		if (text == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
					"Text is empty");
			return;
		} else {
			response.setContentType("application/json");
		}

		// create an empty Annotation just with the given text
		Annotation document = new Annotation(text);

		// run all Annotators on this text
		nlp.annotate(document);

		PrintWriter pw = new PrintWriter(out);
		nlp.prettyPrint(document, pw);
		return;
	}
}
