package cotuba;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubWriter;
import nl.siegmann.epublib.service.MediatypeService;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.stream.Stream;

public class GeradorEPUB {

    public void generate(Path mdsDirectory, Path outputFile) {
        try (Stream<Path> mdFiles = Files.list(mdsDirectory)) {
            Book epubBook = new Book();
            PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:**/*.md");
            mdFiles.filter(pathMatcher::matches)
                    .sorted()
                    .forEach(mdFile -> createPage(epubBook, mdFile));
            writeEpub(outputFile, epubBook);
        } catch (IOException ex) {
            throw new RuntimeException("Erro tentando encontrar arquivos .md em " + mdsDirectory.toAbsolutePath(), ex);
        }
    }

    private void createPage(Book epubBook, Path mdFile) {
        Node document = parseMd(mdFile);
        String html = renderMd(mdFile, document);
        epubBook.addSection("Capítulo", new Resource(html.getBytes(), MediatypeService.XHTML));
    }

    private void writeEpub(Path outputFile, Book epubBook) {
        try {
            EpubWriter epubWriter = new EpubWriter();
            epubWriter.write(epubBook, Files.newOutputStream(outputFile));
        } catch (IOException ex) {
            throw new RuntimeException("Erro ao criar arquivo EPUB: " + outputFile.toAbsolutePath(), ex);
        }
    }

    private Node parseMd(Path mdFile) {
        try {
            Parser parser = Parser.builder().build();
            return parser.parseReader(Files.newBufferedReader(mdFile));
        } catch (Exception ex) {
            throw new RuntimeException("Erro ao fazer parse do arquivo " + mdFile, ex);
        }
    }

    private String renderMd(Path mdFile, Node document) {
        try {
            HtmlRenderer renderer = HtmlRenderer.builder().build();
            return renderer.render(document);
        } catch (Exception ex) {
            throw new RuntimeException("Erro ao renderizar para HTML o arquivo " + mdFile, ex);
        }
    }

}
