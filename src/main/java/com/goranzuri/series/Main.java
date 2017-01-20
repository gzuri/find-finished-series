package com.goranzuri.series;

import org.apache.commons.cli.*;


/**
 * Created by gzuri on 17.12.2016..
 */
public class Main {
    private static final String DIRECTORY_ARGUMENT = "dir";
    private static final String DAYS_FILTER_ARGUMENT = "days";


    private static Options prepareCommandLineOptions(){
        Option option_directory = OptionBuilder.withArgName(DIRECTORY_ARGUMENT).hasArg().withDescription("source directory to search").create(DIRECTORY_ARGUMENT);
        Option option_days = OptionBuilder.withArgName(DAYS_FILTER_ARGUMENT).hasArg().withDescription("number of days from the last episode").create(DAYS_FILTER_ARGUMENT);
        Options options = new Options();

        options.addOption(option_directory);
        options.addOption(option_days);

        return options;
    }

    private static void printHelpPage(Options options){
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("findFinishedSeries", "", options, "Application searches for directory where no file was added after selected date", true);
    }

    private static void runApp(String... args) throws ParseException {
        FinishedSeriesFinder finishedSeriesFinder;
        CommandLineParser parser = new GnuParser();
        CommandLine commandLine;
        Options options = Main.prepareCommandLineOptions();
        Integer numberOfDaysFilter = null;

        commandLine = parser.parse(options, args);

        if (args.length < 2 || !commandLine.hasOption(DIRECTORY_ARGUMENT) || !commandLine.hasOption(DAYS_FILTER_ARGUMENT)){
            Main.printHelpPage(options);
            return;
        }
        numberOfDaysFilter = Integer.parseInt(commandLine.getOptionValue(DAYS_FILTER_ARGUMENT));

        finishedSeriesFinder = new FinishedSeriesFinder();
        finishedSeriesFinder.listFoldersThatDontHaveNewerFiles(commandLine.getOptionValue(DIRECTORY_ARGUMENT), numberOfDaysFilter);
    }


    public static void main(String... args){
        try{
            runApp(args);

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
