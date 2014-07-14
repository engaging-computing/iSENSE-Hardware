/*
 * Copyright (c) 2009, iSENSE Project. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials
 * provided with the distribution. Neither the name of the University of
 * Massachusetts Lowell nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific
 * prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */

package edu.uml.cs.pincomm;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

/**
 * 
 * @author johnfertitta
 *
 * This class handles the loading and caching of all images for the application.  Methods are provided to fetch images and return them as drawables or bitmaps, for both threaded and blocking.
 *
 */

public class ImageManager {
    private final Map<String, Bitmap> bitmapMap;
    private static ImageManager instance = null;
    private HashMap<String, ImageView> viewMap;
    private ExecutorService executor;
    private Collection<Exec> execs;
    
    private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message message) {
			String url = (String) message.obj;
			Bitmap bitmap = bitmapMap.get(url);
			ImageView imageView = viewMap.get(url);
			imageView.setImageBitmap(bitmap);
			viewMap.remove(url);
		}
	};
	
	class Exec implements Callable<Integer> {
		private String url = "";
		
		public Exec(String u) {
			url = u;
		}
		
		@Override
		public Integer call() throws Exception {
			if (fetchBitmap(url) != null) {
				Message message = handler.obtainMessage(1, url);
				handler.sendMessage(message);
				return 1;
			}
			return 0;
		}
		
	}
    
    protected ImageManager() {
    	bitmapMap = new HashMap<String, Bitmap>();
    	viewMap = new HashMap<String, ImageView>();
    	executor = Executors.newFixedThreadPool(1);
    }
    
    /**
     * Get the single instance of the ImageManager
     *
     * @return ImageManager
     */
    public static ImageManager getInstance() {
    	if (instance == null) {
    		instance = new ImageManager();
    	}
    	
    	return instance;
    }

    /**
     * Fetch an image from a URL and return it as a Bitmap
     * 
     * @param urlString
     * @return Bitmap
     */
    public Bitmap fetchBitmap(String urlString) {
    	if (bitmapMap.containsKey(urlString)) {
    		return bitmapMap.get(urlString);
    	}
    	
    	try {
    		InputStream is = fetch(urlString);
    		Bitmap bitmap = BitmapFactory.decodeStream(is);    		
    		bitmapMap.put(urlString, bitmap);
    		
    		return bitmap;
    	} catch (MalformedURLException e) {
    		return null;
    	} catch (IOException e) {
    		return null;
    	} catch (IllegalStateException e) {
    		return null;
    	} catch (OutOfMemoryError e) {
    		return null;
    	}
    }
    
    
    /**
     * Fetch an image from a URL on a thread.  Once fetched it will be displayed in the provided image view.
     * 
     * @param urlString
     * @param imageView
     */
    public void fetchBitmapOnThread(final String urlString, ImageView imageView) {
    	if (bitmapMap.containsKey(urlString)) {
			imageView.setImageBitmap(bitmapMap.get(urlString));
			return;
    	}
    	
        	viewMap.put(urlString, imageView);
    		
    		executor.submit(new Exec(urlString));
    	
    }

    private InputStream fetch(String urlString) throws MalformedURLException, IOException, IllegalStateException {
    	DefaultHttpClient httpClient = new DefaultHttpClient();
    	HttpGet request = new HttpGet(urlString);
    	HttpResponse response = httpClient.execute(request);
    	return response.getEntity().getContent();
    }
}
