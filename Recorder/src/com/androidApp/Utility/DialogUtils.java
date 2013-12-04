package com.androidApp.Utility;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.Window.Callback;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import com.androidApp.Utility.Constants.Classes;
import com.androidApp.Utility.Constants.Fields;

/**
 * utilities for dialogs, popup windows, autocomplete dropdowns, spinner popups, etc.
 * @author matt2
 *
 */
public class DialogUtils {

	/**
	 * for a given spinner, see if this dialog is the spinner's popup dialog
	 * @param dialog dialog
	 * @param spinner candidate spinner
	 * @return true if it belongs, false if it does not
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public static boolean isPopupDialogForSpinner(Dialog dialog, Spinner spinner) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
		Object spinnerPopup = ReflectionUtils.getFieldValue(spinner, Spinner.class, Constants.Fields.POPUP);
		if (spinnerPopup != null) {
			Class spinnerDialogPopupClass = Class.forName(Constants.Classes.SPINNER_DIALOG_POPUP);
			if (spinnerDialogPopupClass.equals(spinnerPopup.getClass())) {
				Object spinnerPopupPopup = ReflectionUtils.getFieldValue(spinnerPopup, spinnerDialogPopupClass, Constants.Fields.POPUP);
				return spinnerPopupPopup == dialog;
			}
		}
		return false;
	}

	public static Spinner isSpinnerDialog(Dialog dialog, Activity activity)  throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
		List<Spinner> spinnerList = ViewExtractor.getActivityViews(activity, Spinner.class);
		for (Spinner spinner : spinnerList) {
			if (isPopupDialogForSpinner(dialog, spinner)) {
				return spinner;
			}
		}
		return null;
	}

    /** 
     * spinner dialogs have to be handled differently.  We search for an adapter of the spinner dropdown type.
     * @param contentView
     * @return
     */  
    public static boolean isSpinnerDialog(View contentView)  throws ClassNotFoundException {
        List<View> listList = TestUtils.getChildrenByClass(contentView, AdapterView.class);
        Class spinnerAdapterClass = Class.forName(Constants.Classes.SPINNER_ADAPTER);
        for (View v : listList) {
            AdapterView adapterView = (AdapterView) v;
            Adapter adapter = adapterView.getAdapter();
            if (adapter.getClass() == spinnerAdapterClass) {
                return true;
            }                   
        }               
        return false;
    }           

	/**
	 * spinners can have popups (which are actually dropdowns), or dialog windows depending on the mode: MODE_DIALOG or mode: MODE_POPUP
	 * @param popupWindow
	 * @return
	 */
	public static boolean isSpinnerPopup(PopupWindow popupWindow) throws NoSuchFieldException, IllegalAccessException {
		View anchorView = DialogUtils.getPopupWindowAnchor(popupWindow);
		return (anchorView instanceof Spinner);
	}

	/**
	 * geting a dialog title is tricky, because they didn't provide an accessor function for it, AND
	 * it's an internal view (which fortunately derives from a TextView)
	 * @param dialog
	 * @return TextView or null if not found
	 */
	public static TextView getDialogTitleView(Dialog dialog) {
		try {
			Class<? extends View> dialogTitleClass = (Class<? extends View>) Class.forName(Constants.Classes.DIALOG_TITLE);
			Window window = dialog.getWindow();
			View decorView = window.getDecorView();
			TextView dialogTitle = (TextView) TestUtils.findChild(decorView, 0, dialogTitleClass);
			if (dialogTitle != null) {
				return dialogTitle;
			} else {
				dialogTitle = (TextView) TestUtils.findChild(decorView, 0, TextView.class);
				return dialogTitle;
			}
		} catch (ClassNotFoundException cnfex) {
			Log.e(TestUtils.TAG, "failed to find dialog title");
			return null;
		}
	}

	/**
	 * get the content view of a dialog so we can intercept it with a MagicFrame
	 * @param dialog
	 * @return
	 */
	public static View getDialogContentView(Dialog dialog) {
		Window window = dialog.getWindow();
		View decorView = window.getDecorView();
		View contentView = ((ViewGroup) decorView).getChildAt(0);
		return contentView;
	}

	/**
	 * given a PhoneWindow$DecorView, see if it has a WindowCallback which derives from Dialog.  If so, then return that
	 * dialog, otherwise return null.
	 * @param phoneWindowDecorView PhoneWindow$DecorView
	 * @return Dialog or null;
	 */
	public static Dialog getDialog(View phoneWindowDecorView) {
		try {
			Class<? extends View> phoneDecorViewClass = (Class<? extends View>) Class.forName(Constants.Classes.PHONE_DECOR_VIEW);
			if (phoneWindowDecorView.getClass() == phoneDecorViewClass) {
				Window phoneWindow = (Window) ReflectionUtils.getFieldValue(phoneWindowDecorView, phoneDecorViewClass, Constants.Classes.THIS);
				Window.Callback callback = phoneWindow.getCallback();
				if (callback instanceof Dialog) {
					Dialog dialog = (Dialog) callback;
					return dialog;
				}
			}
		} catch (Exception ex) {
		}
		return null;
	}

