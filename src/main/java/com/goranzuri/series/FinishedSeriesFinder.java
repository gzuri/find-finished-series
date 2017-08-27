package com.goranzuri.series;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by gzuri on 29.12.2016..
 */
public class FinishedSeriesFinder {

    public FinishedSeriesFinder() {
    }


    public List<File> listFoldersThatDontHaveNewerFiles(String rootFolderPath, Integer numberOfDaysFilter){
        ArrayList<File> listOfUnchangedFolders = new ArrayList<>();
        File parentFolder = new File(rootFolderPath);
        Date minDateFilter = calculateMinDate(numberOfDaysFilter);
        File[] folders = parentFolder.listFiles(File::isDirectory);
        Arrays.sort(folders);
        for (File folder: folders){
            if (!tryCheckIfFolderHasNewFiles(folder, minDateFilter))
                listOfUnchangedFolders.add(folder);
        }

        return listOfUnchangedFolders;
    }


    private Date calculateMinDate(Integer numberOfDays){
        Calendar cal = Calendar.getInstance();

        cal.add(Calendar.DAY_OF_MONTH, -numberOfDays);

        return cal.getTime();
    }

    private boolean tryCheckIfFolderHasNewFiles(File folder, Date minDateFolder){
        try{
            return checkIfFolderHasNewFiles(folder, minDateFolder);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    private boolean checkIfFolderHasNewFiles(File folder, Date minDateFilter) throws IOException {
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
            return false;

        return true;
    }

    public static List<Integer> findMissingEpisodes(File folder){
        Integer lastEpisode = 0;
        List<Integer> missingEpisodes = new ArrayList<>();

        Pattern p = Pattern.compile("(?<episodeNum>\\d+)(?:( )*\\[)");
        for(File episeodeFile: folder.listFiles()){
            Matcher m = p.matcher(episeodeFile.getName());
            if (m.find()){
                Integer episodeNum = Integer.parseInt( m.group("episodeNum"));
                if (episodeNum > lastEpisode){
                    if (lastEpisode + 1 < episodeNum)
                        do{
                            lastEpisode += 1;
                            missingEpisodes.add(lastEpisode);
                        }while (lastEpisode +1 < episodeNum);
                    lastEpisode = episodeNum;
                }

            }
        }

        return missingEpisodes;
    }
}
