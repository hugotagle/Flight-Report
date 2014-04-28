package com.dataedge.android.pc.comparator;

import java.util.Comparator;
import java.util.Date;

import com.dataedge.android.pc.model.MediaFileModel;

// sorts ascending on the file extension or status letter, then on the
// edit date in descending order
public class MediaFilesComparator implements Comparator<MediaFileModel> {
    @Override
    public int compare(MediaFileModel o1, MediaFileModel o2) {

//        Date d1 = new Date(o1.getEditDate());
//        Date d2 = new Date(o2.getEditDate());
//        return d1.compareTo(d2);
        
        String s1 = new String(o1.getFileName());
        String s2 = new String(o2.getFileName());
        return s1.compareTo(s2);

    }
}
