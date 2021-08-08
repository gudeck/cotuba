package cotuba.generators;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubWriter;
import nl.siegmann.epublib.service.MediatypeService;
import org.commonmark.node.Node;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.stream.Stream;

public class EpubGenerator extends FileGenerator {

    @Override
    public void generate(Path outputPath, Path mdsDirectory) {
        Book epub = writePages(mdsDirectory);
        writeFile(epub, outputPath);
    }

    private Book writePages(Path mdsDirectory) {
        try (Stream<Path> mdsPaths = Files.list(mdsDirectory)) {
            Book epub = new Book();
            PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:**/*.md");
            mdsPaths.filter(pathMatcher::matches)
                    .sorted()
                    .forEach(mdPath -> writePage(epub, mdPath));
            return epub;
        } catch (IOException ex) {
            throw new RuntimeException("Erro tentando encontrar arquivos .md em " + mdsDirectory.toAbsolutePath(), ex);
        }
    }

    private void writePage(Book epub, Path mdPath) {
        Node parsedMd = parseMd(mdPath);
        String html = extractHtml(parsedMd);
        epub.addSection("Capítulo", new Resource(html.getBytes(), MediatypeService.XHTML));
    }

    private void writeFile(Book epub, Path outputPath) {
        try {
            EpubWriter epubWriter = new EpubWriter();
            epubWriter.write(epub, Files.newOutputStream(outputPath));
        } catch (IOException ex) {
            throw new RuntimeException("Erro ao criar arquivo EPUB: " + outputPath.toAbsolutePath(), ex);
        }
    }

}
