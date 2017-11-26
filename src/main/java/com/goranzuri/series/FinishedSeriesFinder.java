package com.goranzuri.series;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
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

    public static void downloadTorrentTry(String baseFolder, String animeName) {
        try{
            downloadTorrent(baseFolder, animeName);
        }catch (IOException|ParserConfigurationException|SAXException ex){

        }
    }


    private static void downloadTorrent(String baseFolder, String animeName) throws IOException, ParserConfigurationException, SAXException {
        String animeNameEncoded = URLEncoder.encode(animeName);

        URL url = new URL("https://nyaa.si/?page=rss&c=0_0&f=0&u=HorribleSubs&q=720+" + animeNameEncoded);
        InputStream stream = url.openStream();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        //Get the DOM Builder
        DocumentBuilder builder = factory.newDocumentBuilder();

        //Load and Parse the XML document
        //document contains the complete XML as a Tree.
        Document document = builder.parse(stream);

        //Iterating through the nodes and extracting the data.
        NodeList nodeList = document.getDocumentElement().getChildNodes();

        for (int i = 0; i < nodeList.getLength(); i++) {
            //We have encountered an <employee> tag.
            Node node = nodeList.item(i);
            if (node instanceof Element) {

                NodeList childNodes = node.getChildNodes();
                for(int j = 0; j < childNodes.getLength(); j++){
                    Node childNode = childNodes.item(j);

                    if (childNode.getNodeName().equalsIgnoreCase("item")){
                        NodeList subNodes = childNode.getChildNodes();
                        for(int k = 0; k < subNodes.getLength(); k++){
                            Node kSubNode = subNodes.item(k);

                            if (kSubNode.getNodeName().equalsIgnoreCase("link")){

                                try(InputStream in = new URL(kSubNode.getTextContent()).openStream()){
                                    Files.copy(in, Paths.get(baseFolder, animeName + ".torrent" ));
                                }
                            }
                        }
                    }
                }
            }

        }
    }
}
