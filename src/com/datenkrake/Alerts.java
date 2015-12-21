package com.datenkrake;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

/**
 * Shows different kinds of Dialogs in the given context.
 * 
 * @author svenja
 */
public class Alerts {
	final static String NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";

	/**
	 * Shows the "About this App"-Dialog.
	 */
	public static void showAboutDialog(final Context context) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				context);

		alertDialogBuilder.setTitle(R.string.title_dialog_about);
		alertDialogBuilder.setMessage(R.string.dialog_text_about);
		alertDialogBuilder.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
    }
	
	/**
	 * Shows the "Help"-Dialog.
	 */
	public static void showSimpleOKDialog(Context context, String title,
			String text) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				context);

		alertDialogBuilder.setTitle(title);
		alertDialogBuilder.setMessage(text);
		alertDialogBuilder.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
    }
	
	/**
	 * Shows the "Welcome"-Dialog.
	 */
	public static void showWelcomeDialog(final Context context) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				context);
		alertDialogBuilder.setTitle(R.string.title_dialog_welcome);
		alertDialogBuilder.setMessage(R.string.dialog_text_welcome);
		alertDialogBuilder.setPositiveButton(R.string.give_notification_access,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						context.startActivity(new Intent(
								NOTIFICATION_LISTENER_SETTINGS));
					}
				});
		alertDialogBuilder.setNegativeButton(R.string.not_now,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
    }
	
	/**
	 * Shows a Dialog for configuration problems.
	 */
	public static void showConfigurationDialog(final Context context,
			boolean contacts, boolean access) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				context);
		alertDialogBuilder.setTitle(R.string.title_dialog_configure);

		if (!access) {
			if (!contacts) {
				alertDialogBuilder
						.setMessage(R.string.dialog_text_config_contactlist_and_notif_access);
			} else {
				alertDialogBuilder
						.setMessage(R.string.dialog_text_config_notif_access);
			}
			alertDialogBuilder.setPositiveButton(
					R.string.give_notification_access,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							context.startActivity(new Intent(
									NOTIFICATION_LISTENER_SETTINGS));
						}
					});
			alertDialogBuilder.setNegativeButton(R.string.not_now,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
		}
		if (!contacts) {
			alertDialogBuilder
					.setMessage(R.string.dialog_text_config_contactlist);
			alertDialogBuilder.setNeutralButton(R.string.ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
						}
					});
		}
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}
}
