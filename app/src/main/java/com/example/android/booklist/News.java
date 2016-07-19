package com.example.android.booklist;

/**
 * Created by PotnuruSiva on 11-07-2016.
 */
public class News {

    private String mType;
    private String mWebTitle;
    private String mWebUrl;
    private String mPublishDate;


    public News() {
    }

    public News(String type, String webTitle, String publishDate, String webUrl) {
        mType = type;
        mWebTitle = webTitle;
        mWebUrl = webUrl;
        mPublishDate = publishDate;
    }

    public String getNewsTitle() {
        return mWebTitle;
    }

    public String getNewsUrl() {
        return mWebUrl;
    }

    public String getType() {
        return mType;
    }

    public String getPublishDate() {
        return mPublishDate;
    }

}
