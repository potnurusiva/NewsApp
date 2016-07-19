package com.example.android.booklist;

        import android.app.Activity;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ArrayAdapter;
        import android.widget.TextView;

        import java.util.ArrayList;

/**
 * Created by PotnuruSiva on 11-07-2016.
 */
public class NewsAdapter extends ArrayAdapter<News> {

    public NewsAdapter(Activity context, ArrayList<News> books) {
        super(context, 0, books);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }

        News currentNews = getItem(position);

        TextView newsTypeTextView = (TextView) listItemView.findViewById(R.id.news_type);
        newsTypeTextView.setText(currentNews.getType());

        TextView newsTitleTextView = (TextView) listItemView.findViewById(R.id.news_title);
        newsTitleTextView.setText(currentNews.getNewsTitle());

        TextView newsUrlTextView = (TextView) listItemView.findViewById(R.id.news_url);
        newsUrlTextView.setText(currentNews.getNewsUrl());

        TextView newsDateTextView = (TextView) listItemView.findViewById(R.id.news_date);
        newsDateTextView.setText(currentNews.getPublishDate());

        return listItemView;
    }
}
