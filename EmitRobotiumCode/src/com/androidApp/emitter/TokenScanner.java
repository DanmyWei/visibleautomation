package com.androidApp.emitter;

import java.util.List;

import com.androidApp.util.Constants;

public class TokenScanner {
	public interface Predicate {
		boolean test(List<String> tokens);
	}
	
	/**
	 * scan forward for the line which matches the passed predicate
	 * @param lines tokenized lines
	 * @param startIndex starting index
	 * @param predicate predicate to test
	 * @return
	 */
	public static int scanForward(List<List<String>> lines, int startIndex, Predicate predicate) {
		for (int i = startIndex; i < lines.size(); i++) {
			List<String> tokens = lines.get(i);
			if (predicate.test(tokens)) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * scan backward for the line which matches the passed predicate
	 * @param lines tokenized lines
	 * @param startIndex starting index
	 * @param predicate predicate to test
	 * @return
	 */
	
	public static int scanBackward(List<List<String>> lines, int startIndex, Predicate predicate) {
		for (int i = startIndex; i >= 0; i--) {
			List<String> tokens = lines.get(i);
			if (predicate.test(tokens)) {
				return i;
			}
		}
		return -1;
	}
	
	public static boolean happensBefore(List<List<String>> 	lines, 
										int 				startIndex, 
										Predicate 			before, 
										Predicate 			after) {
		boolean beforeHappened = true;
		for (int i = startIndex; i < lines.size(); i++) {
			List<String> tokens = lines.get(i);
			if (!beforeHappened) {
				beforeHappened = before.test(tokens);
			} else {
				if (after.test(tokens)) {
					return true;
				}
			}
		}
		return false;		
	}
	
	public class UserEventPredicate implements Predicate {
		public boolean test(List<String> tokens) {
			String action = tokens.get(0);
			return Constants.UserEvent.isUserEvent(action);
		}
	}
	
	public class SystemEventPredicate implements Predicate {
		public boolean test(List<String> tokens) {
			String action = tokens.get(0);
			return Constants.SystemEvent.isSystemEvent(action);
		}
	}
	
	public class EventListPredicate implements Predicate {
		protected String[] mEvents;
		
		public EventListPredicate(String[] events) {
			mEvents = events;
		}
		public EventListPredicate(String event) {
			mEvents = new String[1];
			mEvents[0] = event;
		}
	
		public boolean test(List<String> tokens) {
			String action = tokens.get(0);
			for (String event : mEvents) {
				if (event.equals(action)) {
					return true;
				}
			}
			return false;
		}
	}
	
	public class ActivityTransitionPredicate extends EventListPredicate {
		public ActivityTransitionPredicate() {
			super(new String[] { Constants.ActivityEvent.ACTIVITY_BACK.mEventName,
								 Constants.ActivityEvent.ACTIVITY_FORWARD.mEventName,
								 Constants.UserEvent.ACTIVITY_BACK_KEY.mEventName });
		}
	}
	
	public class InterstitialDialogPredicate extends EventListPredicate {
		public InterstitialDialogPredicate() {
			super(new String[] { 
					Constants.UserEvent.INTERSTITIAL_DIALOG_TITLE_ID.mEventName,
					Constants.UserEvent.INTERSTITIAL_DIALOG_TITLE_TEXT.mEventName,
					Constants.UserEvent.INTERSTITIAL_DIALOG_CONTENTS_ID.mEventName,
					Constants.UserEvent.INTERSTITIAL_DIALOG_CONTENTS_TEXT.mEventName });
		}
	}
	
	public class DialogClosePredicate extends EventListPredicate {
		public DialogClosePredicate() {
			super(new String[] {
					Constants.SystemEvent.DISMISS_DIALOG.mEventName,
					Constants.UserEvent.CANCEL_DIALOG.mEventName });
		}
	}
}
