/*
 * Copyright 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mgyaware.instruction;

import android.app.ActionBar;
import android.app.Fragment;
import android.graphics.Color;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.VideoView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;

/**
 * A fragment representing a single step in a wizard. The fragment shows a dummy title indicating
 * the page number, along with some dummy text.
 *
 * <p>This class is used by the {@link //CardFlipActivity} and {@link
 * ScreenSlideActivity} samples.</p>
 */
public class ScreenSlidePageFragment extends Fragment {
    /**
     * The argument key for the page number this fragment represents.
     */
    public static final String ARG_PAGE = "page";

    public static final String ARG_PAGE_TYPE = "type";

    /**
     * The fragment's page number, which is set to the argument value for {@link #ARG_PAGE}.
     */
    private int mPageNumber;

    /**
     * ordinal of pageType represented by pageType enum
     */
   // private SlidePageType pageType;


    /**
     * Factory method for this fragment class. Constructs a new fragment for the given page number.
     * Modification by Sam Waggoner: also accepts slidePageType
     */
    public static ScreenSlidePageFragment create(int pageNumber) {
        ScreenSlidePageFragment fragment = new ScreenSlidePageFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, pageNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public ScreenSlidePageFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageNumber = getArguments().getInt(ARG_PAGE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        try {
            return setSlidePageContent(inflater, container, savedInstanceState);
        }
        catch(XmlPullParserException xmlE)
        {
            Log.e("ScreenSlidePageFragment", "Unable to parse content XML");
            Log.e("ScreenSlidePageFragment", Log.getStackTraceString(xmlE));
            return null;
        }
        catch(IOException ioE)
        {
            Log.e("ScreenSlidePageFragment", "Unable to read content xml file");
            Log.e("ScreenSlidePageFragment", Log.getStackTraceString(ioE));

            return null;
        }

 /*       switch (pageType) {
            case VIDEO_WITH_SLIDE:

                setVideoContent(rootViewSlide);
                setSlideContent(rootViewSlide);
                return rootViewSlide;
            case QUIZ:
                // Inflate the layout containing a quiz.
                ViewGroup rootViewQuiz = (ViewGroup) inflater
                        .inflate(R.layout.fragment_screen_quiz_page, container, false);
                try {
                    setQuizContent(rootViewQuiz);
                }
                catch(XmlPullParserException xmlE)
                {
                    Log.e("ScreenSlidePageFragment", "Unable to parse quiz XML");
                }
                catch (IOException ioE) {
                    Log.e("ScreenSlidePageFragment", "unable to open quiz xml file");
                }
                return rootViewQuiz;
            default:
                Log.e("ScreenSlidePageFragment", "Page type selection failed");
                return null; // This is an illegal condition.

        }*/
        // Set the title view to show the page number.

        // Following code created based on template:
        // http://stackoverflow.com/questions/17399351/how-to-play-mp4-video-in-videoview-in-android





    }

    private View setSlidePageContent(LayoutInflater inflater, ViewGroup container,
                                      Bundle savedInstanceState) throws XmlPullParserException, IOException
    {
        SlideParser quizParser = new SlideParser();
        String resourceFileName = "slide" + mPageNumber;
        Log.d("ScreenSlidePageFragment", "resourceFileName = " + resourceFileName);
        int resIdentifier = this.getActivity().getResources().getIdentifier("slide" + mPageNumber, "raw", getActivity().getPackageName());
        List slideContentList =
                quizParser.parse(this.getActivity().getResources().openRawResource(resIdentifier));
        if (slideContentList.get(0).getClass() == SlideParser.SlideContent.class) {
            // Inflate the layout containing a video and body text.
            ViewGroup rootViewSlide = (ViewGroup) inflater
                    .inflate(R.layout.fragment_screen_slide_page, container, false);
            setSlideContent(rootViewSlide, slideContentList);
            return rootViewSlide;
        }
        else if (slideContentList.get(0).getClass() == SlideParser.QuizEntry.class) {
            ViewGroup rootViewQuiz = (ViewGroup) inflater
                    .inflate(R.layout.fragment_screen_quiz_page, container, false);
            setQuizContent(rootViewQuiz, slideContentList);
            return rootViewQuiz;
        } else {
            return null; //illegal condition
        }

    }

    private void setVideoContent(ViewGroup rootView)
    {
        VideoView videoView = ((VideoView) rootView.findViewById(R.id.video));
        MediaController vidMediaController = new MediaController(getActivity());
        videoView.setVideoPath("android.resource://" + getActivity().getPackageName() + "/" + R.raw.clipcanvas_14348_offline);
        vidMediaController.setMediaPlayer(videoView);
        videoView.setMediaController(vidMediaController);
        videoView.requestFocus();
        videoView.start();
    }

    private void setSlideContent(ViewGroup rootView, List<SlideParser.SlideContent> content )
    {
        ((TextView) rootView.findViewById(android.R.id.content)).setText(
                "information" + mPageNumber);
    }

    private void setQuizContent(ViewGroup rootView, List<SlideParser.QuizEntry> content)
            throws XmlPullParserException, IOException
    {

        LinearLayout quizLayout = (LinearLayout) rootView.findViewById(R.id.quiz_layout);
        QuizButtonListener buttonListener = new QuizButtonListener();
        for (SlideParser.QuizEntry entry : content)
        {
            TextView questionText = new TextView(getActivity());
            questionText.setText(entry.question);
            quizLayout.addView(questionText);
            LinearLayout optionLayout = new LinearLayout(getActivity());
            optionLayout.setOrientation(LinearLayout.VERTICAL);
            Log.d("ScreenSlidePageFragment", "Number of answer options = " + entry.options.size());
            for (String optionText : entry.options) {
                CheckBox optionButton = new CheckBox(getActivity());
                optionButton.setVisibility(CheckBox.VISIBLE);
                optionButton.setText(optionText);
                optionButton.setGravity(CheckBox.TEXT_ALIGNMENT_GRAVITY);
                //optionButton.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
                //optionButton.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
                optionButton.setOnClickListener(buttonListener);
                optionLayout.addView(optionButton);

            }
            quizLayout.addView(optionLayout);


        }

    }

    /**
     * Returns the page number represented by this fragment object.
     */
    public int getPageNumber() {
        return mPageNumber;
    }

    /**
     * Represent types of content slide pagers can display so multiple types of slides can be
     * created
     */
    public static enum SlidePageType {
        VIDEO_WITH_SLIDE, QUIZ
    }


    public class QuizButtonListener implements View.OnClickListener
    {

        @Override
        public void onClick(View v) {
        }
    }
}
