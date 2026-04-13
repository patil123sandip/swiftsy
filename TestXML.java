import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.helpers.DefaultHandler;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestXML {
    public static void main(String[] args) throws Exception {
        String content = Files.readString(Paths.get("src/main/resources/templates/invoice-template.html"));
        // Remove Thymeleaf attributes that might confuse SAX if namespaces aren't declared properly (though SAX handles prefix usually if namespace awareness is off)
        
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setValidating(false);
        SAXParser parser = factory.newSAXParser();
        
        File file = new File("src/main/resources/templates/invoice-template.html");
        try {
            parser.parse(file, new DefaultHandler());
            System.out.println("Valid XML!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
