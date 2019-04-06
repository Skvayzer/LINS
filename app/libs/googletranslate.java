
/*
 *
 * The Google Cloud Translation API Client Library can be downloaded from:
 * https://developers.google.com/api-client-library/java/apis/translate/v2
 *
 */

package com.sibaihm.googletranslate;

import android.os.AsyncTask;
import android.util.Log;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.translate.Translate;
import com.google.api.services.translate.model.TranslationsListResponse;
import java.util.Arrays;


public class GoogleTranslate extends AsyncTask<String, Void, String> {

    /*
     * Your Google API Key here
    */
	
    private final String API_KEY = "";
    
    /*
     * Performing the translation in background process
     */
    
    @Override
    protected String doInBackground(String... params){
    	
    	/*
    	 *  The text which will be translated
    	 */

        final String textToTranslate = params[0];
        
        /*
         * The source language to be translated
         */
        
        final String SOURCE_LANGUAGE = params[1];
        
        /*
         * The wished language to be translated to
         */
        
        final String TARGET_LANGUAGE = params[2];
        
        try {
            
            /*
             * Objects needed for the translate object
             */
        	
            NetHttpTransport netHttpTransport 	= new NetHttpTransport();
 
            JacksonFactory jacksonFactory 		= new JacksonFactory();
            
            /*
             * Creating the Google Translate object
             */
            
            Translate translate = new Translate.Builder(netHttpTransport, jacksonFactory, null).build();
            
            /*
             * Setting the textToTranslate, the API_KEY and TARGET_LANGUAGE
             */

            Translate.Translations.List listToTranslate = translate.new Translations().list(
                Arrays.asList(textToTranslate), TARGET_LANGUAGE).setKey(API_KEY);
            
            /*
             * If you want to let Google detects the language automatically, remove the next line
             * This line set the source language of the translated text
             */
            
            listToTranslate.setSource(SOURCE_LANGUAGE);
            
            /*
             * Executing the translation and saving the response in the response object
             */

            TranslationsListResponse response = listToTranslate.execute();

            /*
             * The response has the form of: {"translatedText":"blabla"}
             * We need only the translated text between the second double quotes pair
             * therefore using getTranslatedText
             */
            
            return response.getTranslations().get(0).getTranslatedText();
        } catch (Exception e){
        	
            Log.e("Google Response ", e.getMessage());
            
            /*
             * I would return empty string if there is an error
             * to let the method which invoked the translating method know that there is an error
             * and subsequently it deals with it
             */
            
            return "";
        }
    }
}
