package cotuba;

import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.IBlockElement;
import com.itextpdf.layout.element.IElement;
import com.itextpdf.layout.property.AreaBreakType;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubWriter;
import nl.siegmann.epublib.service.MediatypeService;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Heading;
import org.commonmark.node.Node;
import org.commonmark.node.Text;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) {

        LeitorOpcoesCLI leitorOpcoesCLI = new LeitorOpcoesCLI(args);
        Path mdsDirectory = leitorOpcoesCLI.getMdsDirectory();
        String ebookFormat = leitorOpcoesCLI.getEbookFormat();
        Path outputFile = leitorOpcoesCLI.getOutputFile();
        boolean verbose = leitorOpcoesCLI.isVerbose();

        try {

            if ("pdf".equals(ebookFormat)) {
                try (PdfWriter writer = new PdfWriter(Files.newOutputStream(outputFile));
                     PdfDocument pdf = new PdfDocument(writer);
                     Document pdfDocument = new Document(pdf)) {

                    PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**/*.md");
                    try (Stream<Path> arquivosMD = Files.list(mdsDirectory)) {
                        arquivosMD
                                .filter(matcher::matches)
                                .sorted()
                                .forEach(arquivoMD -> {
                                    Parser parser = Parser.builder().build();
                                    Node document = null;
                                    try {
                                        document = parser.parseReader(Files.newBufferedReader(arquivoMD));
                                        document.accept(new AbstractVisitor() {
                                            @Override
                                            public void visit(Heading heading) {
                                                if (heading.getLevel() == 1) {
                                                    // capítulo
                                                    String tituloDoCapitulo = ((Text) heading.getFirstChild()).getLiteral();
                                                    // TODO: usar título do capítulo
                                                } else if (heading.getLevel() == 2) {
                                                    // seção
                                                } else if (heading.getLevel() == 3) {
                                                    // título
                                                }
                                            }

                                        });
                                    } catch (Exception ex) {
                                        throw new RuntimeException("Erro ao fazer parse do arquivo " + arquivoMD, ex);
                                    }

                                    try {
                                        HtmlRenderer renderer = HtmlRenderer.builder().build();
                                        String html = renderer.render(document);

                                        List<IElement> convertToElements = HtmlConverter.convertToElements(html);
                                        for (IElement element : convertToElements) {
                                            pdfDocument.add((IBlockElement) element);
                                        }
                                        // TODO: não adicionar página depois do último capítulo
                                        pdfDocument.add(new AreaBreak(AreaBreakType.NEXT_PAGE));

                                    } catch (Exception ex) {
                                        throw new RuntimeException("Erro ao renderizar para HTML o arquivo " + arquivoMD, ex);
                                    }

                                });
                    } catch (IOException ex) {
                        throw new RuntimeException(
                                "Erro tentando encontrar arquivos .md em " + mdsDirectory.toAbsolutePath(), ex);
                    }

                } catch (Exception ex) {
                    throw new RuntimeException("Erro ao criar arquivo PDF: " + outputFile.toAbsolutePath(), ex);
                }

            } else if ("epub".equals(ebookFormat)) {
                Book epub = new Book();

                PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**/*.md");
                try (Stream<Path> arquivosMD = Files.list(mdsDirectory)) {
                    arquivosMD
                            .filter(matcher::matches)
                            .sorted()
                            .forEach(arquivoMD -> {
                                Parser parser = Parser.builder().build();
                                Node document = null;
                                try {
                                    document = parser.parseReader(Files.newBufferedReader(arquivoMD));
                                    document.accept(new AbstractVisitor() {
                                        @Override
                                        public void visit(Heading heading) {
                                            if (heading.getLevel() == 1) {
                                                // capítulo
                                                String tituloDoCapitulo = ((Text) heading.getFirstChild()).getLiteral();
                                                // TODO: usar título do capítulo
                                            } else if (heading.getLevel() == 2) {
                                                // seção
                                            } else if (heading.getLevel() == 3) {
                                                // título
                                            }
                                        }

                                    });
                                } catch (Exception ex) {
                                    throw new RuntimeException("Erro ao fazer parse do arquivo " + arquivoMD, ex);
                                }

                                try {
                                    HtmlRenderer renderer = HtmlRenderer.builder().build();
                                    String html = renderer.render(document);

                                    // TODO: usar título do capítulo
                                    epub.addSection("Capítulo", new Resource(html.getBytes(), MediatypeService.XHTML));

                                } catch (Exception ex) {
                                    throw new RuntimeException("Erro ao renderizar para HTML o arquivo " + arquivoMD, ex);
                                }
                            });
                } catch (IOException ex) {
                    throw new RuntimeException(
                            "Erro tentando encontrar arquivos .md em " + mdsDirectory.toAbsolutePath(), ex);
                }

                EpubWriter epubWriter = new EpubWriter();

                try {
                    epubWriter.write(epub, Files.newOutputStream(outputFile));
                } catch (IOException ex) {
                    throw new RuntimeException("Erro ao criar arquivo EPUB: " + outputFile.toAbsolutePath(), ex);
                }
            } else {
                throw new RuntimeException("ebookFormat do ebook inválido: " + ebookFormat);
            }

            System.out.println("Arquivo gerado com sucesso: " + outputFile);

        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            if (verbose) {
                ex.printStackTrace();
            }
            System.exit(1);
        }
    }

}
