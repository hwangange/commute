package com.interns.team3.openstax.myttsapplication;


/*
import com.google.cloud.texttospeech.v1.AudioConfig;
import com.google.cloud.texttospeech.v1.AudioEncoding;
import com.google.cloud.texttospeech.v1.SsmlVoiceGender;
import com.google.cloud.texttospeech.v1.SynthesisInput;
import com.google.cloud.texttospeech.v1.SynthesizeSpeechResponse;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1.VoiceSelectionParams;
import com.google.protobuf.ByteString;

*/

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class demo {


    public static void main (String args[]) throws Exception {
        System.out.println("roflmao");
        //firstTest("Hi! This is a test.");
        /*try {
            URL my_url = new URL("https://openstax.org/details/books/calculus-volume-3");
            BufferedReader br = new BufferedReader(new InputStreamReader(my_url.openStream()));
            String strTemp = "";
            while(null != (strTemp = br.readLine())){
                System.out.println(strTemp);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } */

       /* Document doc = Jsoup.connect("https://wikipedia.org").get();
        String title = doc.title();
        System.out.println(title);
        Elements logo = doc.getElementsByClass("logo-white");
        Elements links = doc.getElementsByTag("a");

        System.out.println("umm: " + logo.html());
        System.out.println("Links: ");
        for (Element link : links) {
            String linkHref = link.attr("href");
            String linkText = link.text();
            System.out.println(linkHref + ": " + linkText);
        }
        */


        /*
            This is the way to get file in "Context"?


            StringBuilder buf=new StringBuilder();
        InputStreamReader inputStream = new InputStreamReader(getAssets().open("filename.html"));
        BufferedReader bufferedReader = new BufferedReader(inputStream);
        String str;
        while ((str=bufferedReader.readLine()) != null) {
            buf.append(str);
        }
        Document doc = Jsoup.parse(buf.toString());
         *
         *
         */


        File input = new File("/Users/Linda/AndroidStudioProjects1/MyTTSApplication/app/src/main/res/values/col11629_1.7_complete/collection.xml");
       Document doc = Jsoup.parse(input, "UTF-8", "");


       String title = doc.title();
       System.out.println(title);
       Element body =  doc.body();
       Elements subcollections = body.getElementsByTag("col:subcollection");
       for (Element sub : subcollections)
       {
           String subTitle = sub.getElementsByTag("md:title").first().ownText();
           System.out.println("Title: " + subTitle);
           if(sub.hasText())
               System.out.println(sub.ownText() + "\t" + sub.attributes());

           Elements modules = sub.getElementsByTag("col:module");
           for (Element mod : modules)
           {
               String modTitle = mod.getElementsByTag("md:title").first().ownText();
               System.out.println("\t"+modTitle + "\t" + mod.attributes().get("document"));
           }

           System.out.println("------------------------");
       }


       /* important code. Keep this. */

       /*
       //src/main/res/values/col11629_1.7_complete/m48993/index.cnxml.html
        // ../../../../../../res/values/col11629_1.7_complete/m48993/index.cnxml.html
        // /Users/Linda/AndroidStudioProjects1/MyTTSApplication/app/src/main/res/values/col11629_1.7_complete/m48993/index.cnxml.html


       File input = new File("/Users/Linda/AndroidStudioProjects1/MyTTSApplication/app/src/main/res/values/col11629_1.7_complete/m48993/index.cnxml.html");
       Document doc = Jsoup.parse(input, "UTF-8", "");
       String title = doc.title();
       System.out.println(title);
       Element body =  doc.body();
       Elements bodyElements = body.getAllElements();
       for (Element elem : bodyElements)
       {
           System.out.println("Tag: " + elem.tag()); // make tag go here
           if(elem.hasText())
               System.out.println(elem.ownText());
           System.out.println("Attributes: " + elem.attributes());
           System.out.println("------------------------");
       }
       */

       /* end important code. Keep this. */

    }



    public static void firstTest(String text) throws Exception
    {
       // TextToSpeechClient textToSpeechClient = TextToSpeechClient.create();
    }

    /**
     * Demonstrates using the Text to Speech client to synthesize text or ssml.
     * @param text the raw text to be synthesized. (e.g., "Hello there!")
     * @throws Exception on TextToSpeechClient Errors.
     */
    public static void synthesizeText(String text)
            throws Exception {
      /*  System.out.println("uhhh hello?");
        // Instantiates a client
        try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {
            // Set the text input to be synthesized
            SynthesisInput input = SynthesisInput.newBuilder()
                    .setText(text)
                    .build();
            System.out.println("I set the text input to be synthesized!");

            // Build the voice request
            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                    .setLanguageCode("en-US") // languageCode = "en_us"
                    .setSsmlGender(SsmlVoiceGender.FEMALE) // ssmlVoiceGender = SsmlVoiceGender.FEMALE
                    .build();
            System.out.println("I built the voice request!");

            // Select the type of audio file you want returned
            AudioConfig audioConfig = AudioConfig.newBuilder()
                    .setAudioEncoding(AudioEncoding.MP3) // MP3 audio.
                    .build();
            System.out.println("I selected the type of audio file to be returned!");

            // Perform the text-to-speech request
            SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice,
                    audioConfig);
            System.out.println("I performed the text-to-speech request!");

            // Get the audio contents from the response
            ByteString audioContents = response.getAudioContent();
            System.out.println("I got the audio contents from the response!");

            // Write the response to the output file.
            try (OutputStream out = new FileOutputStream("output.mp3")) {
                out.write(audioContents.toByteArray());
                System.out.println("Audio content written to file \"output.mp3\"");
            }
        }*/
    }
}
