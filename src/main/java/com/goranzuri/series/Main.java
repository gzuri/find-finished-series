package com.goranzuri.series;

import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


/**
 * Created by gzuri on 17.12.2016..
 */
public class Main {
    private static final String DIRECTORY_ARGUMENT = "dir";
    private static final String DAYS_FILTER_ARGUMENT = "days";
    private static final String MOVE_FINISHED = "moveDest";
    private static final String DELETE_MOVED = "remove";

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";


    private static Options prepareCommandLineOptions(){
        Option option_directory = OptionBuilder.withArgName(DIRECTORY_ARGUMENT).hasArg().withDescription("source directory to search").create(DIRECTORY_ARGUMENT);
        Option option_days = OptionBuilder.withArgName(DAYS_FILTER_ARGUMENT).hasArg().withDescription("number of days from the last episode").create(DAYS_FILTER_ARGUMENT);
        Option option_move = OptionBuilder.withArgName(MOVE_FINISHED).hasArg().withDescription("location where to move finished series").create(MOVE_FINISHED);
        Option option_remove = OptionBuilder.withArgName(DELETE_MOVED).withDescription("remove moved directories").create(DELETE_MOVED);
        Options options = new Options();

        options.addOption(option_directory);
        options.addOption(option_days);
        options.addOption(option_move);
        options.addOption(option_remove);

        return options;
    }

    private static void printHelpPage(Options options){
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("findFinishedSeries", "", options, "Application searches for directory where no file was added after selected date", true);
    }

    private static void runApp(String... args) throws ParseException, IOException {
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
        List<File> files = finishedSeriesFinder.listFoldersThatDontHaveNewerFiles(commandLine.getOptionValue(DIRECTORY_ARGUMENT), numberOfDaysFilter);

        for(File file : files)
            System.out.println(file.getName());


        if (commandLine.hasOption(MOVE_FINISHED)){
            Path destinationPath =  Paths.get(commandLine.getOptionValue(MOVE_FINISHED));

            if (!Files.exists(destinationPath)){
                System.out.println(ANSI_RED + "Move directory doesn't exist");
                return;
            }

            for(File file : files){
                Path destFolder = destinationPath.resolve(file.getName());
                if (Files.exists(destFolder)){
                    System.out.println(ANSI_YELLOW + "Duplicate: " + file.getName());
                    if (commandLine.hasOption(DELETE_MOVED)) {
                        org.apache.commons.io.FileUtils.deleteDirectory(file);
                        System.out.println(ANSI_GREEN  + "Deleted source: " + file.getName());
                    }
                }else {
                    System.out.println(ANSI_GREEN + "Copying: " + file.getName());
                    Files.createDirectory(destFolder);
                    org.apache.commons.io.FileUtils.copyDirectory(file, destFolder.toFile(), true);
                    System.out.println(ANSI_GREEN  + "Copied: " + file.getName());
                    if (commandLine.hasOption(DELETE_MOVED)) {
                        org.apache.commons.io.FileUtils.deleteDirectory(file);
                        System.out.println(ANSI_GREEN  + "Deleted source: " + file.getName());
                    }
                }
            }
        }
    }


    public static void main(String... args){
        try{
            runApp(args);

        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
