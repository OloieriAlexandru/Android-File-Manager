package com.example.projectafd.ui.adapters;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.example.projectafd.R;
import com.example.projectafd.models.FileItem;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.O)
public class FileItemsAdapter extends ArrayAdapter<FileItem> {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy")
            .withZone(ZoneId.systemDefault());
    private static final int TITLE_DISPLAYED_CHARACTERS_COUNT = 20;

    private final Context context;

    private final List<FileItem> files;

    public FileItemsAdapter(@NonNull Context context, int resource, @NonNull List<FileItem> files) {
        super(context, resource, files);

        this.context = context;
        this.files = files;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.file_item, parent, false);

        FileItem item = files.get(position);
        if (item == null) {
            return rowView;
        }

        ImageView imageView = (ImageView) rowView.findViewById(R.id.fileItemIcon);
        imageView.setImageResource(item.getImageResId());

        TextView nameTextView = (TextView) rowView.findViewById(R.id.fileItemName);
        nameTextView.setText(getDisplayedFileName(item.getName()));

        TextView dateTextView = (TextView) rowView.findViewById(R.id.fileItemDate);
        dateTextView.setText(DATE_TIME_FORMATTER.format(item.getLastModified()));

        TextView specificInfoTextView = (TextView) rowView.findViewById(R.id.fileItemSpecificInfo);
        specificInfoTextView.setText(item.getSpecificInfo().getSpecificInfoToDisplay());

        return rowView;
    }

    private String getDisplayedFileName(String fileName) {
        String displayedName = fileName;
        if (displayedName.length() > TITLE_DISPLAYED_CHARACTERS_COUNT - 3) {
            displayedName = String.format("%s...", displayedName.substring(0, TITLE_DISPLAYED_CHARACTERS_COUNT - 3));
        }
        return displayedName;
    }
}
