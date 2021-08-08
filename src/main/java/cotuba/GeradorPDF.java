package cotuba;

import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.IBlockElement;
import com.itextpdf.layout.element.IElement;
import com.itextpdf.layout.property.AreaBreakType;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.stream.Stream;

public class GeradorPDF {

    public void generate(Path mdsDirectory, Path outputFile) {
        try (PdfWriter pdfWriter = new PdfWriter(Files.newOutputStream(outputFile));
             PdfDocument pdf = new PdfDocument(pdfWriter);
             Document pdfDocument = new Document(pdf)) {
            createPdf(mdsDirectory, pdfDocument);
        } catch (Exception ex) {
            throw new RuntimeException("Erro ao criar arquivo PDF: " + outputFile.toAbsolutePath(), ex);
        }
    }

    private void createPdf(Path mdsDirectory, Document pdfDocument) {
        try (Stream<Path> mdFiles = Files.list(mdsDirectory)) {
            PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:**/*.md");
            mdFiles.filter(pathMatcher::matches)
                    .sorted()
                    .forEach(mdFile -> createPage(pdfDocument, mdFile));
        } catch (IOException ex) {
            throw new RuntimeException("Erro tentando encontrar arquivos .md em " + mdsDirectory.toAbsolutePath(), ex);
        }
    }

    private void createPage(Document pdfDocument, Path mdFile) {
        Node document = parseMd(mdFile);
        List<IElement> elements = renderMd(mdFile, document);

        for (IElement element : elements) {
            pdfDocument.add((IBlockElement) element);
        }
        pdfDocument.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
    }

    private Node parseMd(Path mdFile) {
        try {
            Parser parser = Parser.builder().build();
            return parser.parseReader(Files.newBufferedReader(mdFile));
        } catch (Exception ex) {
            throw new RuntimeException("Erro ao fazer parse do arquivo " + mdFile, ex);
        }
    }

    private List<IElement> renderMd(Path mdFile, Node document) {
        try {
            HtmlRenderer htmlRenderer = HtmlRenderer.builder().build();
            String html = htmlRenderer.render(document);
            return HtmlConverter.convertToElements(html);
        } catch (Exception ex) {
            throw new RuntimeException("Erro ao renderizar para HTML o arquivo " + mdFile, ex);
        }
    }

}
