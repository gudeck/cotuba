package cotuba;

import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {

        LeitorOpcoesCLI leitorOpcoesCLI = new LeitorOpcoesCLI(args);
        Path mdsDirectory = leitorOpcoesCLI.getMdsDirectory();
        String ebookFormat = leitorOpcoesCLI.getEbookFormat();
        Path outputFile = leitorOpcoesCLI.getOutputFile();
        boolean verbose = leitorOpcoesCLI.isVerbose();

        try {
            if ("pdf".equals(ebookFormat)) {
                GeradorPDF geradorPDF = new GeradorPDF();
                geradorPDF.generate(mdsDirectory, outputFile);
            } else if ("epub".equals(ebookFormat)) {
                GeradorEPUB geradorEPUB = new GeradorEPUB();
                geradorEPUB.generate(mdsDirectory, outputFile);
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