	/**
	 * see if this activity has popped up a dialog.
	 * @param activity activity to test
	 * @return Dialog or null
	 */
	public static Dialog findDialog(Activity activity) {
		try {
			View[] views = ViewExtractor.getWindowDecorViews();
			if (views != null) {
				int numDecorViews = views.length;
				
				// iterate through the set of decor windows.  The dialog may already have been presented.
				for (int iView = 0; iView < numDecorViews; iView++) {
					View v = views[iView];
					if (DialogUtils.isDialogOrPopup(activity, v)) {	
						Dialog dialog = getDialog(v);
						if (dialog != null) {
							return dialog;
						}
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;		
	}

    /** 
     * is this view in the same context as the activity, but has a different window? Then it is contained in a popup window
     * and may or may not be a dialog.
     * @param a the activity
     * @param v a PhoneWindow$DecorView or PopupContainerView or something like that
     * @return true, or maybe false.  probably true, though
     */  
    public static boolean isDialogOrPopup(Activity a, View v) {
        if (v != null) { 
            Context viewContext = v.getContext();
            // dialogs use a context theme wrapper, not a context, so we have to extract he context from the theme wrapper's
            // base context. Dialogs can also have nested Context Theme wrappers, so we have to loop to get the "true"
            // base context.
            while (viewContext instanceof ContextThemeWrapper) {
                ContextThemeWrapper ctw = (ContextThemeWrapper) viewContext;
                viewContext = ctw.getBaseContext();
            }           
            Context activityContext = a;
            Context activityBaseContext = a.getBaseContext();
            return (activityContext.equals(viewContext) || activityBaseContext.equals(viewContext)) && (v != a.getWindow().getDecorView());
        } else {
            return false;
        }       
    }   

	/**
	 * find a view associated with a popup window
	 * @param activity
	 * @param popupWindow
	 * @return
	 */
	public static View findViewForPopup(Activity activity, PopupWindow popupWindow) {
		try {
			Class popupViewContainerClass = Class.forName(Constants.Classes.POPUP_VIEW_CONTAINER);
			View[] views = ViewExtractor.getWindowDecorViews();
			if (views != null) {
				int numDecorViews = views.length;
				
				// iterate through the set of decor windows.  The dialog may already have been presented.
				for (int iView = 0; iView < numDecorViews; iView++) {
					View v = views[iView];
					if (DialogUtils.isDialogOrPopup(activity, v)) {	
						if (v.getClass() == popupViewContainerClass) {
							PopupWindow candPopupWindow = (PopupWindow) ReflectionUtils.getFieldValue(v, popupViewContainerClass, Constants.Classes.THIS);
							if (candPopupWindow == popupWindow) {
								return v;
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	/** 
	 * since we're in a polling loop for popup windows, we can interrogate the window before it's actually populated, 
	 * and we need to check the menu items for callbacks to determine if it's the options menu, if the window is empty, we blow it off
	 * @param popupWindow
	 * @return
	 */
	public static boolean isPopupWindowEmpty(PopupWindow popupWindow) {
		View contentView = popupWindow.getContentView();
		ViewGroup contentViewGroup = (ViewGroup) contentView;
		return contentViewGroup.getChildCount() == 0;
	}

	/**
	 * popup windows are slightly different than dialogs, so we have a separate path which polls for them
	 * to set up in RecordTest
	 * @param activity
	 * @return Object: because it can be a window or a popup window, and they don't inherit from each other
	 */
	public static WindowAndView findPopupWindow(Activity activity) {
		try {
			View[] views = ViewExtractor.getWindowDecorViews();
			if (views != null) {
				int numDecorViews = views.length;
				
				// iterate through the set of decor windows.  The dialog may already have been presented.
				for (int iView = 0; iView < numDecorViews; iView++) {
					View v = views[iView];
					if (isDialogOrPopup(activity, v)) {
						// dialogs are handled in the other case.
						Dialog dialog = getDialog(v);
						if (dialog == null) {
							
							// Toasts and other weird things get picked up as popup windows.
							try {
								Object window = ReflectionUtils.getFieldValue(v, v.getClass(), Constants.Classes.THIS);
								WindowAndView windowAndView = new WindowAndView(window, v);
								return windowAndView;
							} catch (Exception ex) {
								return null;
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;		
	}

	/**
	 * from the popup window, get the content view, then iterate over its children.  Each child contains mItemData, which contains mMenu, which
	 *  has a callback field. If that callback field is PhoneWindow, then it's an options menu, otherwise it's a popup.
	 *  the classes are all internal, so we need to do Class.formName to extract fields.
	 *  popupWindow.mContentView.mChildren[*].mItemData.mMenu.mCallback
	 *  types: android.widget.PopupWindow
	 *  android.widget.ListPopupWindow$DropDownListView
	 *  com.android.internal.view.menu.ListMenuItemView
	 *  com.android.internal.view.menu.MenuItemImpl
	 *  com.android.internal.view.menu.MenuBuilder
	 *  returns Object[] where android.widget.PopupMenu for Popup and PhoneWindow for activity option menu
	 */
	
	public static List<Object> getPopupWindowCallbackList(PopupWindow popupWindow) throws ClassNotFoundException, IllegalAccessException, NoSuchFieldException {
		List<Object> callbackList = new ArrayList<Object>();
		View contentView = popupWindow.getContentView();
		ViewGroup contentViewGroup = (ViewGroup) contentView;
		Class listMenuItemViewClass = Class.forName(Constants.Classes.LIST_MENU_ITEM_VIEW);
		Class menuItemImplClass = Class.forName(Constants.Classes.MENU_ITEM_IMPL);
		Class menuBuilderClass = Class.forName(Constants.Classes.MENU_BUILDER);
		if (contentViewGroup.getChildCount() == 0) {
			Log.i(TestUtils.TAG, "interesting");
		}
		for (int i = 0; i < contentViewGroup.getChildCount(); i++) {
			View menuItemCandView = contentViewGroup.getChildAt(i);
			if (menuItemCandView.getClass() == listMenuItemViewClass) {
				Object itemData = ReflectionUtils.getFieldValue(menuItemCandView, listMenuItemViewClass, Constants.Fields.ITEM_DATA);
				if (itemData.getClass() == menuItemImplClass) {
					Object menu = ReflectionUtils.getFieldValue(itemData, menuItemImplClass, Constants.Fields.MENU);
					if (menu.getClass() == menuBuilderClass) {
						Object callback = ReflectionUtils.getFieldValue(menu, menuBuilderClass, Constants.Fields.CALLBACK);
						callbackList.add(callback);
					}
				}
			}
		}
		return callbackList;		
	}

	/**
	 * some popup windows have anchors, like the overflow menu button in the action bar, or the button in a spinner
	 * @param popupWindow the potentially anchored popup window
	 * @return anchor view or null.
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 */
	public static View getPopupWindowAnchor(PopupWindow popupWindow) throws IllegalAccessException, NoSuchFieldException {
		WeakReference<View> anchorRef = (WeakReference<View>) ReflectionUtils.getFieldValue(popupWindow, PopupWindow.class, Constants.Fields.ANCHOR);
		if (anchorRef != null) {
			return anchorRef.get();
		}
		return null;
	}

	/**
	 * is this popup window the dropdown to an AutoCompleteTextView?
	 * @param popupWindow
	 * @return true if the anchor is AutoCompleteTextView
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 */
	public static boolean isAutoCompleteWindow(PopupWindow popupWindow) throws ClassNotFoundException, IllegalAccessException, NoSuchFieldException {
		View anchorView = getPopupWindowAnchor(popupWindow);
		if (anchorView != null) {
			if (anchorView instanceof AutoCompleteTextView) {
				return true;
			}
		}
		return false;
	}

	/**
	 * is this the popup for the options menu for the activity? see getPopupWindowCallbackList() for details
	 * @param popupWindow popup window to test
	 * @return true if the callbacks for the menu item point back to the phone window.
	 * 
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 */
	public static boolean isOptionsMenu(PopupWindow popupWindow) throws ClassNotFoundException, IllegalAccessException, NoSuchFieldException {
		List<Object> callbackList = getPopupWindowCallbackList(popupWindow);
		Class phoneWindowClass = Class.forName(Constants.Classes.PHONE_WINDOW);
		if (callbackList.isEmpty()) {
			return false;
		} else {
			for (Object callback : callbackList) {
				if (callback.getClass() != phoneWindowClass) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * same, except for "window", not PopupWindow
	 * @param window
	 * @return
	 */
	public static boolean isWindowEmpty(Window window) {
		ViewGroup decorView = (ViewGroup) window.getDecorView();
		ViewGroup contentView = (ViewGroup) decorView.getChildAt(0);		
		ViewGroup contentViewGroup = (ViewGroup) contentView;
		return contentViewGroup.getChildCount() == 0;
	}

	/**
	 * find the view associated with the options menu for this activity
	 * @param activity
	 * @return
	 */
	public static View findOptionsMenu(Activity activity) {
		try {
			View[] views = ViewExtractor.getWindowDecorViews();
			if (views != null) {
				int numDecorViews = views.length;
				
				// iterate through the set of decor windows.  The dialog may already have been presented.
				for (int iView = 0; iView < numDecorViews; iView++) {
					View v = views[iView];
					if (isDialogOrPopup(activity, v)) {	
						if (DialogUtils.isOptionsMenu(v)) {
							return v;
						}
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

    /** 
     * v is actually a PhoneDecorView.  We're looking for its child, which is com.android.internal.view.menu.ExpandedMenuView
     * @param v
     * @return
     */
    public static boolean isOptionsMenu(View v) throws ClassNotFoundException {
        if (v instanceof ViewGroup) {
            View vChild = ((ViewGroup) v).getChildAt(0);
            Class<? extends View> menuViewClass = (Class<? extends View>) Class.forName(Constants.Classes.EXPANDED_MENU_VIEW);
            return vChild.getClass() == menuViewClass;
        } else {
            return false;
        }
    }

}
