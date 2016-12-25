package com.goranzuri.anime;

import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by gzuri on 17.12.2016..
 */
public class Main {
    private static final String DIRECTORY_ARGUMENT = "dir";
    private static final String DAYS_FILTER_ARGUMENT = "days";


    private static void processSeriesFolder(File seriesFolder, Date dateFilter){
        FileTime lastDate = FileTime.fromMillis(dateFilter.getTime());
        Boolean folderHasANewFile = false;

        for(File episodeFile : seriesFolder.listFiles()){
            try {
                Path episodePath = Paths.get(episodeFile.getPath());
                BasicFileAttributes fileAttributes = Files.readAttributes(episodePath, BasicFileAttributes.class);

                if (lastDate.compareTo(fileAttributes.lastModifiedTime()) < 0){
                    folderHasANewFile = true;
                }
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }

        if (!folderHasANewFile)
            System.out.println(seriesFolder.getName());

    }


    public static void searchDownloadFolder(String folderPath, Integer numberOfDays){
        File parentFolder = new File(folderPath);
        Calendar cal = Calendar.getInstance();

        cal.add(Calendar.DAY_OF_MONTH, -numberOfDays);
        Date result = cal.getTime();

        File[] folders = parentFolder.listFiles(File::isDirectory);
        Arrays.sort(folders);
        for (File folder: folders){

            processSeriesFolder(folder, result);
        }
    }


    public static void main(String... args){

        System.out.println("test ok");
        CommandLine commandLine;
        Option option_directory = OptionBuilder.withArgName(DIRECTORY_ARGUMENT).hasArg().withDescription("source directory to search").create(DIRECTORY_ARGUMENT);
        Option option_days = OptionBuilder.withArgName(DAYS_FILTER_ARGUMENT).hasArg().withDescription("number of days from the last episode").create(DAYS_FILTER_ARGUMENT);
        CommandLineParser parser = new GnuParser();
        Options options = new Options();

        options.addOption(option_directory);
        options.addOption(option_days);

        try{
            commandLine = parser.parse(options, args);

            if (args.length < 2 || !commandLine.hasOption(DIRECTORY_ARGUMENT) || !commandLine.hasOption(DAYS_FILTER_ARGUMENT)){
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("findFinishedSeries", "", options, "Application searches for directory where no file was added after selected date", true);
                return;
            }

            searchDownloadFolder(commandLine.getOptionValue(DIRECTORY_ARGUMENT), 5);

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
