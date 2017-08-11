package com.example.shivam.boxapiintegration2;

import com.box.androidsdk.content.BoxApiFile;
import com.box.androidsdk.content.BoxApiFolder;

/**
 * Created by Shivam on 4/14/2017.
 */

public class ListArrayAdapter {

    String filename;
    BoxApiFile file;
    BoxApiFolder folder;

    public void setFilename(String filename)
    {
        this.filename=filename;
    }
    public String getFilename()
    {
        return this.filename;
    }

    public void setFile(BoxApiFile file)
    {
        this.file=file;
    }
    public  void setFolder(BoxApiFolder folder)
    {
        this.folder=folder;
    }

    public BoxApiFolder getFolder()
    {
        return  folder;
    }
    public  BoxApiFile getFile()
    {
        return  file;
    }

}
