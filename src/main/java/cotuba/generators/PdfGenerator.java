package cotuba.generators;

import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.IBlockElement;
import com.itextpdf.layout.element.IElement;
import com.itextpdf.layout.property.AreaBreakType;
import org.commonmark.node.Node;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.stream.Stream;

public class PdfGenerator extends FileGenerator {

    @Override
    public void generate(Path outputPath, Path mdsDirectory) {
        try (PdfWriter pdfWriter = new PdfWriter(Files.newOutputStream(outputPath));
             PdfDocument pdfDocument = new PdfDocument(pdfWriter);
             Document pdf = new Document(pdfDocument)) {
            writePages(pdf, mdsDirectory);
        } catch (Exception ex) {
            throw new RuntimeException("Erro ao criar arquivo PDF: " + outputPath.toAbsolutePath(), ex);
        }
    }

    private void writePages(Document pdf, Path mdsDirectory) {
        try (Stream<Path> mdsPaths = Files.list(mdsDirectory)) {
            PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:**/*.md");
            mdsPaths.filter(pathMatcher::matches)
                    .sorted()
                    .forEach(mdPath -> writePage(pdf, mdPath));
        } catch (IOException ex) {
            throw new RuntimeException("Erro tentando encontrar arquivos .md em " + mdsDirectory.toAbsolutePath(), ex);
        }
    }

    private void writePage(Document pdf, Path mdPath) {
        try {
            Node parsedMd = parseMd(mdPath);
            String html = extractHtml(parsedMd);
            for (IElement element : HtmlConverter.convertToElements(html)) {
                pdf.add((IBlockElement) element);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Erro ao renderizar para HTML o arquivo " + mdPath, ex);
        }
        pdf.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
    }

}
