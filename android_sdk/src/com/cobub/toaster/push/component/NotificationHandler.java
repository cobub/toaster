package com.cobub.toaster.push.component;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat.Builder;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.cobub.toaster.push.api.CPushInterface;
import com.cobub.toaster.push.cont.CommConst;
import com.cobub.toaster.push.model.ErrorRespPacket;
import com.cobub.toaster.push.model.NetPacket;
import com.cobub.toaster.push.util.BaseUtility;

/*
 * need Permission <uses-permission android:name="android.permission.VIBRATE"/>
 */
public class NotificationHandler extends PushDataHandlerItem {
	private String mType = "notification";

	@Override
	public boolean check(String type) {
		// TODO Auto-generated method stub
		return mType.equals(type);
	}

	@Override
	public NetPacket handle(Context context, JSONObject json,
			NetPacket rawPacket) {
		// TODO Auto-generated method stub
		try {
			String style = json.getString("style");
			JSONObject configJs = json.getJSONObject("config");
			String icondata = configJs.getString("icondata");
			String vibrate = configJs.optString("vibrate", "0");
			String sound = configJs.optString("sound", "0");
			String title = configJs.getString("title");
			String ticker = configJs.getString("ticker");
			String body = configJs.getString("body");
			JSONObject clickconfig = configJs.getJSONObject("clickconfig");

			// retrieve a NotificationManager
			NotificationManager notifManager = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			try {
				Notification notification = handleNotificationStyle(context,
						Integer.parseInt(style), icondata, vibrate, sound,
						title, ticker, body, clickconfig);
				if (notification == null)
					throw new Exception("no valid notification found");
				notifManager.notify((int) System.currentTimeMillis(),
						notification);
				CPushInterface.sendReadFeedback(context, rawPacket.getValueByKey("mid", "0"), rawPacket.getValueByKey("expired", "-1"));
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				Map<String, String> errorMap = BaseUtility.makeErrorResopnse(
						"411", "build notification error:" + e.getMessage());
				return new ErrorRespPacket(errorMap, true);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Map<String, String> errorMap = BaseUtility.makeErrorResopnse(
						"411", "build notification error:" + e.getMessage());
				return new ErrorRespPacket(errorMap, true);
			}
		} catch (JSONException e) {
			Map<String, String> errorMap = BaseUtility.makeErrorResopnse("410",
					"Json parse error:" + e.getMessage());
			return new ErrorRespPacket(errorMap, true);
		}
		return rawPacket;
	}

	private PendingIntent makePI4ClickConfig(Context ctx, JSONObject clkConfigJs)
			throws JSONException {
		String operation = clkConfigJs.getString("operation");
		if ("launchActivity".equals(operation)) {
			/*
			 * op of launchActivity
			 */
			Intent intent4launch = new Intent();
			intent4launch.setAction(CommConst.ACTION4LAUNCHCMD);
			intent4launch.putExtra("package", clkConfigJs.getString("package"));
			intent4launch.putExtra("targetActivity",
					clkConfigJs.getString("targetActivity"));
			PendingIntent pi = PendingIntent.getBroadcast(ctx, (int)System.currentTimeMillis(),
					intent4launch, PendingIntent.FLAG_CANCEL_CURRENT);
			return pi;
		} else if ("download".equals(operation)) {
			/*
			 * op of download
			 */
			Intent intent4dl = new Intent(ctx, DownloadService.class);
			intent4dl.putExtra("appname", clkConfigJs.getString("appname"));
			intent4dl.putExtra("dlurl", clkConfigJs.getString("dlurl"));
			PendingIntent pi = PendingIntent.getService(ctx, (int)System.currentTimeMillis(), intent4dl,
					PendingIntent.FLAG_CANCEL_CURRENT);
			return pi;
		} else if ("loadWebpage".equals(operation)) {
			/*
			 * op of loadWebpage
			 */
			Intent intent4loadwp = new Intent(ctx, BrowserAct.class);
			intent4loadwp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			String url = clkConfigJs.optString("url");
			String page = clkConfigJs.optString("page");
			if (!TextUtils.isEmpty(url)) {
				intent4loadwp.putExtra("url", url);
			}
			if (!TextUtils.isEmpty(page)) {
				intent4loadwp.putExtra("page", page);
			}
			PendingIntent pi = PendingIntent.getActivity(ctx, (int)System.currentTimeMillis(), intent4loadwp,
					PendingIntent.FLAG_CANCEL_CURRENT);
			return pi;
		} else {
			// record unknown click config
			Map<String, String> unknownMap = BaseUtility.makeErrorResopnse(
					"406", "click config type not found");
			BaseUtility.logErrorPacket(ctx, new ErrorRespPacket(unknownMap,
					true));
		}
		return null;
	}

	private Notification handleNotificationStyle(Context context,
			int styleIndex, String icondata, String vibrate, String sound,
			String title, String ticker, String body, JSONObject clickConfigJs)
			throws Exception {
		/*
		 * !!!!ignore style at this version!!!!
		 */
		PendingIntent pi = makePI4ClickConfig(context, clickConfigJs);
		if (pi == null)
			return null;
		Bitmap icon = decodeBmp(icondata);
		Builder builder = new Builder(context);
		builder.setContentIntent(pi).setTicker(ticker).setContentTitle(title)
				.setContentText(body).setAutoCancel(true)
				.setSmallIcon(android.R.drawable.ic_menu_info_details);
		if(icon != null){
			builder.setLargeIcon(icon);
		}
		Notification notification = builder.build();
		if (vibrate.equals("1")) {
			notification.defaults |= Notification.DEFAULT_VIBRATE;
		}
		if (sound.equals("1")) {
			notification.defaults |= Notification.DEFAULT_SOUND;
		}
		return notification;
		// notification.tickerText = ticker;
		// notification.setLatestEventInfo(context, title, body, pi);

		// RemoteViews remoteViews = notification.contentView;
		// Class<?> clazz = Class.forName("com.android.internal.R$id");
		// Field field = clazz.getField("icon");
		// field.setAccessible(true);
		// int id_icon = field.getInt(null);
		// View localView =
		// LayoutInflater.from(context.getApplicationContext()).inflate(notification.contentView.getLayoutId(),
		// null);
		// ImageView imageView = getImaeView(localView);
		// if(notification.contentView != null && icon != null){
		// notification.contentView.setImageViewBitmap(imageView.getId(), icon);
		// }else{
		// }
		// notification.icon = android.R.drawable.sym_def_app_icon;
	}

	protected static ImageView getImaeView(View paramView) {
		if (paramView instanceof ImageView)
			return (ImageView) paramView;
		if (paramView instanceof ViewGroup)
			for (int i = 0; i < ((ViewGroup) paramView).getChildCount(); ++i) {
				View localView = ((ViewGroup) paramView).getChildAt(i);
				if (localView instanceof ImageView)
					return (ImageView) localView;
				if (localView instanceof ViewGroup)
					return getImaeView(localView);
			}
		return null;
	}

	private Bitmap decodeBmp(String data) {
		byte[] rawBytes = Base64.decode(data, Base64.DEFAULT);
		Bitmap raw = BitmapFactory
				.decodeByteArray(rawBytes, 0, rawBytes.length);
		// Bitmap scaledBmp = Bitmap.createScaledBitmap(raw, 64, 64, true);
		return raw;
	}

}
