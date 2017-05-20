package com.example.android.booklisting;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by nalin on 20-May-17.
 */

public class BookAdapter extends ArrayAdapter<Book> {

    public BookAdapter(Activity Context, List<Book> books){
        super(Context,0,books);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.book_list_item, parent, false);
        }

        Book currentBook = getItem(position);

        TextView titleView = (TextView) listItemView.findViewById(R.id.title_view);

        titleView.setText(currentBook.getTitle());

        TextView authorView = (TextView)listItemView.findViewById(R.id.author_view);

        authorView.setText(currentBook.getAuthor());

        return listItemView;
    }
}
