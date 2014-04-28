package com.dataedge.android.pc;

import java.util.List;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dataedge.android.pc.model.MediaFileModel;

public class MediaFileArrayAdapter extends ArrayAdapter<MediaFileModel> {
    String TAG = ReportArrayAdapter.class.getName();

    private final Activity activity;
    private final List<MediaFileModel> mediaFiles;

    public MediaFileArrayAdapter(Activity activity, List<MediaFileModel> objects) {
        super(activity, R.layout.report_list_item, objects);
        this.activity = activity;
        this.mediaFiles = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        MediaFileView mediaFileView = null;

        if (rowView == null) {
            // Get a new instance of the row layout view
            LayoutInflater inflater = activity.getLayoutInflater();
            rowView = inflater.inflate(R.layout.media_file_list_item, null);

            // Hold the view objects in an object,
            // so they don't need to be re-fetched
            mediaFileView = new MediaFileView();
            mediaFileView.fileName = (TextView) rowView.findViewById(R.id.mediaFileName);
            mediaFileView.editDate = (TextView) rowView.findViewById(R.id.editDate);
            mediaFileView.thumbnail = (ImageView) rowView.findViewById(R.id.thumbnail);

            // Cache the view objects in the TAG,
            // so they can be re-accessed later
            rowView.setTag(mediaFileView);
        } else {
            mediaFileView = (MediaFileView) rowView.getTag();
        }

        // Transfer the media file data from the data object
        // to the view objects
        MediaFileModel currentMediaFile = (MediaFileModel) mediaFiles.get(position);
        mediaFileView.fileName.setText(currentMediaFile.getFileName());

        mediaFileView.thumbnail.setImageBitmap(BitmapFactory.decodeFile(activity.getFilesDir()
                .getAbsolutePath()
                + "/"
                + currentMediaFile.getFileName()
                        .replace(Codes.FILE_EXT_PHOTO, Codes.FILE_EXT_THUMB)));

        // dynamically build the text views
        mediaFileView.editDate.setText(currentMediaFile.getEditDate());

        return rowView;
    }

    protected static class MediaFileView {
        protected TextView fileName;
        protected TextView locatorCode;
        protected TextView editDate;
        protected ImageView thumbnail;
    }

}
