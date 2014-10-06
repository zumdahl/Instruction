package com.mgyaware.instruction;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sam on 10/4/14.
 *
 * Used various templates and design concepts from
 * http://developer.android.com/training/basics/network-ops/xml.html
 */
public class SlideParser {

    public List parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            if (parser.getName().equals("slide")) {
                return readSlideFeed(parser);
            } else {
                return readQuizFeed(parser);
            }
        } finally {
            in.close();
        }
    }
    private List<QuizEntry> readQuizFeed(XmlPullParser parser) throws XmlPullParserException, IOException {

        Log.d("SlideParser", "enter readQuizFeed");

        List<QuizEntry> entries = new ArrayList<QuizEntry>();

        parser.require(XmlPullParser.START_TAG, null, "quiz");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("entry")) {
                entries.add(readQuizEntry(parser));
                parser.nextTag();
            } else {
                Log.e("QuizParser", "Unexpected quiz XML format, tried to read " + parser.getName());
            }
        }
        return entries;
    }

    // This returns a list of one element to make it compatible with the quiz return type
    private List<SlideContent> readSlideFeed(XmlPullParser parser) throws XmlPullParserException, IOException {

        Log.d("SlideParser", "enter readSlideFeed");

        SlideContent content = new SlideContent();

        //parser.require(XmlPullParser.START_TAG, null, "feed");
        while (parser.next() != XmlPullParser.END_TAG) {

            Log.d("SlideParser", "Beggining of readSlideFeed loop");
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("video")) {
                parser.next();
                content.videoFileName = parser.getText();
                Log.d("SlideParser", "read video file name = " + content.videoFileName);
                parser.nextTag();
                parser.require(XmlPullParser.END_TAG, null, "video");
            } else if (name.equals("html_formatted_text")) {
                parser.next();
                content.htmlText = parser.getText();
                Log.d("SlideParser", "read htmlText = " + content.htmlText);
                parser.nextTag();
                parser.require(XmlPullParser.END_TAG, null, "html_formatted_text");
            } else {
                Log.e("SlideParser", "Unexpected slide XML format sent to readSlideFeed");
            }
        }
        List<SlideContent> slideContentWrapper = new ArrayList<SlideContent>();
        slideContentWrapper.add(content);
        return slideContentWrapper;
    }

    // Parses the contents of a quiz entry. Builds a QuizEntry object for entry passed in and
    // returns it.
    private QuizEntry readQuizEntry(XmlPullParser parser) throws XmlPullParserException, IOException {

        Log.d("SlideParser", "enter readQuizEntry");
        parser.require(XmlPullParser.START_TAG, null, "entry");
        QuizEntry entry = new QuizEntry();
        while (parser.next() != XmlPullParser.END_TAG) {
            Log.d("SlideParser", "At beggining of loop");

            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            Log.d("SlideParser", "current name = " + name);
            if (name.equals("question")) {
              Log.d("SlideParser", "read question tag");
              parser.next();
              entry.question = parser.getText();
              Log.d("SlideParser", "question = " + entry.question);
              parser.nextTag();
              parser.require(XmlPullParser.END_TAG, null, "question");
            } else if (name.contains("option")) {
                Log.d("SlideParser", "read option tag");
                parser.next();
                entry.options.add(parser.getText());
                parser.nextTag();

            } else if (name.equals("answer")) {
                Log.d("SlideParser", "read correct answer tag");
                parser.next();
                entry.correctAnswer = Integer.parseInt(parser.getText());
                parser.nextTag();
                parser.require(XmlPullParser.END_TAG, null, "answer");
            }

        }
        return entry;
    }

    public static class QuizEntry {
        public String question;
        public List<String> options = new ArrayList<String>();
        public int correctAnswer;
    }

    public static class SlideContent {
        public String videoFileName;
        public String htmlText;
    }





}
