package cotuba;

import cotuba.clis.Cli;
import cotuba.generators.EpubGenerator;
import cotuba.generators.PdfGenerator;

import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {

        Cli cli = new Cli(args);
        Path mdsDirectory = cli.getMdsDirectory();
        String ebookFormat = cli.getEbookFormat();
        Path outputPath = cli.getOutputPath();
        boolean verbose = cli.isVerbose();

        try {
            if ("pdf".equals(ebookFormat)) {
                PdfGenerator pdfGenerator = new PdfGenerator();
                pdfGenerator.generate(outputPath, mdsDirectory);
            } else if ("epub".equals(ebookFormat)) {
                EpubGenerator epubGenerator = new EpubGenerator();
                epubGenerator.generate(outputPath, mdsDirectory);
            } else {
                throw new RuntimeException("ebookFormat do ebook inválido: " + ebookFormat);
            }
            System.out.println("Arquivo gerado com sucesso: " + outputPath);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            if (verbose) {
                ex.printStackTrace();
            }
            System.exit(1);
        }
    }

}
