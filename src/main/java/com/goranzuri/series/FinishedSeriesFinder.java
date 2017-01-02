package com.goranzuri.series;

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
 * Created by gzuri on 29.12.2016..
 */
public class FinishedSeriesFinder {

    public FinishedSeriesFinder() {
    }


    public void listFoldersThatDontHaveNewerFiles(String rootFolderPath, Integer numberOfDaysFilter){
        File parentFolder = new File(rootFolderPath);
        Date minDateFilter = calculateMinDate(numberOfDaysFilter);
        File[] folders = parentFolder.listFiles(File::isDirectory);
        Arrays.sort(folders);
        for (File folder: folders){
            tryProcessFolder(folder, minDateFilter);
        }
    }


    private Date calculateMinDate(Integer numberOfDays){
        Calendar cal = Calendar.getInstance();

        cal.add(Calendar.DAY_OF_MONTH, -numberOfDays);

        return cal.getTime();
    }

    private void tryProcessFolder(File folder, Date minDateFolder){
        try{
            processFolder(folder, minDateFolder);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void processFolder(File folder, Date minDateFilter) throws IOException {
        FileTime lastDate = FileTime.fromMillis(minDateFilter.getTime());
        Boolean folderHasANewFile = false;

        for(File episodeFile : folder.listFiles()){
                Path episodePath = Paths.get(episodeFile.getPath());
                BasicFileAttributes fileAttributes = Files.readAttributes(episodePath, BasicFileAttributes.class);

                if (lastDate.compareTo(fileAttributes.lastModifiedTime()) < 0){
                    folderHasANewFile = true;
                }
        }

        if (!folderHasANewFile)
            System.out.println(folder.getName());

    }
}
