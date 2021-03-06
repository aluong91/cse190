package com.example.bidit;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;


public class ImagePagerActivity extends BiditActivity {

	private static final String STATE_POSITION = "STATE_POSITION";
	private static final String ADAPTER = "ADAPTER";
	
	private DisplayImageOptions options;
	private int pagerPosition = 0;
	
	protected ImageLoader imageLoader;
	protected ImagePagerAdapter imgpgradapter;
	protected ViewPager pager;
	protected int loadlimit = 10;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_image_pager);
		
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
		imageLoader = ImageLoader.getInstance();
		imageLoader.init(config);

		if(!isNetworkOnline())
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(ImagePagerActivity.this);
            builder.setCancelable(false);
            builder.setTitle("No Network Detected");
            builder.setMessage("Please check your network connection!");
            builder.setInverseBackgroundForced(true);
            builder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                int which) {
                        	EasyTracker easyTracker = EasyTracker.getInstance(ImagePagerActivity.this);
                        	easyTracker.send(MapBuilder
                					.createEvent("ui_action",
                							     "button_click",
                							     "no_network_ok",
                							     null)
                					.build()
                			);
                            dialog.dismiss();
                            finish();
                        }
                    });
            
            AlertDialog alert = builder.create();
            alert.show();
		}
		
		
		if (savedInstanceState != null) {

			pagerPosition = savedInstanceState.getInt(STATE_POSITION, 0);
			ArrayList<Ad> temp = savedInstanceState.<Ad>getParcelableArrayList(ADAPTER);
			imgpgradapter = new ImagePagerAdapter(temp);
			pager = (ViewPager) findViewById(R.id.pager);
			pager.setAdapter(imgpgradapter);
			pager.setCurrentItem(pagerPosition);

		}
		else
		{
			new RequestAdsTask().execute();
			pager = (ViewPager) findViewById(R.id.pager);
			imgpgradapter = new ImagePagerAdapter();
			pager.setCurrentItem(pagerPosition);
		}
		
		options = new DisplayImageOptions.Builder()
		.showImageForEmptyUri(R.drawable.ic_empty)
		.showImageOnFail(R.drawable.ic_error)
		.resetViewBeforeLoading(true)
		.cacheOnDisc(true)
		.imageScaleType(ImageScaleType.EXACTLY)
		.bitmapConfig(Bitmap.Config.RGB_565)
		.considerExifParams(true)
		.displayer(new FadeInBitmapDisplayer(300))
		.build();
		
	    pager.setOnTouchListener(new OnTouchListener() {
	        @Override
	        public boolean onTouch(View v, MotionEvent e) {               
	            switch (e.getAction() & MotionEvent.ACTION_MASK) {
	                case MotionEvent.ACTION_DOWN:
	                    break;
	                case MotionEvent.ACTION_MOVE:
	                	EasyTracker easyTracker = EasyTracker.getInstance(ImagePagerActivity.this);
						easyTracker.send(MapBuilder
								.createEvent("ui_action",
										     "swipe",
										     "image_pager",
										     null)
								.build()
						);
	                    if(pagerPosition+1 == imgpgradapter.getCount())
	                    {
	                    	new RequestMoreAdsTask().execute();
	                    }
	                    break;
	                case MotionEvent.ACTION_UP: 
	                    break;
	           }
	           return false;
	       }
	   });
	   
	   
		pager.setOnPageChangeListener(new OnPageChangeListener(){

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageSelected(int arg0) {
				EasyTracker easyTracker = EasyTracker.getInstance(ImagePagerActivity.this);
				easyTracker.send(MapBuilder
						.createEvent("ui_action",
								     "swipe",
								     "image_pager",
								     null)
						.build()
				);
				pagerPosition = arg0;
				if(arg0 + 1 == imgpgradapter.getCount())
				{
					new RequestMoreAdsTask().execute();
				}
				
			}		
		});
		
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (imageLoader != null)
			imageLoader.destroy();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		
		outState.putInt(STATE_POSITION, pager.getCurrentItem());
		outState.putParcelableArrayList(ADAPTER, imgpgradapter.getAdapter());
		
	}
	
	private class ImagePagerAdapter extends PagerAdapter {

		private LayoutInflater inflater;
		private ArrayList<Ad> adapter;

		ImagePagerAdapter() {
			adapter = new ArrayList<Ad>();
			inflater = getLayoutInflater();
		}
		
		ImagePagerAdapter(ArrayList<Ad> ads) {
			adapter = ads;
			inflater = getLayoutInflater();
		}
		
		public ArrayList<Ad> getAdapter()
		{
			return adapter;
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public int getCount() {
			return adapter.size();
		}

		@Override
		public Object instantiateItem(ViewGroup view, final int position) {
			
			View imageLayout = inflater.inflate(R.layout.item_pager_image, view, false);
			assert imageLayout != null;
			ImageView imageView = (ImageView) imageLayout.findViewById(R.id.image);
			final ProgressBar spinner = (ProgressBar) imageLayout.findViewById(R.id.loading);
			
			imageView.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
						Bid bid = new Bid(null, null, null, adapter.get(position));
						BidDialogFragment bdf = new BidDialogFragment(bid);
						bdf.show(getSupportFragmentManager(), "BidDialog");

				}
			});
			
			imageLoader.displayImage(adapter.get(position).getImagePath(), imageView, options, new SimpleImageLoadingListener() {
				@Override
				public void onLoadingStarted(String imageUri, View view) {
					spinner.setVisibility(View.VISIBLE);
				}

				@Override
				public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
					EasyTracker easyTracker = EasyTracker.getInstance(ImagePagerActivity.this);
					String message = null;
					switch (failReason.getType()) {
						case IO_ERROR:
							message = "Input/Output error";
							easyTracker.send(MapBuilder
									.createException(message, false)
									.build()
							);
							break;
						case DECODING_ERROR:
							message = "Image can't be decoded";
							easyTracker.send(MapBuilder
									.createException(message, false)
									.build()
							);
							break;
						case NETWORK_DENIED:
							message = "Downloads are denied";
							easyTracker.send(MapBuilder
									.createException(message, false)
									.build()
							);
							break;
						case OUT_OF_MEMORY:
							message = "Out Of Memory error";
							easyTracker.send(MapBuilder
									.createException(message, false)
									.build()
							);
							break;
						case UNKNOWN:
							message = "Unknown error";
							easyTracker.send(MapBuilder
									.createException(message, false)
									.build()
							);
							break;
					}
					Toast.makeText(ImagePagerActivity.this, message, Toast.LENGTH_SHORT).show();

					spinner.setVisibility(View.GONE);
				}

				@Override
				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
					spinner.setVisibility(View.GONE);
				}
			});

			view.addView(imageLayout, 0);
			return imageLayout;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view.equals(object);
		}

		@Override
		public void restoreState(Parcelable state, ClassLoader loader) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}
		
		public void addAll(Ad... ads){
			for(Ad ad : ads){
				adapter.add(ad);
			}
		}
		
	}
	
	@Override
	public void onLoginSuccessful() {
		// TODO Auto-generated method stub
		
	}
	
	public class RequestAdsTask extends AsyncTask<Void, Ad, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			
			int offset = 0;
			String rangeurl = "";
			try {
				rangeurl = "?q=" + URLEncoder.encode("{\"offset\":", "UTF-8")
						+ URLEncoder.encode(offset+"", "UTF-8")
						+ URLEncoder.encode(",\"limit\":", "UTF-8")
						+ URLEncoder.encode(loadlimit+"", "UTF-8")
						+ URLEncoder.encode("}", "UTF-8");
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			HttpGet request = new HttpGet(Util.AD_API + rangeurl);
			try {
				
				HttpResponse response = Util.getHttpClient().execute(request);
				String content = EntityUtils.toString(response.getEntity());
				JSONObject json = new JSONObject(content);
				JSONArray objects = json.getJSONArray("objects");
				for (int i = 0; i < objects.length(); ++i) {
					JSONObject o = objects.getJSONObject(i);
					User seller = new User(o.getString("email"));
					BigDecimal price = new BigDecimal(o.getDouble("price"));
					String description = o.getString("description");
					String imageUrl = (Util.BASE_URL + "/uploads/" + o.getString("id")+".jpg");
					Ad ad = new Ad(seller, price, description, imageUrl);
					ad.setId(o.getInt("id"));
					publishProgress(ad);
				}
			} catch (ClientProtocolException e) {
				EasyTracker easyTracker = EasyTracker.getInstance(ImagePagerActivity.this);
				easyTracker.send(MapBuilder
						.createException(new StandardExceptionParser(ImagePagerActivity.this, null)
							.getDescription(Thread.currentThread().getName(), e),
							false)
						.build()
				);
				e.printStackTrace();
			} catch (IOException e) {
				EasyTracker easyTracker = EasyTracker.getInstance(ImagePagerActivity.this);
				easyTracker.send(MapBuilder
						.createException(new StandardExceptionParser(ImagePagerActivity.this, null)
							.getDescription(Thread.currentThread().getName(), e),
							false)
						.build()
				);
				e.printStackTrace();
			} catch (JSONException e) {
				EasyTracker easyTracker = EasyTracker.getInstance(ImagePagerActivity.this);
				easyTracker.send(MapBuilder
						.createException(new StandardExceptionParser(ImagePagerActivity.this, null)
							.getDescription(Thread.currentThread().getName(), e),
							false)
						.build()
				);
				e.printStackTrace();
			} 
			return null;
		}

		@Override
		protected void onProgressUpdate(Ad... ads) {
			imgpgradapter.addAll(ads);
			imgpgradapter.notifyDataSetChanged();
			
		}
		
		@Override
		protected void onPostExecute(Void vd) {
			//wait for ad objects before rendering
			pager.setAdapter(imgpgradapter);
			pager.setCurrentItem(pagerPosition);
		}
		
	}
	

	public class RequestMoreAdsTask extends AsyncTask<Void, Ad, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			
			
			int offset = imgpgradapter.getCount();
			String rangeurl = "";
			try {
				rangeurl = "?q=" + URLEncoder.encode("{\"offset\":", "UTF-8")
						+ URLEncoder.encode(offset+"", "UTF-8")
						+ URLEncoder.encode(",\"limit\":", "UTF-8")
						+ URLEncoder.encode(loadlimit+"", "UTF-8")
						+ URLEncoder.encode("}", "UTF-8");
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			HttpGet request = new HttpGet(Util.AD_API + rangeurl);
			try {
				
				HttpResponse response = Util.getHttpClient().execute(request);
				String content = EntityUtils.toString(response.getEntity());
				JSONObject json = new JSONObject(content);
				JSONArray objects = json.getJSONArray("objects");
				for (int i = 0; i < objects.length(); ++i) {
					JSONObject o = objects.getJSONObject(i);
					User seller = new User(o.getString("email"));
					BigDecimal price = new BigDecimal(o.getDouble("price"));
					String description = o.getString("description");
					String imageUrl = (Util.BASE_URL + "/uploads/" + o.getString("id")+".jpg");
					Ad ad = new Ad(seller, price, description, imageUrl);
					ad.setId(o.getInt("id"));
					publishProgress(ad);
				}
			} catch (ClientProtocolException e) {
				EasyTracker easyTracker = EasyTracker.getInstance(ImagePagerActivity.this);
				easyTracker.send(MapBuilder
						.createException(new StandardExceptionParser(ImagePagerActivity.this, null)
							.getDescription(Thread.currentThread().getName(), e),
							false)
						.build()
				);
				e.printStackTrace();
			} catch (IOException e) {
				EasyTracker easyTracker = EasyTracker.getInstance(ImagePagerActivity.this);
				easyTracker.send(MapBuilder
						.createException(new StandardExceptionParser(ImagePagerActivity.this, null)
							.getDescription(Thread.currentThread().getName(), e),
							false)
						.build()
				);
				e.printStackTrace();
			} catch (JSONException e) {
				EasyTracker easyTracker = EasyTracker.getInstance(ImagePagerActivity.this);
				easyTracker.send(MapBuilder
						.createException(new StandardExceptionParser(ImagePagerActivity.this, null)
							.getDescription(Thread.currentThread().getName(), e),
							false)
						.build()
				);
				e.printStackTrace();
			} 
			return null;
		}

		@Override
		protected void onProgressUpdate(Ad... ads) {
			imgpgradapter.addAll(ads);
			imgpgradapter.notifyDataSetChanged();	
		}
		
		@Override
		protected void onPostExecute(Void vd) {
			
		}
		
	}
	
}
