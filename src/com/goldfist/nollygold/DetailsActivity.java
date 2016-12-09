package com.goldfist.nollygold;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Config;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.goldfist.nollygold.ServiceAccess.ItemData;

public class DetailsActivity extends Activity {

	static ProgressDialog pd;
	DownloadManager downloadManager;
	long downloadReference;
	static String[] reviews;
	static boolean isReviewLoaded = false, paymentInProgress = false,
			isBuy = false, isPurchased = false, isRented = false,
			isFavourite = false, favResult = false;
	static String itemCode;
	static ItemData itemData;
	Handler loadDetailsHandler, loadUserReviewHandler, paymentHandler,
			favHandler;
	static ImageView imageView;
	static ImageButton rentButton, buyButton, streamButton, downloadButton,
			trailerButton, favouriteButton;
	static TextView titleView, yearView, priceView, sourceTextView;
	static ImageLoader imageLoader;
	static String serverImageUrl = new ServiceAccess().serverRootUrl
			+ "/rok/images/";

	static LinearLayout rentLay, buyLay, streamLay, downloadLay, actionLay,
			trailerLay, favouriteLay, shareLay;
	static ImageButton producerButton, castButton, synopsisButton,
			reviewButton, shareButton;
	static TextView pTextView, cTextView, sTextView, rTextView;

