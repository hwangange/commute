package com.interns.team3.openstax.myttsapplication;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.polly.AmazonPollyPresigningClient;
import com.amazonaws.services.polly.model.DescribeVoicesRequest;
import com.amazonaws.services.polly.model.DescribeVoicesResult;
import com.amazonaws.services.polly.model.OutputFormat;
import com.amazonaws.services.polly.model.SynthesizeSpeechPresignRequest;
import com.amazonaws.services.polly.model.Voice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public abstract class AudioClient {
    String audioFolder;
    String voice;
    String language;
    boolean isMale;

    AudioClient(String folder) {
        this.audioFolder = folder;
        makeFolder(folder);
    }

    public String getAudioFolder() {
        return this.audioFolder;
    }

    public void setAudioFolder(String newFolder) {
        this.audioFolder = newFolder;
    }

    public String getVoice() {
        return this.voice;
    }

    public void setVoice(String newVoice) {
        this.voice = newVoice;
    }

    public String getLanguage() {
        return this.language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getGender() {
        return this.isMale ? "Male" : "Female";
    }

    private static void makeFolder(String folder) {
        try {
            Files.createDirectories(Paths.get(folder));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    static void listAllVoices(String client) {
//        switch (client.toLowerCase()) {
//            case "google":  {
////                GoogleClient.listAllSupportedVoices();
//                break;
//            }
//            case "amazon": {
//                new AmazonClient("", null).listAllSupportedVoices();
//                break;
//            }
//            default: {
//                System.err.printf("ERROR: \"%s\" is not a supported client. Please choose one of \"Google\" or \"Amazon\"\n", client);
//            }
//        }
//    }

    static void combineMP3(String folder, String outputFile, String file1, String file2, boolean deleteOldFiles, boolean debug) {
        try {
            FileInputStream input1 = new FileInputStream(folder + file1 + ".mp3");
            FileInputStream input2 = new FileInputStream(folder + file2 + ".mp3");
            SequenceInputStream seqStream = new SequenceInputStream(input1, input2);
            FileOutputStream output = new FileOutputStream(folder + outputFile + ".mp3");

            int temp;
            while((temp = seqStream.read()) != -1) {
                output.write(temp);
            }

            input1.close();
            input2.close();
            seqStream.close();
            output.close();

            if (debug) {
                System.out.printf("Audio files \"%s.mp3\" and \"%s.mp3\" merged into file \"%s.mp3\"\n", file1, file2, outputFile);
            }

            if (deleteOldFiles) {
                deleteFile(folder + file1);
                deleteFile(folder + file2);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void combineMP3(String folder, String outputFile, List<String> inputFiles, boolean deleteOldFiles, boolean debug) {
        try {
            List<FileInputStream> inputStreams = new ArrayList<>();
            int length = inputFiles.size();
            for (String input : inputFiles) {
                inputStreams.add(new FileInputStream(folder + input + ".mp3"));
            }

            SequenceInputStream seqStream = new SequenceInputStream(Collections.enumeration(inputStreams));
            FileOutputStream output = new FileOutputStream(folder + outputFile + ".mp3");
            int temp;
            while((temp = seqStream.read()) != -1) {
                output.write(temp);
            }

            if (debug) {
                System.out.printf("%d audio files merged into file \"%s.mp3\"\n\n", length, outputFile);
            }

            for (FileInputStream inputStream : inputStreams) {
                inputStream.close();
            }
            seqStream.close();
            output.close();

            if (deleteOldFiles) {
                for (String input: inputFiles) {
                    deleteFile(folder + input);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void deleteFile(String fileString) {
        try {
            File file = new File(fileString + ".mp3");
            if (!(file.delete())) {
                System.out.printf("ERROR: Failed to delete file \"%s.mp3\"", fileString);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void synthesizeAudio(String fileName, boolean isSSML, List<String> contentList, boolean debug) {
        List<String> audioFiles = new ArrayList<>();
        for (int i = 0; i < contentList.size(); i++) {
            try {
                String subFileName = String.format(Locale.ENGLISH, "%s_%d", fileName, i);
                audioFiles.add(subFileName);
                String content = contentList.get(i);
                synthesizeAudio(subFileName, isSSML, content, debug);
            } catch (Exception e) {
                System.out.println(contentList.get(i));
                e.printStackTrace();
            }
        }
        combineMP3(this.audioFolder, fileName, audioFiles, true, debug);
    }

    public abstract void synthesizeAudio(String fileName, boolean isSSML, String content, boolean debug);

    public static class AmazonClient extends AudioClient {
        private String cognitoId = "us-east-1:1fe997b7-6fe9-4137-92ab-39a50d0e34b3";
        private Regions myRegion = Regions.US_EAST_1;
        private Context context;
        private AmazonPollyPresigningClient client;

        AmazonClient(String folder, Context context) {
            super(folder);
            this.context = context;
            this.voice = "Matthew";
            this.language = "en-US";
            this.isMale = true;
            initPollyClient();
        }

        AmazonClient(String folder, Context context, String voice) {
            super(folder);
            this.context = context;
            this.voice = voice;
            this.language = "en-US";
            initPollyClient();
        }

        AmazonClient(String folder, Context context, boolean isMale) {
            super(folder);
            this.context = context;
            this.voice = isMale ? "Matthew" : "Joanna";
            this.language = "en-US";
            this.isMale = isMale;
            initPollyClient();
        }

        AmazonClient(String folder, Context context, String voice, boolean isMale) {
            super(folder);
            this.context = context;
            this.voice = voice;
            this.language = "en-US";
            this.isMale = isMale;
            initPollyClient();
        }

        private void initPollyClient() {
            //Initialize the Amazon Cognito credentials provider.
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    this.context,
                    this.cognitoId,
                    this.myRegion
            );

            // Create a client that supports generation of presigned URLs.
            this.client = new AmazonPollyPresigningClient(credentialsProvider);
        }

        // to get voices, call new AmazonClient("",null).getSupportedVoices();
        public List<String> getSupportedVoices() {
            List<String> voices = new ArrayList<>();
            DescribeVoicesRequest allVoicesRequest = new DescribeVoicesRequest().withLanguageCode(this.language);
            try {
                String nextToken;
                do {
                    DescribeVoicesResult allVoicesResult = this.client.describeVoices(allVoicesRequest);
                    nextToken = allVoicesResult.getNextToken();
                    allVoicesRequest.setNextToken(nextToken);

                    for (Voice voice: allVoicesResult.getVoices()) {
                        voices.add(voice.getId());
//                        System.out.printf("%s: %s\n", "Name", voice.getName());
//                        System.out.printf("%s: %s\n", "Gender", voice.getGender());
//                        System.out.printf("%s: %s\n", "Id", voice.getId());
//                        System.out.printf("%s: %s\n", "LanguageCode", voice.getLanguageCode());
//                        System.out.printf("%s: %s\n", "LanguageName", voice.getLanguageName());
//                        System.out.println();
                    }
                } while (nextToken != null);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return voices;
        }

        private void writeToFile(String filename, URL streamURL, boolean debug) {
            try {
                writeToFile(filename, streamURL.openStream(), debug);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void writeToFile(String fileName, InputStream audioStream, boolean debug) {

            String file = this.audioFolder + fileName + ".mp3";
            byte[] buffer = new byte[1024 * 4];
            int readBytes;

            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                Log.i("WriteToFile", fileName);
                while ((readBytes = audioStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, readBytes);
                }
                outputStream.close();

                //showProgress
                ((MainActivity) context).runOnUiThread(() -> {
                    Fragment fragment = ((MainActivity) context).getActiveFragment();
                    if (fragment instanceof TextbookViewFragment) {
                        ((TextbookViewFragment) fragment).showProgress(Integer.parseInt(fileName));
                    }
                });

                if (debug) {
                    Log.i("Audio content written to file \"%s.mp3\"\n", fileName);
                    //System.out.printf("Audio content written to file \"%s.mp3\"\n", fileName);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void synthesizeAudio(String fileName, boolean isSSML, String content, boolean debug) {
            String textType = isSSML ? "ssml" : "text";
            SynthesizeSpeechPresignRequest synthReq = new SynthesizeSpeechPresignRequest()
                    .withText(content).withTextType(textType)
                    .withVoiceId(this.voice)
                    .withOutputFormat(OutputFormat.Mp3);

            new SynthesizeAudioTask(fileName, isSSML, content, debug).execute(synthReq);
        }

        private class SynthesizeAudioTask extends AsyncTask<SynthesizeSpeechPresignRequest, Void, String> {
            String fileName; // just a number
            boolean isSSML;
            String content;
            boolean debug;
            int count;

            SynthesizeAudioTask(String fileName, boolean isSSML, String content, boolean debug ){
                this.fileName = fileName;
                this.isSSML = isSSML;
                this.content = content;
                this.debug = debug;
                this.count = 0;
            }

            protected String doInBackground(SynthesizeSpeechPresignRequest... requests) {
                SynthesizeSpeechPresignRequest synthReq = requests[0];
                URL synthResURL = client.getPresignedSynthesizeSpeechUrl(synthReq); // formerly, this.client...
                writeToFile(fileName, synthResURL, debug);
                return "";
            }

            protected void onProgressUpdate() {
            }

            protected void onPostExecute(String s) {
            }
        }

        public void getVoiceDetails() {
            DescribeVoicesRequest allVoicesRequest = new DescribeVoicesRequest();

            try {
                String nextToken;
                boolean foundVoice = false;
                do {
                    DescribeVoicesResult allVoicesResult = this.client.describeVoices(allVoicesRequest);
                    nextToken = allVoicesResult.getNextToken();
                    allVoicesRequest.setNextToken(nextToken);

                    for (com.amazonaws.services.polly.model.Voice voice: allVoicesResult.getVoices()) {
                        if (voice.getId().equals(this.voice)) {
                            foundVoice = true;
                            System.out.printf("%s: %s\n", "Name", voice.getName());
                            System.out.printf("%s: %s\n", "Gender", voice.getGender());
                            System.out.printf("%s: %s\n", "Id", voice.getId());
                            System.out.printf("%s: %s\n", "LanguageCode", voice.getLanguageCode());
                            System.out.printf("%s: %s\n", "LanguageName", voice.getLanguageName());
                            break;
                        }
                    }
                    if (foundVoice) {
                        break;
                    }
                } while (nextToken != null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}