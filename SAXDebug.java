import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXParseException;
import java.io.File;

public class SAXDebug {
    public static void main(String[] args) throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        try {
            parser.parse(new File("src/main/resources/templates/invoice-template.html"), new DefaultHandler() {
                public void error(SAXParseException e) {
                    System.err.println("Parse Error: " + e.getMessage() + " line " + e.getLineNumber() + " col " + e.getColumnNumber());
                }
                public void fatalError(SAXParseException e) {
                    System.err.println("Fatal Error: " + e.getMessage() + " line " + e.getLineNumber() + " col " + e.getColumnNumber());
                }
            });
            System.out.println("Parsed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