	static long paymentResult = 0;
	static Activity mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_details);

		mContext = this;

		Intent intent = getIntent();
		itemCode = intent.getStringExtra("itemCode");
		isRented = intent.getBooleanExtra("isRented", false);
		isPurchased = intent.getBooleanExtra("isPurchased", false);
		isFavourite = intent.getBooleanExtra("isFavourite", false);

		imageLoader = new ImageLoader(getApplicationContext());

		imageView = (ImageView) findViewById(R.id.imageView);

		rentLay = (LinearLayout) findViewById(R.id.rentLay);
		buyLay = (LinearLayout) findViewById(R.id.buyLay);
		streamLay = (LinearLayout) findViewById(R.id.streamLay);
		downloadLay = (LinearLayout) findViewById(R.id.downloadLay);
		actionLay = (LinearLayout) findViewById(R.id.actionLay);
		trailerLay = (LinearLayout) findViewById(R.id.trailerLay);
		favouriteLay = (LinearLayout) findViewById(R.id.favouriteLay);
		shareLay = (LinearLayout) findViewById(R.id.shareLay);

		pTextView = (TextView) findViewById(R.id.producerTextView);
		cTextView = (TextView) findViewById(R.id.castTextView);
		sTextView = (TextView) findViewById(R.id.synopsisTextView);
		rTextView = (TextView) findViewById(R.id.reviewTextView);

		producerButton = (ImageButton) findViewById(R.id.producerButton);
		producerButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (pTextView.getVisibility() == View.GONE) {
					pTextView.setVisibility(View.VISIBLE);
					producerButton.setImageResource(R.drawable.up);
				} else {
					pTextView.setVisibility(View.GONE);
					producerButton.setImageResource(R.drawable.down);
				}
			}
		});

		castButton = (ImageButton) findViewById(R.id.castButton);
		castButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (cTextView.getVisibility() == View.GONE) {
					cTextView.setVisibility(View.VISIBLE);
					castButton.setImageResource(R.drawable.up);
				} else {
					cTextView.setVisibility(View.GONE);
					castButton.setImageResource(R.drawable.down);
				}
			}
		});

		synopsisButton = (ImageButton) findViewById(R.id.synopsisButton);
		synopsisButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				if (sTextView.getVisibility() == View.GONE) {
					sTextView.setVisibility(View.VISIBLE);
					synopsisButton.setImageResource(R.drawable.up);
				} else {
					sTextView.setVisibility(View.GONE);
					synopsisButton.setImageResource(R.drawable.down);
				}

			}
		});

		reviewButton = (ImageButton) findViewById(R.id.reviewButton);
		reviewButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				if (isReviewLoaded) {
					if (rTextView.getVisibility() == View.GONE) {
						rTextView.setVisibility(View.VISIBLE);
						reviewButton.setImageResource(R.drawable.up);
					} else {
						rTextView.setVisibility(View.GONE);
						reviewButton.setImageResource(R.drawable.down);
					}
				} else {
					Thread loadUserReviewThread = new Thread() {
						public void run() {
							reviews = new ServiceAccess()
									.getItemReviews(itemData.code);

							loadUserReviewHandler.sendEmptyMessage(0);
						}
					};
					loadUserReviewThread.start();
				}
			}
		});

		rentButton = (ImageButton) findViewById(R.id.rentButton);
		rentButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				final int transactionPrice = itemData.price;
				AlertDialog.Builder alertDialog = new AlertDialog.Builder(
						DetailsActivity.this);

				// Setting Dialog Title
				alertDialog.setTitle("NollyGold");
				alertDialog.setMessage("N"
						+ String.valueOf(transactionPrice)
						+ " will be deducted from your account.\nDo you wish to continue?");
				alertDialog.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {

								final String userid = new ServiceAccess()
										.checkLoginStatus(mContext);// preferenceManager.getString("ilogin",
																	// null);

								if (userid != null) {

									actionLay.setEnabled(false);

									paymentInProgress = true;

									isBuy = false;

									pd = new ProgressDialog(mContext);
									pd.setTitle("Processing...");
									pd.setCancelable(false);
									pd.setIndeterminate(true);
									pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
									pd.show();

									Thread makePaymentThread = new Thread() {
										public void run() {

											paymentResult = new ServiceAccess()
													.makePayment(
															0,
															itemData.code,
															transactionPrice,
															itemData.expiryDays,
															userid, 0, mContext);

											paymentHandler.sendEmptyMessage(0);
										}
									};
									makePaymentThread.start();

								} else {
									Toast.makeText(mContext, "Please Login.",
											Toast.LENGTH_LONG).show();
								}

							}
						});

				// Setting Negative "NO" Button
				alertDialog.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								// User pressed No button. Write Logic Here
								dialog.cancel();

							}
						});
				alertDialog.show();
			}
		});

		buyButton = (ImageButton) findViewById(R.id.buyButton);
		buyButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				final int transactionPrice = itemData.price * 2;

				AlertDialog.Builder alertDialog = new AlertDialog.Builder(
						DetailsActivity.this);

				// Setting Dialog Title
				alertDialog.setTitle("NollyGold");
				alertDialog.setMessage("N"
						+ String.valueOf(transactionPrice)
						+ " will be deducted from your account.\nDo you wish to continue?");
				alertDialog.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {

								final String userid = new ServiceAccess()
										.checkLoginStatus(mContext);// preferenceManager.getString("ilogin",
																	// null);

								if (userid != null) {

									actionLay.setEnabled(false);

									paymentInProgress = true;

									isBuy = true;

									pd = new ProgressDialog(mContext);
									pd.setTitle("Processing...");
									pd.setCancelable(false);
									pd.setIndeterminate(true);
									pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
									pd.show();

									Thread makePaymentThread = new Thread() {
										public void run() {

											paymentResult = new ServiceAccess()
													.makePayment(
															0,
															itemData.code,
															transactionPrice,
															itemData.expiryDays,
															userid, 1, mContext);

											paymentHandler.sendEmptyMessage(0);
										}
									};
									makePaymentThread.start();

								} else {
									Toast.makeText(mContext, "Please Login.",
											Toast.LENGTH_LONG).show();
								}

							}
						});

				// Setting Negative "NO" Button
				alertDialog.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								// User pressed No button. Write Logic Here
								dialog.cancel();

							}
						});
				alertDialog.show();

				// ////////////////////////////////////////////////////
			}
		});

		trailerButton = (ImageButton) findViewById(R.id.trailerButton);
		trailerButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				String videoId = itemData.trailerStreamUrl;

				if (videoId == "-") {
					Toast.makeText(mContext, "Not available.",
							Toast.LENGTH_LONG).show();
					return;
				}

				if (videoId.contains("http://") || videoId.contains("rtsp://")
						|| videoId.contains("https://")) {

					Intent playIntent = new Intent(mContext,
							VideoPlayerActivity.class);
					playIntent.putExtra("video_path", videoId);
					playIntent.putExtra("isUrl", true);
					playIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
					startActivity(playIntent);

				} else {

					Intent streamIntent = new Intent(mContext,
							VideoStreamActivity.class);
					streamIntent.putExtra("video_id", videoId);
					streamIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
					startActivity(streamIntent);
				}

			}
		});

		streamButton = (ImageButton) findViewById(R.id.streamButton);
		streamButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				String videoId = itemData.movieStreamUrl;

				if (videoId == "-") {
					Toast.makeText(mContext, "Not available.",
							Toast.LENGTH_LONG).show();
					return;
				}

				if (videoId.contains("http://") || videoId.contains("rtsp://")
						|| videoId.contains("https://")) {

					Intent playIntent = new Intent(mContext,
							VideoPlayerActivity.class);
					playIntent.putExtra("video_path", videoId);
					playIntent.putExtra("isUrl", true);
					playIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
					startActivity(playIntent);

				} else {

					Intent streamIntent = new Intent(mContext,
							VideoStreamActivity.class);
					streamIntent.putExtra("video_id", videoId);
					streamIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
					startActivity(streamIntent);
				}

			}
		});

		downloadButton = (ImageButton) findViewById(R.id.downloadButton);
		downloadButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				AlertDialog.Builder alertDialog = new AlertDialog.Builder(
						DetailsActivity.this);

				// Setting Dialog Title
				alertDialog.setTitle("NollyGold");
				alertDialog
						.setMessage("Download may exceed 100mb. You may incur additional data costs.\nDo you wish to continue?");
				alertDialog.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {

								startDownload();
							}
						});

				// Setting Negative "NO" Button
				alertDialog.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								// User pressed No button. Write Logic Here
								dialog.cancel();

							}
						});
				alertDialog.show();
			}
		});

		shareButton = (ImageButton) findViewById(R.id.shareButton);
		shareButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				Intent shareIntent = new Intent(
						android.content.Intent.ACTION_SEND);
				shareIntent.setType("text/plain");
				shareIntent.putExtra(android.content.Intent.EXTRA_TITLE,
						"NollyGold App Movie Share");
				shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,
						"Now viewing '" + itemData.title
								+ "' on NollyGold App for Android."
								+ "Download LINK");
				startActivity(Intent.createChooser(shareIntent, "Share via"));
			}
		});

		favouriteButton = (ImageButton) findViewById(R.id.favouriteButton);
		favouriteButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				final String userid = new ServiceAccess()
						.checkLoginStatus(mContext);

				if (userid != null) {

					actionLay.setEnabled(false);

					Thread getDataThread = new Thread() {
						public void run() {
							// DatabaseHelper db = new
							// DatabaseHelper(getActivity().getApplicationContext());
							favResult = new ServiceAccess().storeFavourites(
									userid, itemData.code, itemData.title,
									itemData.code.hashCode());
							favHandler.sendEmptyMessage(0);
						}
					};
					getDataThread.start();
				} else {
					Toast.makeText(mContext, "Please Login.", Toast.LENGTH_LONG)
							.show();
				}
				/*
				 * try { DatabaseHelper db = new DatabaseHelper(
				 * getApplicationContext());
				 * db.saveFavourite(itemData.code.hashCode(), itemData.code,
				 * itemData.title);
				 * 
				 * Toast.makeText(mContext, "Movie added to favourites.",
				 * Toast.LENGTH_LONG).show(); } catch (Exception e) { }
				 */

			}
		});

		titleView = (TextView) findViewById(R.id.titleView);
		yearView = (TextView) findViewById(R.id.yearView);
		priceView = (TextView) findViewById(R.id.priceView);
		sourceTextView = (TextView) findViewById(R.id.sourceTextView);

		loadDetailsHandler = new LoadDetailsHandler();
		loadUserReviewHandler = new LoadUserReviewHandler();
		paymentHandler = new MakePaymentHandler();
		favHandler = new FavHandler();

		if (itemData == null)
			loadDetails();
		else {
			showData();
		}
	}

	static void showData() {

		if (itemData != null) {
			shareLay.setVisibility(View.VISIBLE);
			favouriteLay.setVisibility(View.VISIBLE);

			titleView.setText(itemData.title);
			yearView.setText(itemData.year);
			sourceTextView.setText("Source: " + itemData.source);

			pTextView.setText(itemData.producer);
			cTextView.setText(itemData.cast);
			sTextView.setText(itemData.synopsis);

			boolean free = itemData.price == 0 ? true : false;

			priceView.setText(free ? "Free" : ("N" + itemData.price));

			if (isRented) {
				buyLay.setVisibility(View.VISIBLE);
				streamLay.setVisibility(View.VISIBLE);
			} else if (isPurchased) {
				streamLay.setVisibility(View.VISIBLE);
				downloadLay.setVisibility(View.VISIBLE);

			} else {
				if (free) {
					// buyButton.setVisibility(Button.GONE);
					// rentButton.setVisibility(Button.GONE);
					streamLay.setVisibility(View.VISIBLE);
				} else {

					if (itemData.movieDownloadUrl.length() > 2)
						buyLay.setVisibility(View.VISIBLE);

					rentLay.setVisibility(View.VISIBLE);
				}
			}

			if (itemData.trailerStreamUrl.length() > 2)
				trailerLay.setVisibility(View.VISIBLE);

			if (isFavourite)
				favouriteLay.setVisibility(View.GONE);

			imageLoader.DisplayImage(serverImageUrl + itemData.code + ".jpg",
					R.drawable.gradient_bg, imageView);
		} else
			Toast.makeText(mContext, "Could not load data.", Toast.LENGTH_LONG)
					.show();
	}

	@Override
	public Object onRetainNonConfigurationInstance() {

		return itemData;
	}

	static void addReview(String name, String review) {

		String oldReviewContent = rTextView.getText().toString();

		String newReviewContent = "By " + (name == "" ? "Anonymous" : name)
				+ "\n" + review + "\n\n";

		rTextView.setText(oldReviewContent + newReviewContent);
	}

	void loadDetails() {

		Thread loadDetails = new Thread() {
			public void run() {

				itemData = new ServiceAccess().getItemData(itemCode);

				loadDetailsHandler.sendEmptyMessage(0);
			}
		};
		loadDetails.start();
	}

	void startDownload() {

		if (itemData.movieDownloadUrl == "-") {
			Toast.makeText(mContext, "Not available.", Toast.LENGTH_LONG)
					.show();
			return;
		}

		String destUrl = Environment.getExternalStorageDirectory().getPath()
				+ "/nollygold/" + itemData.code;

		Intent downloadIntent = new Intent(getApplicationContext(),
				DownloadService.class);
		downloadIntent.putExtra("downloadurl", itemData.movieDownloadUrl);
		downloadIntent.putExtra("desturl", destUrl);
		downloadIntent.putExtra("itemcode", itemData.code);
		downloadIntent.putExtra("title", itemData.title);
		startService(downloadIntent);
		/*
		 * IntentFilter filter = new IntentFilter(
		 * DownloadManager.ACTION_DOWNLOAD_COMPLETE);
		 * registerReceiver(downloadReceiver, filter);
		 * 
		 * downloadManager = (DownloadManager)
		 * getSystemService(DOWNLOAD_SERVICE); Uri Download_Uri =
		 * Uri.parse(itemData.movieDownloadUrl); DownloadManager.Request request
		 * = new DownloadManager.Request( Download_Uri);
		 * request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
		 * DownloadManager.Request.NETWORK_MOBILE);
		 * request.setAllowedOverRoaming(false);
		 * request.setTitle("NollyGold Movie Download");
		 * request.setDescription("Title:\n" + itemData.title);
		 * 
		 * String destUri = Environment.getExternalStorageDirectory().getPath()
		 * + "/nollygold/" + itemData.code;
		 * request.setDestinationUri(Uri.fromFile(new File(destUri)));
		 * 
		 * // Enqueue a new download and same the referenceId downloadReference
		 * = downloadManager.enqueue(request);
		 */

		/*
		 * DatabaseHelper db = new DatabaseHelper(getApplicationContext());
		 * db.storeLibraryData(itemData.code, itemData.title, destUrl,
		 * downloadReference);
		 */

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.details, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// Handle action buttons
		switch (item.getItemId()) {

		case R.id.action_login: {
			new ServiceAccess().showLoginDialog(mContext);
			return true;
		}
		case R.id.action_websearch: {
			new ServiceAccess().showSearchDialog(mContext);
			return true;
		}
		/*
		 * case R.id.action_check_downloads: { Intent intent = new Intent();
		 * intent.setAction(DownloadManager.ACTION_VIEW_DOWNLOADS);
		 * startActivity(intent); }
		 */
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	static class MakePaymentHandler extends Handler {

		public void handleMessage(Message msg) {

			actionLay.setEnabled(true);

			paymentInProgress = false;

			pd.dismiss();

			if ((paymentResult == 801) || (paymentResult == 601)) {// rent

				if (paymentResult == 601)
					Toast.makeText(mContext,
							"You have previously rented this item.",
							Toast.LENGTH_LONG).show();
				else if (paymentResult == 801)
					Toast.makeText(mContext, "Payment successful.",
							Toast.LENGTH_LONG).show();

				streamLay.setVisibility(View.VISIBLE);
				rentLay.setVisibility(View.GONE);

			} else if ((paymentResult == 901) || (paymentResult == 701)) {// buy

				if (paymentResult == 701)
					Toast.makeText(mContext,
							"You have previously purchased this item.",
							Toast.LENGTH_LONG).show();
				else if (paymentResult == 901)
					Toast.makeText(mContext, "Payment successful.",
							Toast.LENGTH_LONG).show();

				streamLay.setVisibility(View.VISIBLE);
				downloadLay.setVisibility(View.VISIBLE);
				buyLay.setVisibility(View.GONE);
				rentLay.setVisibility(View.GONE);
			} else if (paymentResult == 805) {
				Toast.makeText(mContext, "Database error.", Toast.LENGTH_LONG)
						.show();
			} else if (paymentResult == 111) {// No SIM Card
				Toast.makeText(mContext, "No SIM Card found.",
						Toast.LENGTH_LONG).show();
			} else if (paymentResult == 888) {
				Toast.makeText(mContext, "Service Provider Not Supported.",
						Toast.LENGTH_LONG).show();
			} else if (paymentResult == 507) {
				Toast.makeText(mContext,
						"Movie already purchased. Cannot be rented.",
						Toast.LENGTH_LONG).show();

				streamLay.setVisibility(View.VISIBLE);
				downloadLay.setVisibility(View.VISIBLE);
				buyLay.setVisibility(View.GONE);
				rentLay.setVisibility(View.GONE);
			} else
				Toast.makeText(mContext, "Unknown Error", Toast.LENGTH_LONG)
						.show();
		}
	}

	static class LoadUserReviewHandler extends Handler {

		public void handleMessage(Message msg) {

			if ((reviews != null) && (reviews.length > 0)) {
				isReviewLoaded = true;
				for (int i = 0; i < reviews.length; i++) {

					String name, review;
					name = reviews[i].substring(0, reviews[i].indexOf(":"));
					review = reviews[i].substring(reviews[i].indexOf(":") + 1);

					addReview(name, review);
				}

				if (rTextView.getVisibility() == View.GONE) {
					rTextView.setVisibility(View.VISIBLE);
					reviewButton.setImageResource(R.drawable.up);
				} else {
					rTextView.setVisibility(View.GONE);
					reviewButton.setImageResource(R.drawable.down);
				}
			} else {
				Toast.makeText(mContext, "No reviews.", Toast.LENGTH_LONG)
						.show();
			}
		}

	}

	static class LoadDetailsHandler extends Handler {

		public void handleMessage(Message msg) {

			showData();

		}
	}

	static class FavHandler extends Handler {

		public void handleMessage(Message msg) {

			actionLay.setEnabled(true);

			if (favResult) {
				Toast.makeText(mContext, "Movie added to favourites.",
						Toast.LENGTH_LONG).show();
			} else
				Toast.makeText(mContext, "Unable to add movie to favourites.",
						Toast.LENGTH_LONG).show();

		}
	}

	void destroyDetails() {

		isRented = false;
		isPurchased = false;
		isBuy = false;
		isFavourite = false;
		reviews = null;
		isReviewLoaded = false;
		itemCode = null;
		itemData = null;
		paymentResult = 0;
		paymentInProgress = false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {

			if (paymentInProgress) {
				Toast.makeText(mContext, "Payment transaction in progress...",
						Toast.LENGTH_LONG).show();
				return true;
			} else
				destroyDetails();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onDestroy() {
		destroyDetails();
		super.onDestroy();
	}

	/*
	 * private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
	 * 
	 * @Override public void onReceive(Context context, Intent intent) {
	 * 
	 * // check if the broadcast message is for our Enqueued download long
	 * referenceId = intent.getLongExtra( DownloadManager.EXTRA_DOWNLOAD_ID,
	 * -1); if (downloadReference == referenceId) {
	 * 
	 * } } };
	 */

}
