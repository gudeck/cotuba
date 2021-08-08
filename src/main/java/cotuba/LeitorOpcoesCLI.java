package cotuba;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LeitorOpcoesCLI {

    private Path mdsDirectory;
    private String ebookFormat;
    private Path outputFile;
    private boolean verbose = false;

    public LeitorOpcoesCLI(String[] args) {
        createCmd(args);
    }

    public void createCmd(String[] args) {
        CommandLineParser cmdParser = new DefaultParser();
        HelpFormatter help = new HelpFormatter();

        Options options = getOptions();

        try {
            CommandLine cmd = cmdParser.parse(options, args);
            configureOutput(cmd);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            help.printHelp("cotuba", options);
            System.exit(1);
        }
    }

    private Options getOptions() {
        Options options = new Options();

        Option opcaoDeDiretorioDosMD = new Option("d", "dir", true,
                "Diretório que contem os arquivos md. Default: diretório atual.");
        options.addOption(opcaoDeDiretorioDosMD);

        Option opcaoDeFormatoDoEbook = new Option("f", "format", true,
                "Formato de saída do ebook. Pode ser: pdf ou epub. Default: pdf");
        options.addOption(opcaoDeFormatoDoEbook);

        Option opcaoDeArquivoDeSaida = new Option("o", "output", true,
                "Arquivo de saída do ebook. Default: book.{formato}.");
        options.addOption(opcaoDeArquivoDeSaida);

        Option opcaoModoVerboso = new Option("v", "verbose", false,
                "Habilita modo verboso.");
        options.addOption(opcaoModoVerboso);

        return options;
    }

    private void configureOutput(CommandLine cmd) {
        configureDirectory(cmd);
        configureFormat(cmd);
        configureName(cmd);
        configureVerbose(cmd);
    }

    private void configureDirectory(CommandLine cmd) {
        String mdsDirectoryName = cmd.getOptionValue("dir");

        if (mdsDirectoryName != null) {
            mdsDirectory = Paths.get(mdsDirectoryName);
            if (!Files.isDirectory(mdsDirectory)) {
                throw new RuntimeException(mdsDirectoryName + " não é um diretório.");
            }
        } else {
            mdsDirectory = Paths.get("");
        }
    }

    private void configureFormat(CommandLine cmd) {
        String ebookFormatName = cmd.getOptionValue("format");

        if (ebookFormatName != null) {
            ebookFormat = ebookFormatName.toLowerCase();
        } else {
            ebookFormat = "pdf";
        }
    }

    private void configureName(CommandLine cmd) {
        String outputFilename = cmd.getOptionValue("output");
        if (outputFilename != null) {
            outputFile = Paths.get(outputFilename);
            if (Files.exists(outputFile) && Files.isDirectory(outputFile)) {
                throw new RuntimeException(outputFilename + " é um diretório.");
            }
        } else {
            outputFile = Paths.get("book." + ebookFormat.toLowerCase());
        }
    }

    private void configureVerbose(CommandLine cmd) {
        verbose = cmd.hasOption("verbose");
    }

    public String getEbookFormat() {
        return ebookFormat;
    }

    public Path getMdsDirectory() {
        return mdsDirectory;
    }

    public Path getOutputFile() {
        return outputFile;
    }

    public boolean isVerbose() {
        return verbose;
    }

}
