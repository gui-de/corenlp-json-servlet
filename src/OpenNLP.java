import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Iterator;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.didion.jwnl.JWNL;
import opennlp.tools.chunker.ChunkSample;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.coref.DiscourseEntity;
import opennlp.tools.coref.LinkerMode;
import opennlp.tools.coref.TreebankLinker;
import opennlp.tools.coref.mention.DefaultParse;
import opennlp.tools.coref.mention.Mention;
import opennlp.tools.coref.mention.MentionContext;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.NameSample;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSSample;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import opennlp.tools.util.Span;

public class OpenNLP extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private ParserModel parserModel = null;

	private TokenNameFinderModel nameFinderModel = null;
	private TokenNameFinderModel dateFinderModel = null;
	private TokenNameFinderModel locationFinderModel = null;
	private TokenNameFinderModel moneyFinderModel = null;
	private TokenNameFinderModel orgFinderModel = null;
	private TokenNameFinderModel percentFinderModel = null;
	private TokenNameFinderModel timeFinderModel = null;

	private POSModel posMaxentModel = null;
	private POSModel posPerceptronModel = null;
	private ChunkerModel chunkerModel = null;

	private TreebankLinker treebankLinker = null;

	public OpenNLP() {
		super();
	}

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		InputStream parserModelIn = null;

		InputStream nameFinderModelIn = null;
		InputStream dateFinderModelIn = null;
		InputStream locationFinderModelIn = null;
		InputStream moneyFinderModelIn = null;
		InputStream orgFinderModelIn = null;
		InputStream percentFinderModelIn = null;
		InputStream timeFinderModelIn = null;

		InputStream posMaxentModelIn = null;
		InputStream posPerceptronModelIn = null;

		InputStream chunkerModelIn = null;
		try {
			parserModelIn = config.getServletContext().getResourceAsStream(
					"WEB-INF/lib/en-parser-chunking.bin");
			this.parserModel = new ParserModel(parserModelIn);

			nameFinderModelIn = config.getServletContext().getResourceAsStream(
					"WEB-INF/lib/en-ner-person.bin");
			this.nameFinderModel = new TokenNameFinderModel(nameFinderModelIn);

			dateFinderModelIn = config.getServletContext().getResourceAsStream(
					"WEB-INF/lib/en-ner-date.bin");
			this.dateFinderModel = new TokenNameFinderModel(dateFinderModelIn);

			locationFinderModelIn = config.getServletContext()
					.getResourceAsStream("WEB-INF/lib/en-ner-location.bin");
			this.locationFinderModel = new TokenNameFinderModel(
					locationFinderModelIn);

			moneyFinderModelIn = config.getServletContext()
					.getResourceAsStream("WEB-INF/lib/en-ner-money.bin");
			this.moneyFinderModel = new TokenNameFinderModel(moneyFinderModelIn);

			orgFinderModelIn = config.getServletContext().getResourceAsStream(
					"WEB-INF/lib/en-ner-organization.bin");
			this.orgFinderModel = new TokenNameFinderModel(orgFinderModelIn);

			percentFinderModelIn = config.getServletContext()
					.getResourceAsStream("WEB-INF/lib/en-ner-percentage.bin");
			this.percentFinderModel = new TokenNameFinderModel(
					percentFinderModelIn);

			timeFinderModelIn = config.getServletContext().getResourceAsStream(
					"WEB-INF/lib/en-ner-time.bin");
			this.timeFinderModel = new TokenNameFinderModel(timeFinderModelIn);

			posMaxentModelIn = config.getServletContext().getResourceAsStream(
					"WEB-INF/lib/en-pos-maxent.bin");
			this.posMaxentModel = new POSModel(posMaxentModelIn);

			posPerceptronModelIn = config.getServletContext()
					.getResourceAsStream("WEB-INF/lib/en-pos-maxent.bin");
			this.posPerceptronModel = new POSModel(posPerceptronModelIn);

			chunkerModelIn = config.getServletContext().getResourceAsStream(
					"WEB-INF/lib/en-chunker.bin");
			this.chunkerModel = new ChunkerModel(chunkerModelIn);

			this.treebankLinker = new TreebankLinker(config.getServletContext()
					.getRealPath("WEB-INF/lib/coref/"), LinkerMode.TEST);

			JWNL.initialize(config.getServletContext().getResourceAsStream(
					"WEB-INF/file_properties.xml"));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				nameFinderModelIn.close();
				dateFinderModelIn.close();
				locationFinderModelIn.close();
				moneyFinderModelIn.close();
				orgFinderModelIn.close();
				percentFinderModelIn.close();
				timeFinderModelIn.close();

				parserModelIn.close();
				posMaxentModelIn.close();
				posPerceptronModelIn.close();
				chunkerModelIn.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String text = request.getParameter("text");

		if (text == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
					"Text is empty");
			return;
		} else {
			// response.setContentType("application/json");
		}

		String[] tokens = WhitespaceTokenizer.INSTANCE.tokenize(text);

		StringBuffer result = new StringBuffer();

		// parsetree
		Parser parser = ParserFactory.create(this.parserModel);
		Parse parse = ParserTool.parseLine(text, parser, 1)[0];

		result.append("parsetree: \n");
		parse.show(result);
		result.append("\n\n");

		// name finder
		NameFinderME nameFinder = new NameFinderME(this.nameFinderModel);
		Span[] names = nameFinder.find(tokens);
		names = NameFinderME.dropOverlappingSpans(names);
		NameSample nameSample = new NameSample(tokens, names, false);
		nameFinder.clearAdaptiveData();

		result.append("detected names: \n");
		result.append(nameSample.toString());
		result.append("\n\n");

		// date finder
		nameFinder = new NameFinderME(this.dateFinderModel);
		names = nameFinder.find(tokens);
		names = NameFinderME.dropOverlappingSpans(names);
		nameSample = new NameSample(tokens, names, false);
		nameFinder.clearAdaptiveData();

		result.append("detected dates: \n");
		result.append(nameSample.toString());
		result.append("\n\n");

		// location finder
		nameFinder = new NameFinderME(this.locationFinderModel);
		names = nameFinder.find(tokens);
		names = NameFinderME.dropOverlappingSpans(names);
		nameSample = new NameSample(tokens, names, false);
		nameFinder.clearAdaptiveData();

		result.append("detected locations: \n");
		result.append(nameSample.toString());
		result.append("\n\n");

		// money finder
		nameFinder = new NameFinderME(this.moneyFinderModel);
		names = nameFinder.find(tokens);
		names = NameFinderME.dropOverlappingSpans(names);
		nameSample = new NameSample(tokens, names, false);
		nameFinder.clearAdaptiveData();

		result.append("detected money: \n");
		result.append(nameSample.toString());
		result.append("\n\n");

		// organization finder
		nameFinder = new NameFinderME(this.orgFinderModel);
		names = nameFinder.find(tokens);
		names = NameFinderME.dropOverlappingSpans(names);
		nameSample = new NameSample(tokens, names, false);
		nameFinder.clearAdaptiveData();

		result.append("detected org: \n");
		result.append(nameSample.toString());
		result.append("\n\n");

		// percentage finder
		nameFinder = new NameFinderME(this.percentFinderModel);
		names = nameFinder.find(tokens);
		names = NameFinderME.dropOverlappingSpans(names);
		nameSample = new NameSample(tokens, names, false);
		nameFinder.clearAdaptiveData();

		result.append("detected percent: \n");
		result.append(nameSample.toString());
		result.append("\n\n");

		// time finder
		nameFinder = new NameFinderME(this.timeFinderModel);
		names = nameFinder.find(tokens);
		names = NameFinderME.dropOverlappingSpans(names);
		nameSample = new NameSample(tokens, names, false);
		nameFinder.clearAdaptiveData();

		result.append("detected time: \n");
		result.append(nameSample.toString());
		result.append("\n\n");

		// pos (maxent)
		POSTaggerME tagger = new POSTaggerME(posMaxentModel);
		String[] tags = tagger.tag(tokens);
		POSSample sample = new POSSample(tokens, tags);

		result.append("pos tagger (maxent): \n");
		String posTaggedSentence = sample.toString();
		result.append(posTaggedSentence);
		result.append("\n\n");

		// pos (perceptron)
		tagger = new POSTaggerME(posPerceptronModel);
		tags = tagger.tag(tokens);
		sample = new POSSample(tokens, tags);

		result.append("pos tagger (perceptron): \n");
		result.append(sample.toString());
		result.append("\n\n");

		// chunker
		POSSample posSample = POSSample.parse(posTaggedSentence);
		ChunkerME chunker = new ChunkerME(chunkerModel,
				ChunkerME.DEFAULT_BEAM_SIZE);
		String[] chunks = chunker.chunk(posSample.getSentence(),
				posSample.getTags());
		result.append("chunker (via pos maxent): \n");
		result.append(new ChunkSample(posSample.getSentence(), posSample
				.getTags(), chunks).nicePrint());
		result.append("\n\n");

		// coref
		Mention[] mentions = treebankLinker.getMentionFinder().getMentions(
				new DefaultParse(parse, 0));
		// from CoreferencerTool.java source
		for (int i = 0, en = mentions.length; i < en; i++) {
			// construct parses for mentions which don't have constituents
			if (mentions[i].getParse() == null) {
				// not sure how to get head index, but it doesn't seem to be
				// used at this point
				final Parse snp = new Parse(parse.getText(),
						mentions[i].getSpan(), "NML", 1.0, 0);
				parse.insert(snp);
				// setting a new Parse for the current extent
				mentions[i].setParse(new DefaultParse(snp, 0));
			}
		}
		DiscourseEntity[] entities = treebankLinker.getEntities(mentions);
		result.append("coref: \n");
		for (int i = 0; i < entities.length; i++) {
			DiscourseEntity entity = entities[i];
			result.append(entity.toString()).append("\n");
			result.append("category: ").append(entity.getCategory())
					.append("\n");
			result.append("gender: ").append(entity.getGender()).append("\n");
			result.append("number: ").append(entity.getNumber()).append("\n");
			result.append("mentions: ").append(entity.getNumMentions())
					.append("\n");
			for (Iterator<MentionContext> mi = entity.getMentions(); mi
					.hasNext();) {
				MentionContext mc = mi.next();
				// result.append(mc.toText());
				Parse mentionParse = ((DefaultParse) mc.getParse()).getParse();
				result.append("\t").append(mentionParse.getText()).append("\n");
			}
			result.append("\n\n");
		}
		result.append("\n\n");

		PrintWriter out = response.getWriter();
		out.write(result.toString());
		out.flush();
		out.close();
		return;
	}
}
