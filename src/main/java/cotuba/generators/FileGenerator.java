package cotuba.generators;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.nio.file.Files;
import java.nio.file.Path;

public abstract class FileGenerator {

    protected String extractHtml(Node parsedMd) {
        HtmlRenderer htmlRenderer = HtmlRenderer.builder().build();
        return htmlRenderer.render(parsedMd);
    }

    public abstract void generate(Path outputPath, Path mdsDirectory);

    protected Node parseMd(Path mdPath) {
        try {
            Parser parser = Parser.builder().build();
            return parser.parseReader(Files.newBufferedReader(mdPath));
        } catch (Exception ex) {
            throw new RuntimeException("Erro ao fazer parse do arquivo " + mdPath, ex);
        }
    }

}
